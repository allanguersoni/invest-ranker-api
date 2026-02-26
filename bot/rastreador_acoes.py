import pandas as pd
import urllib.request
import requests
import time
import io

def capturar_acoes_b3():
    print("📡 [SRE] Iniciando Motor de Ações (Value Investing / Bypass Cloudflare)...")
    url = "https://www.fundamentus.com.br/resultado.php"
    
    # Cabeçalho para simular navegador
    req = urllib.request.Request(
        url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
    )
    
    try:
        html_bytes = urllib.request.urlopen(req).read()
        html_str = html_bytes.decode('ISO-8859-1')
        
        # O Pandas lê a tabela base
        df = pd.read_html(io.StringIO(html_str), decimal=',', thousands='.')[0]

        # 1. Limpeza Segura de valores monetários e numéricos simples
        df['Cotação'] = pd.to_numeric(df['Cotação'], errors='coerce')
        df['P/L'] = pd.to_numeric(df['P/L'], errors='coerce')
        df['P/VP'] = pd.to_numeric(df['P/VP'], errors='coerce')
        df['Liq.2meses'] = pd.to_numeric(df['Liq.2meses'], errors='coerce')
        
        # 2. A CORREÇÃO CIRÚRGICA: Tratamento de porcentagens extremas (ex: "1.202,89%")
        # Passo A: Remove o '%'. Passo B: Remove o ponto de milhar. Passo C: Troca a vírgula por ponto decimal.
        df['Div.Yield'] = df['Div.Yield'].astype(str).str.replace('%', '', regex=False).str.replace('.', '', regex=False).str.replace(',', '.', regex=False)
        df['ROE'] = df['ROE'].astype(str).str.replace('%', '', regex=False).str.replace('.', '', regex=False).str.replace(',', '.', regex=False)
        
        # 3. Conversão final para float (se algum texto bizarro passar, vira NaN e depois 0.0)
        df['Div.Yield'] = pd.to_numeric(df['Div.Yield'], errors='coerce').fillna(0.0)
        df['ROE'] = pd.to_numeric(df['ROE'], errors='coerce').fillna(0.0)

        # 4. Filtro SRE: Ações com Liquidez (> R$ 1 Milhão/dia) e preço válido
        df_limpo = df[(df['Cotação'] > 0) & (df['Liq.2meses'] > 1000000) & (df['P/VP'] > 0)].copy()
        
        print(f"🔍 Qualidade garantida: {len(df_limpo)} Ações passaram no filtro. Injetando na API...")

        for index, row in df_limpo.iterrows():
            ticker = str(row['Papel'])
            
            payload = {
                "ticker": ticker,
                "cotacaoAtual": float(row['Cotação']),
                "pl": float(row['P/L']),
                "pvp": float(row['P/VP']),
                "roe": float(row['ROE']),
                "dividendoUltimos12Meses": float(row['Div.Yield'])
            }
            
            try:
                res = requests.post("http://localhost:8080/api/acoes", json=payload)
                if res.status_code in [200, 201, 202]:
                    print(f"✅ Injetado: {ticker} | P/L: {float(row['P/L'])} | ROE: {float(row['ROE'])}%")
            except requests.exceptions.ConnectionError:
                print("\n❌ ERRO: A API Java não está rodando na porta 8080!")
                return
            
            time.sleep(0.1) 
            
        print("\n🏁 SUCESSO! Varredura de Ações finalizada.")
            
    except Exception as e:
        print(f"❌ Erro fatal na extração: {e}")

if __name__ == "__main__":
    capturar_acoes_b3()