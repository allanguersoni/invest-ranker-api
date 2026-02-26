package com.lab.invest_ranker.controller;

import com.lab.invest_ranker.model.Acao;
import com.lab.invest_ranker.repository.AcaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acoes")
public class AcaoController {

    private final AcaoRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public AcaoController(AcaoRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // A Porta de Entrada (Onde o robô Python injeta os dados)
    @PostMapping
    public ResponseEntity<String> receberAcao(@RequestBody Acao acao) {
        repository.save(acao);
        kafkaTemplate.send("analise-acao", "TICKER: " + acao.getTicker());
        return ResponseEntity.accepted().body("Ação na fila de processamento SRE");
    }

    // A Porta de Saída Estática (Caso algum painel ainda use o /top/10 fixo)
    @GetMapping("/top/10")
    public List<Acao> getTop10() {
        return repository.encontrarTopDinamico(10);
    }

    // NOVA PORTA DE SAÍDA DINÂMICA (Ex: /api/acoes/top/20 ou /top/5)
    @GetMapping("/top/{limite}")
    public List<Acao> getTopDinamico(@PathVariable int limite) {
        return repository.encontrarTopDinamico(limite);
    }
}