package com.lab.invest_ranker.consumer;

import com.lab.invest_ranker.model.RendaFixa;
import com.lab.invest_ranker.repository.RendaFixaRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class RendaFixaConsumer {

    private final RendaFixaRepository repository;

    public RendaFixaConsumer(RendaFixaRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "analise-rendafixa", groupId = "ranker-group")
    public void processarRendaFixa(String mensagem) {
        try {
            // O padrão da mensagem que enviamos no Controller é "ID: 1"
            String[] partes = mensagem.split(":");
            if (partes.length < 2) return;
            
            Long id = Long.parseLong(partes[1].trim());
            Optional<RendaFixa> ativoOpt = repository.findById(id);

            if (ativoOpt.isPresent()) {
                RendaFixa ativo = ativoOpt.get();

                // Evita NullPointer caso a raspagem venha suja
                double taxaBruta = ativo.getTaxaRendimento() != null ? ativo.getTaxaRendimento() : 0.0;
                double taxaLiquida = taxaBruta;
                double aliquotaIR = 0.0;

                // 1. MOTOR DE IMPOSTO DE RENDA (Tabela Regressiva)
                boolean isento = ativo.getIsentoIR() != null && ativo.getIsentoIR();
                
                if (!isento) {
                    long diasAteVencimento = 721; // Padrão seguro para títulos longos ou sem data
                    if (ativo.getDataVencimento() != null) {
                        diasAteVencimento = ChronoUnit.DAYS.between(LocalDate.now(), ativo.getDataVencimento());
                    }

                    // Regras da Receita Federal Brasileira
                    if (diasAteVencimento <= 180) aliquotaIR = 0.225;      // 22,5%
                    else if (diasAteVencimento <= 360) aliquotaIR = 0.20; // 20,0%
                    else if (diasAteVencimento <= 720) aliquotaIR = 0.175; // 17,5%
                    else aliquotaIR = 0.15;                               // 15,0%

                    // Calcula a taxa real que vai para o bolso do usuário
                    taxaLiquida = taxaBruta * (1.0 - aliquotaIR);
                }

                // Arredonda para 2 casas decimais
                ativo.setRentabilidadeLiquidaAnual(Math.round(taxaLiquida * 100.0) / 100.0);

                // 2. ALGORITMO SRE DE SCORE E RISCO (0 a 100)
                double scoreBase = 40.0; 

                // Benefícios de Liquidez e Segurança
                if (ativo.getLiquidezDiaria() != null && ativo.getLiquidezDiaria()) scoreBase += 20.0; 
                
                // Validação de Risco de Crédito
                if ("TESOURO".equalsIgnoreCase(ativo.getTipoAtivo())) {
                    scoreBase += 25.0; // Risco Soberano (Nota Máxima de Segurança)
                    ativo.setTemFGC(false); // Tesouro não tem FGC, tem garantia do Governo
                } else if (ativo.getTemFGC() != null && ativo.getTemFGC()) {
                    scoreBase += 15.0; // Proteção Fundo Garantidor de Crédito
                } else {
                    scoreBase -= 20.0; // Risco Corporativo sem FGC (Debêntures, CRI, CRA)
                }

                // Premiação por Rentabilidade (Lógica baseada no CDI atual ou IPCA)
                if ("CDI".equals(ativo.getIndexador()) && taxaLiquida > 100.0) {
                    scoreBase += (taxaLiquida - 100.0); // Bônus por bater o CDI líquido
                } else if (("IPCA".equals(ativo.getIndexador()) || "PREFIXADO".equals(ativo.getIndexador())) && taxaLiquida > 6.0) {
                    scoreBase += (taxaLiquida * 1.5); // Títulos que pagam inflação + prêmio alto ganham destaque
                }

                // Trava matemática entre 0 e 100
                double scoreFinal = Math.max(0.0, Math.min(scoreBase, 100.0));
                ativo.setScore(Math.round(scoreFinal * 100.0) / 100.0);

                // 3. DECISÃO AUTOMATIZADA PARA O USUÁRIO FINAL
                if (scoreFinal >= 85.0) {
                    ativo.setRecomendacao("OPORTUNIDADE DE OURO 🏆");
                } else if (scoreFinal >= 65.0) {
                    ativo.setRecomendacao("BOM PARA CAIXA 🟢");
                } else if (scoreFinal < 40.0) {
                    ativo.setRecomendacao("CILADA TRIBUTÁRIA / RISCO ALTO 🔴");
                } else {
                    ativo.setRecomendacao("MANTER 🟡");
                }

                repository.save(ativo);
                
                System.out.println(String.format("🧠 [IA RENDA FIXA] %s processado | Score: %.2f | IR: %.1f%% | %s", 
                                   ativo.getNomeTitulo(), ativo.getScore(), (aliquotaIR * 100), ativo.getRecomendacao()));
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem do Kafka: " + e.getMessage());
        }
    }
}