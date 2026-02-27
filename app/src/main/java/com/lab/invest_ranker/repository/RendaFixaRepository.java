package com.lab.invest_ranker.repository;

import com.lab.invest_ranker.model.RendaFixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RendaFixaRepository extends JpaRepository<RendaFixa, Long> {
    
    // Já deixamos a query dinâmica pronta para o Grafana usar depois!
    @Query(value = "SELECT * FROM renda_fixa ORDER BY score DESC LIMIT :limite", nativeQuery = true)
    List<RendaFixa> encontrarTopDinamico(@Param("limite") int limite);
}