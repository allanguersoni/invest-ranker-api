package com.lab.invest_ranker.repository;

import com.lab.invest_ranker.model.FundoImobiliario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FundoImobiliarioRepository extends JpaRepository<FundoImobiliario, Long> {
    
    Optional<FundoImobiliario> findByTicker(String ticker);

    // Rota legada (Top 10)
    List<FundoImobiliario> findTop10ByOrderByScoreDesc();

    // NOVA INTELIGÊNCIA: Busca dinâmica no banco (Top N)
    @Query(value = "SELECT * FROM fundo_imobiliario WHERE score > 0 ORDER BY score DESC LIMIT :limite", nativeQuery = true)
    List<FundoImobiliario> encontrarTopDinamico(@Param("limite") int limite);
}