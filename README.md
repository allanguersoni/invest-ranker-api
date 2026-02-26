# 📈 Invest Ranker API - Ecossistema Quantitativo SRE

O **Invest Ranker API** é um Micro-SaaS de inteligência financeira focado em Value Investing e Renda Passiva. Utilizando uma arquitetura orientada a eventos (SRE), o sistema rastreia o mercado da B3 em tempo real, burla proteções anti-scraping e aplica rigorosos modelos matemáticos para encontrar assimetrias de preço (oportunidades onde o mercado está irracional).

## 💎 A Proposta de Valor (Por que usar?)
Diferente de home brokers ou calculadoras comuns que apenas exibem cotações, o Invest Ranker age como um **analista quantitativo automatizado**:
* **Fuga do Efeito Manada:** O algoritmo ignora notícias e emoções. Ele avalia balanços, lucros e patrimônio, alertando sobre "bolhas" (quando o mercado está eufórico e caro) e identificando "pechinchas" (quando empresas excelentes caem sem motivo).
* **Filtro Anti-Ciladas (Value Traps):** Impede que o investidor seja seduzido por dividendos altos ("Vacas Leiteiras" falsas) que escondem empresas endividadas ou fundos imobiliários vazios.
* **Economia de Tempo Absoluta:** Uma varredura completa da bolsa brasileira (mais de 400 ativos) leva menos de 5 segundos, entregando as informações mastigadas com o status de `COMPRA FORTE 🚀` ou `CARO 🔴`.

## 🛠️ Tecnologias e Arquitetura SRE
* **Backend:** Java 21 com Spring Boot 3 (Alta performance e tipagem forte).
* **Mensageria:** Apache Kafka (Desacoplamento e resiliência, garantindo que nenhum dado se perca em picos de volatilidade).
* **Banco de Dados:** PostgreSQL (Histórico imutável de séries temporais).
* **Coleta de Dados (Workers):** Python com Pandas e requests customizados (Bypass Cloudflare).
* **Observabilidade:** Grafana e Prometheus via Docker.

## 🧠 Motores de Inteligência e Risco

### 1. Motor de Ações (Fórmula de Benjamin Graham)
Encontra o "Valor Intrínseco" das empresas.
* **Margem de Segurança:** Calcula a distância entre o que a empresa vale em patrimônio/lucro e o preço negociado na tela. Apenas descontos superiores a 30% recebem grau máximo.
* **Trava de Eficiência:** Requer um ROE (Retorno sobre Patrimônio) saudável para garantir que a empresa é barata, mas não é "lixo".

### 2. Motor de Fundos Imobiliários (FIIs)
Focado em proteção de capital e renda passiva recorrente.
* **Penalidade de Vacância:** Zera o score de fundos com taxa de imóveis vazios superior a 15%.
* **Trava Monoativo:** Penaliza drasticamente fundos com apenas 1 ou 2 imóveis (risco de ruína).
* **Filtro P/VP:** Identifica distorções patrimoniais severas e ágios injustificados.

## 📊 Terminal Bloomberg Particular (Grafana)
O sistema conta com um dashboard institucional que exibe:
* **Termômetro da Bolsa:** Média de desconto do mercado (indicador de pânico ou euforia).
* **Radar de Sentimento:** Gráficos de dispersão de ativos caros vs. descontados.
* **Top Oportunidades & Vacas Leiteiras:** Tabelas dinâmicas atualizadas com a data e hora do último pregão.