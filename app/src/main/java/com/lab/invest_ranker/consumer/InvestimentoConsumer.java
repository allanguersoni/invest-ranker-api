package com.lab.invest_ranker.consumer;

import com.lab.invest_ranker.model.FundoImobiliario;
import com.lab.invest_ranker.repository.FundoImobiliarioRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class InvestimentoConsumer {

    private final FundoImobiliarioRepository repository;

    public InvestimentoConsumer(FundoImobiliarioRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "analise-fii", groupId = "ranker-group")
    public void processarMensagemDoKafka(String mensagem) {
        String ticker = mensagem.split(": ")[1].trim();
        Optional<FundoImobiliario> fundoOpt = repository.findByTicker(ticker);

        if (fundoOpt.isPresent()) {
            FundoImobiliario fundo = fundoOpt.get();
            
            double dyAnual = fundo.getDividendoUltimos12Meses();
            double pvp = (fundo.getPvp() != null && fundo.getPvp() > 0) ? fundo.getPvp() : 1.0;
            double vacancia = (fundo.getVacanciaMedia() != null) ? fundo.getVacanciaMedia() : 0.0;
            int imoveis = (fundo.getQtdImoveis() != null) ? fundo.getQtdImoveis() : 0;

            // Pontuação Base (Matemática pura)
            double scoreRenda = Math.min((dyAnual / 12.0) * 50.0, 50.0);
            double scoreDesconto = Math.max(0, 50.0 - ((Math.abs(pvp - 0.85)) * 100.0)); // P/VP ideal agora é 0.85
            double scoreBase = scoreRenda + scoreDesconto;

            // --- MOTOR DE RISCO E PENALIDADES SRE ---
            int alertasRisco = 0;

            // 1. Armadilha de Valor (O mercado sabe que vai dar ruim)
            if (pvp < 0.65) {
                scoreBase -= 30.0; // Punição severa
                alertasRisco += 2;
            }
            
            // 2. Risco de Inadimplência (Yield irreal)
            if (dyAnual > 15.0) {
                scoreBase -= 20.0;
                alertasRisco += 1;
            }

            // 3. Risco Imobiliário (Fundos de Tijolo)
            if (vacancia > 15.0) {
                scoreBase -= 15.0; // Muito imóvel vazio
                alertasRisco += 1;
            }
            if (imoveis == 1) {
                scoreBase -= 15.0; // Monoativo (se o inquilino sair, a renda zera)
                alertasRisco += 1;
            }

            // Definição do Grau de Risco
            if (alertasRisco >= 2) fundo.setGrauRisco("Alto");
            else if (alertasRisco == 1) fundo.setGrauRisco("Médio");
            else fundo.setGrauRisco("Baixo");

            // Trava nota entre 0 e 100
            double scoreFinal = Math.max(0, Math.min(scoreBase, 100.0));
            fundo.setScore(Math.round(scoreFinal * 100.0) / 100.0);
            
            repository.save(fundo);
        }
    }
}