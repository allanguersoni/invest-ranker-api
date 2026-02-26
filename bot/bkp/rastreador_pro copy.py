import pandas as pd
import cloudscraper
import requests
import time

def capturar_mercado_real():
    url = "https://www.fundamentus.com.br/fii_resultado.php"
    
    print("🤖 Iniciando bypass de segurança (Cloudflare) e raspagem de dados...")
    
    # O scraper que simula um navegador Chrome real para enganar o anti-bot
    scraper = cloudscraper.create_scraper(browser={
        'browser': 'chrome',
        'platform': 'windows',
        'desktop': True
    })

    try:
        html = scraper.get(url).text
        
        if 'cdn-cgi/challenge-platform' in html:
            print("❌ O Cloudflare bloqueou a requisição. Tente rodar novamente em alguns minutos.")
            return

        # Puxa todas as tabelas da página (O Fundamentus usa padrão brasileiro de pontuação)
        tabelas = pd.read_html(html, decimal=',', thousands='.')
        df = tabelas[0]

        # --- LIMPEZA E INTELIGÊNCIA DE DADOS (O que agrega valor ao SaaS) ---
        df['Cotação'] = pd.to_numeric(df['Cotação'], errors='coerce')
        df['P/VP'] = pd.to_numeric(df['P/VP'], errors='coerce')
        df['Liquidez'] = pd.to_numeric(df['Liquidez'], errors='coerce')
        
        # Tira o símbolo de % do Yield e converte para float
        df['Dividend Yield'] = df['Dividend Yield'].astype(str).str.replace('%', '').str.replace(',', '.').astype(float)

        # A MÁGICA DO FILTRO: 
        # Só aceita fundos vivos (Cotação > 0), com negócio real (Liquidez > R$ 200k/dia) e P/VP válido
        df_limpo = df[(df['Cotação'] > 0) & (df['Liquidez'] > 200000) & (df['P/VP'] > 0)].copy()

        fundos_processados = 0
        print(f"🔍 Sucesso! Encontrados {len(df_limpo)} fundos saudáveis e líquidos. Iniciando injeção na API...")

        for index, row in df_limpo.iterrows():
            ticker = row['Papel']
            preco = row['Cotação']
            dy = row['Dividend Yield']
            pvp = row['P/VP']
            segmento = row['Segmento']

            # Tratamento caso o site não informe o segmento
            if pd.isna(segmento):
                segmento = "Indefinido"

            payload = {
                "ticker": str(ticker),
                "cotacaoAtual": float(preco),
                "dividendoUltimos12Meses": float(dy),
                "pvp": float(pvp),
                "tipo": str(segmento)
            }

            res = requests.post("http://localhost:8080/api/fundos", json=payload)
            
            if res.status_code in [200, 201]:
                print(f"✅ {ticker} injetado | P/VP: {pvp:.2f} | DY: {dy:.2f}% | Liquidez Diária: R$ {row['Liquidez']:,.2f}")
                fundos_processados += 1
            
            # Pausa cirúrgica de 200ms para não sobrecarregar o seu banco de dados e o Kafka
            time.sleep(0.2)

        print(f"🏁 Missão cumprida! {fundos_processados} fundos validados e processados pelo seu app.")

    except Exception as e:
        print(f"❌ Erro fatal na extração: {e}")

if __name__ == "__main__":
    capturar_mercado_real()