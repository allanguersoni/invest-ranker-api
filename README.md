# 📈Invest Ranker API - Motor Quantitativo SRE

Este projeto é um Micro-SaaS de inteligência financeira que utiliza uma arquitetura moderna para realizar análise de risco e ranqueamento de Fundos Imobiliários (FIIs) em tempo real.

## 🛠️ Tecnologias Utilizadas
* **Backend:** Java 21 com Spring Boot 3
* **Mensageria:** Apache Kafka para processamento assíncrono
* **Banco de Dados:** PostgreSQL
* **Coleta de Dados:** Python com motor de extração profunda (Bypass Cloudflare)
* **Monitoramento:** Grafana e Prometheus via Docker

## 🧠 Inteligência de Risco (Algoritmo SRE)
Diferente de calculadoras comuns, este motor aplica penalidades institucionais:
* **Penalidade de Vacância:** Reduz o score se a vacância média for superior a 15%.
* **Trava de Ativos:** Penaliza fundos monoativos para proteção contra vacância total.
* **Filtro de Value Trap:** Identifica e penaliza yields irreais ou P/VP distorcidos que escondem riscos de crédito.

## 📊 Dashboard Visual
Inclui integração nativa com Grafana para visualização do **Top 10 Oportunidades** e métricas de saúde do mercado.
