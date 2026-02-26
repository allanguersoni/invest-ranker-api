package com.lab.invest_ranker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

@Entity
public class Acao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private Double cotacaoAtual;
    private Double pl; // Preço / Lucro (Quanto tempo para o lucro pagar a ação)
    private Double pvp; // Preço / Valor Patrimonial
    private Double roe; // Retorno sobre o Patrimônio (Eficiência da gestão)
    private Double dividendoUltimos12Meses;
    
    // --- INTELIGÊNCIA QUANTITATIVA (O SEGREDO DO APP) ---
    private Double precoJustoGraham; 
    private Double margemSeguranca; // Ex: 45% de desconto
    private String recomendacao; // "COMPRA FORTE", "BOA OPÇÃO", "CARO / AFASTAR"
    private Double score;

    // --- NOVO: CONTROLE DE TEMPO SRE ---
    private LocalDateTime dataAtualizacao;

    public Acao() {}

    // Este bloco garante que toda vez que o Java salvar ou atualizar essa ação no banco, a data/hora exata seja gravada automaticamente
    @PrePersist
    @PreUpdate
    public void atualizarData() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    
    public Double getCotacaoAtual() { return cotacaoAtual; }
    public void setCotacaoAtual(Double cotacaoAtual) { this.cotacaoAtual = cotacaoAtual; }
    
    public Double getPl() { return pl; }
    public void setPl(Double pl) { this.pl = pl; }
    
    public Double getPvp() { return pvp; }
    public void setPvp(Double pvp) { this.pvp = pvp; }
    
    public Double getRoe() { return roe; }
    public void setRoe(Double roe) { this.roe = roe; }
    
    public Double getDividendoUltimos12Meses() { return dividendoUltimos12Meses; }
    public void setDividendoUltimos12Meses(Double dividendoUltimos12Meses) { this.dividendoUltimos12Meses = dividendoUltimos12Meses; }
    
    public Double getPrecoJustoGraham() { return precoJustoGraham; }
    public void setPrecoJustoGraham(Double precoJustoGraham) { this.precoJustoGraham = precoJustoGraham; }
    
    public Double getMargemSeguranca() { return margemSeguranca; }
    public void setMargemSeguranca(Double margemSeguranca) { this.margemSeguranca = margemSeguranca; }
    
    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
    
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}