package com.lab.invest_ranker.service;

import com.lab.invest_ranker.model.FundoImobiliario;
import com.lab.invest_ranker.repository.FundoImobiliarioRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvestimentoService {

    private final FundoImobiliarioRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public InvestimentoService(FundoImobiliarioRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public FundoImobiliario receberNovoFundo(FundoImobiliario novoFundo) {
        // 1. Tenta encontrar o fundo pelo Ticker para evitar duplicidade
        Optional<FundoImobiliario> fundoExistente = repository.findByTicker(novoFundo.getTicker());

        FundoImobiliario fundoParaProcessar;

        if (fundoExistente.isPresent()) {
            // Caso exista, atualizamos apenas os valores brutos recebidos
            fundoParaProcessar = fundoExistente.get();
            fundoParaProcessar.setCotacaoAtual(novoFundo.getCotacaoAtual());
            fundoParaProcessar.setDividendoUltimos12Meses(novoFundo.getDividendoUltimos12Meses());
            System.out.println("[SRE LOG] Ticker " + novoFundo.getTicker() + " já existe. Atualizando dados brutos...");
        } else {
            // Caso não exista, usamos o novo objeto para criar um registro
            fundoParaProcessar = novoFundo;
            System.out.println("[SRE LOG] Novo Ticker detectado: " + novoFundo.getTicker());
        }

        // 2. Salva ou Atualiza no Banco de Dados
        FundoImobiliario salvo = repository.save(fundoParaProcessar);

        // 3. Dispara o evento para o Kafka processar o novo Score de forma assíncrona
        this.enviarParaKafka(salvo);

        return salvo;
    }

    private void enviarParaKafka(FundoImobiliario fundo) {
        String mensagem = "Novo Fundo para análise: " + fundo.getTicker();
        kafkaTemplate.send("analise-fii", mensagem);
        System.out.println("[SRE LOG] Evento enviado ao Kafka para o Worker: " + fundo.getTicker());
    }
}