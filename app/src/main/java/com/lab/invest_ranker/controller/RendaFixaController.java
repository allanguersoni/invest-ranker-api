package com.lab.invest_ranker.controller;

import com.lab.invest_ranker.model.RendaFixa;
import com.lab.invest_ranker.repository.RendaFixaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendafixa")
public class RendaFixaController {

    private final RendaFixaRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RendaFixaController(RendaFixaRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> receberTitulo(@RequestBody RendaFixa titulo) {
        // 1. Salva o dado bruto no banco (PostgreSQL)
        RendaFixa salvo = repository.save(titulo);
        
        // 2. Avisa o Kafka: "Chegou Renda Fixa nova, ID tal, vai lá calcular o Imposto!"
        kafkaTemplate.send("analise-rendafixa", "ID: " + salvo.getId());
        
        return ResponseEntity.accepted().body("Título de Renda Fixa na esteira SRE");
    }

    @GetMapping("/top/{limite}")
    public List<RendaFixa> getTopDinamico(@PathVariable int limite) {
        return repository.encontrarTopDinamico(limite);
    }
}