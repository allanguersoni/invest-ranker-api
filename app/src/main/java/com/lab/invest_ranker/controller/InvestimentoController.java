package com.lab.invest_ranker.controller;

import com.lab.invest_ranker.model.FundoImobiliario;
import com.lab.invest_ranker.repository.FundoImobiliarioRepository;
import com.lab.invest_ranker.service.InvestimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fundos")
public class InvestimentoController {

    private final InvestimentoService service;
    private final FundoImobiliarioRepository repository;

    public InvestimentoController(InvestimentoService service, FundoImobiliarioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<String> receberFundo(@RequestBody FundoImobiliario fundo) {
        service.receberNovoFundo(fundo);
        return ResponseEntity.accepted().body("Fundo " + fundo.getTicker() + " recebido e na fila do Kafka para análise!");
    }

    // Rota Fixa: /api/fundos/top10
    @GetMapping("/top10")
    public ResponseEntity<List<FundoImobiliario>> obterTop10Pechinchas() {
        List<FundoImobiliario> top10 = repository.findTop10ByOrderByScoreDesc();
        return ResponseEntity.ok(top10);
    }

    // ROTA DINÂMICA (SRE PRO): /api/fundos/top/{quantidade}
    // Exemplo de uso: /api/fundos/top/20 ou /api/fundos/top/50
    @GetMapping("/top/{limite}")
    public ResponseEntity<List<FundoImobiliario>> obterTopDinamico(@PathVariable int limite) {
        // Validação de segurança para não travar o banco se o usuário pedir 1 milhão de fundos
        int limiteSeguro = Math.min(limite, 100); 
        
        List<FundoImobiliario> topN = repository.encontrarTopDinamico(limiteSeguro);
        return ResponseEntity.ok(topN);
    }
}