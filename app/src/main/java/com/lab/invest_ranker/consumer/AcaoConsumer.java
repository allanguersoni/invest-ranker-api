package com.lab.invest_ranker.consumer;

import com.lab.invest_ranker.model.Acao;
import com.lab.invest_ranker.repository.AcaoRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AcaoConsumer {

    private final AcaoRepository repository;

    public AcaoConsumer(AcaoRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "analise-acao", groupId = "ranker-group")
    public void processarAcao(String mensagem) {
        try {
            // O Kafka manda apenas o Ticker. Ex: "TICKER: PETR4"
            String ticker = mensagem.split(":")[1].trim();
            Optional<Acao> acaoOpt = repository.findByTicker(ticker);

            if (acaoOpt.isPresent()) {
                Acao acao = acaoOpt.get();
                
                double cotacao = acao.getCotacaoAtual() != null ? acao.getCotacaoAtual() : 0.0;
                double pl = acao.getPl() != null ? acao.getPl() : 0.0;
                double pvp = acao.getPvp() != null ? acao.getPvp() : 0.0;
                double roe = acao.getRoe() != null ? acao.getRoe() : 0.0;
                double dy = acao.getDividendoUltimos12Meses() != null ? acao.getDividendoUltimos12Meses() : 0.0;

                // 1. FILTRO DE LIXO SRE (Empresas dando prejuízo ou sem dados)
                if (pl <= 0 || pvp <= 0 || cotacao <= 0) {
                    acao.setScore(0.0);
                    acao.setPrecoJustoGraham(0.0);
                    acao.setMargemSeguranca(0.0);
                    acao.setRecomendacao("CILADA (Prejuízo/Sem Liquidez)");
                    repository.save(acao);
                    return;
                }

                // 2. ENGENHARIA REVERSA (LPA e VPA)
                double lpa = cotacao / pl; // Lucro por Ação
                double vpa = cotacao / pvp; // Valor Patrimonial por Ação

                // 3. FÓRMULA DE BENJAMIN GRAHAM (Protegida contra raiz negativa)
                double valorIntrinseco = 22.5 * lpa * vpa;
                double precoJusto = 0.0;
                if (valorIntrinseco > 0) {
                    precoJusto = Math.sqrt(valorIntrinseco);
                }
                acao.setPrecoJustoGraham(Math.round(precoJusto * 100.0) / 100.0);

                // 4. MARGEM DE SEGURANÇA (O Desconto real)
                double margem = ((precoJusto / cotacao) - 1.0) * 100.0;
                acao.setMargemSeguranca(Math.round(margem * 100.0) / 100.0);

                // 5. MOTOR DE SCORE (0 a 100)
                double scoreBase = 0.0;
                
                // Pontua desconto (Até 50 pts)
                if (margem > 0) {
                    scoreBase += Math.min(margem, 50.0);
                }
                
                // Pontua eficiência/ROE (Até 30 pts)
                if (roe > 0) {
                    scoreBase += Math.min((roe / 20.0) * 30.0, 30.0);
                }
                
                // Pontua dividendos (Até 20 pts)
                if (dy > 0) {
                    scoreBase += Math.min((dy / 10.0) * 20.0, 20.0);
                }

                // Trava o score entre 0 e 100
                double scoreFinal = Math.max(0.0, Math.min(scoreBase, 100.0));
                acao.setScore(Math.round(scoreFinal * 100.0) / 100.0);

                // 6. RECOMENDAÇÃO FINAL
                if (margem >= 30.0 && roe >= 10.0) {
                    acao.setRecomendacao("COMPRA FORTE \uD83D\uDE80"); // Foguete
                } else if (margem > 0 && margem < 30.0) {
                    acao.setRecomendacao("BOA OPÇÃO \uD83D\uDFE2"); // Círculo verde
                } else {
                    acao.setRecomendacao("CARO / AFASTAR \uD83D\uDD34"); // Círculo vermelho
                }

                repository.save(acao);
                
                // Log limpo para você ver o motor girando no terminal
                System.out.println("🧠 [GRAHAM IA] " + ticker + " processada | Preço Justo: R$ " + acao.getPrecoJustoGraham() + " | Margem: " + acao.getMargemSeguranca() + "%");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar ação no Kafka: " + e.getMessage());
        }
    }
}