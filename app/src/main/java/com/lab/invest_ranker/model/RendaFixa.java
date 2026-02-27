package com.lab.invest_ranker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class RendaFixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "CDB Banco Master", "Tesouro IPCA+ 2029", "LCA Banco do Brasil"
    private String nomeTitulo; 
    
    // Ex: "CDB", "LCI", "LCA", "TESOURO", "CRI", "CRA", "DEBENTURE"
    private String tipoAtivo; 
    
    private String emissor; 

    // --- REGRAS DE RENTABILIDADE (COMO O ATIVO PAGA) ---
    private String indexador; // "CDI", "IPCA", "PREFIXADO"
    private Double taxaRendimento; // Ex: 115.0 (para 115% CDI) ou 6.5 (para IPCA + 6,5%)
    
    // --- TEMPO E LIQUIDEZ ---
    private LocalDate dataVencimento;
    private Boolean liquidezDiaria; // true se o usuário puder sacar qualquer dia (D+0)

    // --- REGRAS DE RISCO SRE ---
    private Boolean isentoIR; // true para LCI/LCA/CRI/CRA
    private Boolean temFGC; // true para CDB/LCI/LCA

    // --- INTELIGÊNCIA QUANTITATIVA (O SEGREDO DO APP) ---
    private Double rentabilidadeLiquidaAnual; // Já descontado o Imposto de Renda
    private String recomendacao; // "OPORTUNIDADE DE OURO", "BOM PARA CAIXA", "RISCO ALTO", "CILADA TRIBUTÁRIA"
    private Double score;

    // --- CONTROLE DE TEMPO SRE ---
    private LocalDateTime dataAtualizacao;

    public RendaFixa() {}

    @PrePersist
    @PreUpdate
    public void atualizarData() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeTitulo() { return nomeTitulo; }
    public void setNomeTitulo(String nomeTitulo) { this.nomeTitulo = nomeTitulo; }

    public String getTipoAtivo() { return tipoAtivo; }
    public void setTipoAtivo(String tipoAtivo) { this.tipoAtivo = tipoAtivo; }

    public String getEmissor() { return emissor; }
    public void setEmissor(String emissor) { this.emissor = emissor; }

    public String getIndexador() { return indexador; }
    public void setIndexador(String indexador) { this.indexador = indexador; }

    public Double getTaxaRendimento() { return taxaRendimento; }
    public void setTaxaRendimento(Double taxaRendimento) { this.taxaRendimento = taxaRendimento; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public Boolean getLiquidezDiaria() { return liquidezDiaria; }
    public void setLiquidezDiaria(Boolean liquidezDiaria) { this.liquidezDiaria = liquidezDiaria; }

    public Boolean getIsentoIR() { return isentoIR; }
    public void setIsentoIR(Boolean isentoIR) { this.isentoIR = isentoIR; }

    public Boolean getTemFGC() { return temFGC; }
    public void setTemFGC(Boolean temFGC) { this.temFGC = temFGC; }

    public Double getRentabilidadeLiquidaAnual() { return rentabilidadeLiquidaAnual; }
    public void setRentabilidadeLiquidaAnual(Double rentabilidadeLiquidaAnual) { this.rentabilidadeLiquidaAnual = rentabilidadeLiquidaAnual; }

    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}