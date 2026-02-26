package com.lab.invest_ranker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class FundoImobiliario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ticker; 
    private Double cotacaoAtual;
    private Double dividendoUltimos12Meses;
    private Double pvp; 
    private String tipo; 
    
    // --- NOVOS ATRIBUTOS DE RISCO SRE ---
    private Integer qtdImoveis;
    private Double vacanciaMedia;
    private String grauRisco; // "Baixo", "Médio" ou "Alto"
    private Double score; 

    public FundoImobiliario() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public Double getCotacaoAtual() { return cotacaoAtual; }
    public void setCotacaoAtual(Double cotacaoAtual) { this.cotacaoAtual = cotacaoAtual; }
    public Double getDividendoUltimos12Meses() { return dividendoUltimos12Meses; }
    public void setDividendoUltimos12Meses(Double dividendoUltimos12Meses) { this.dividendoUltimos12Meses = dividendoUltimos12Meses; }
    public Double getPvp() { return pvp; }
    public void setPvp(Double pvp) { this.pvp = pvp; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Integer getQtdImoveis() { return qtdImoveis; }
    public void setQtdImoveis(Integer qtdImoveis) { this.qtdImoveis = qtdImoveis; }
    public Double getVacanciaMedia() { return vacanciaMedia; }
    public void setVacanciaMedia(Double vacanciaMedia) { this.vacanciaMedia = vacanciaMedia; }
    public String getGrauRisco() { return grauRisco; }
    public void setGrauRisco(String grauRisco) { this.grauRisco = grauRisco; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}