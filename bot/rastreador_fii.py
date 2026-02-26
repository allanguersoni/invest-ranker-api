import pandas as pd
import urllib.request
import requests
import time
import io

def capturar_mercado_real():
    print("📡 [SRE] Iniciando motor com Análise de Risco (Bypass Cloudflare)...")
    url = "https://www.fundamentus.com.br/fii_resultado.php"
    
    req = urllib.request.Request(
        url, headers={'User-Agent': 'Mozilla/5.0'}
    )
    
    try:
        html_bytes = urllib.request.urlopen(req).read()
        html_str = html_bytes.decode('ISO-8859-1')
        df = pd.read_html(io.StringIO(html_str), decimal=',', thousands='.')[0]

        df['Cotação'] = pd.to_numeric(df['Cotação'], errors='coerce')
        df['P/VP'] = pd.to_numeric(df['P/VP'], errors='coerce')
        df['Liquidez'] = pd.to_numeric(df['Liquidez'], errors='coerce')
        df['Dividend Yield'] = df['Dividend Yield'].astype(str).str.replace('%', '').str.replace(',', '.').astype(float)
        
        # NOVAS COLUNAS DE RISCO
        df['Qtd de imóveis'] = pd.to_numeric(df['Qtd de imóveis'], errors='coerce').fillna(0)
        df['Vacância Média'] = df['Vacância Média'].astype(str).str.replace('%', '').str.replace(',', '.').astype(float).fillna(0.0)

        df_limpo = df[(df['Cotação'] > 0) & (df['Liquidez'] > 200000) & (df['P/VP'] > 0)].copy()
        
        for index, row in df_limpo.iterrows():
            payload = {
                "ticker": str(row['Papel']),
                "cotacaoAtual": float(row['Cotação']),
                "dividendoUltimos12Meses": float(row['Dividend Yield']),
                "pvp": float(row['P/VP']),
                "tipo": str(row['Segmento']) if pd.notna(row['Segmento']) and row['Segmento'] != '' else 'Outros',
                "qtdImoveis": int(row['Qtd de imóveis']),
                "vacanciaMedia": float(row['Vacância Média'])
            }
            
            try:
                requests.post("http://localhost:8080/api/fundos", json=payload)
                print(f"✅ Injetado: {row['Papel']} | Imóveis: {int(row['Qtd de imóveis'])} | Vacância: {float(row['Vacância Média'])}%")
            except Exception:
                pass
            time.sleep(0.1) 
            
    except Exception as e:
        print(f"❌ Erro fatal: {e}")

if __name__ == "__main__":
    capturar_mercado_real()