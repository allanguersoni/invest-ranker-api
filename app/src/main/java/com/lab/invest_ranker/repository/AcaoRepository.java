package com.lab.invest_ranker.repository;

import com.lab.invest_ranker.model.Acao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcaoRepository extends JpaRepository<Acao, Long> {
    
    Optional<Acao> findByTicker(String ticker);

    // NOVA INTELIGÊNCIA SRE: Busca dinâmica no banco (Top N)
    // Ordenamos por Score e também por Margem de Segurança como critério de desempate
    @Query(value = "SELECT * FROM acao WHERE score > 0 ORDER BY score DESC, margem_seguranca DESC LIMIT :limite", nativeQuery = true)
    List<Acao> encontrarTopDinamico(@Param("limite") int limite);
}