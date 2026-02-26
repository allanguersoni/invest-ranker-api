package com.lab.invest_ranker.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/simulador")
public class SimuladorController {

    @GetMapping
    public Map<String, Object> sugerirAlocacao(@RequestParam Double capital) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        
        resposta.put("Capital Disponível", "R$ " + capital);
        
        // Estratégia SRE de Diversificação
        resposta.put("1. Reserva de Oportunidade (15%)", "R$ " + (capital * 0.15));
        resposta.put("2. Fundos de Tijolo (50% - Segurança)", "R$ " + (capital * 0.50));
        resposta.put("3. Fundos de Papel (35% - Alta Renda)", "R$ " + (capital * 0.35));
        
        resposta.put("Dica do Especialista", "Com este valor, recomendamos comprar cotas de pelo menos 8 fundos diferentes para diluir o risco de vacância.");
        
        return resposta;
    }
}