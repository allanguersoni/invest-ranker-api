import requests
import time

def capturar_dados_api():
    print("📡 [SRE] Abandonando scraping frágil. Iniciando ingestão via API REST Oficial...")
    
    # ⚠️ COLE O SEU TOKEN DA BRAPI AQUI
    TOKEN = "7GFiwqvhmSiJXgMsxSgf5m"
    
    # Lista dos maiores FIIs da bolsa para nossa base de conhecimento
    tickers = ["MXRF11", "HGLG11", "KNIP11", "XPLG11", "VISC11", "BTLG11", "IRDM11", "ALZR11", "TGAR11", "CPTS11"]
    
    fundos_processados = 0
    
    for ticker in tickers:
        # A Brapi nos entrega os fundamentos completos com o parâmetro fundamental=true
        url = f"https://brapi.dev/api/quote/{ticker}?fundamental=true&token={TOKEN}"
        
        try:
            res = requests.get(url)
            
            # Se o token estiver errado, ele avisa na hora
            if res.status_code == 401:
                print("❌ Erro: Autenticação negada. Você esqueceu de colocar o Token no script!")
                return
                
            if res.status_code != 200:
                print(f"⚠️ Falha ao buscar {ticker}. O fundo pode não existir na base.")
                continue
                
            # Extração limpa do JSON (sem se preocupar com HTML quebrado)
            dados = res.json().get('results', [{}])[0]
            
            preco = dados.get('regularMarketPrice', 0)
            
            # Algumas APIs chamam o P/VP de priceToBook e o Yield de dividendYield
            dy = dados.get('dividendYield', 0) 
            vpa = dados.get('priceToBook', 0) 
            pvp = vpa if vpa > 0 else 1.0
            
            # Se o preço for válido, mandamos para o nosso Java!
            if preco > 0:
                payload = {
                    "ticker": ticker,
                    "cotacaoAtual": float(preco),
                    "dividendoUltimos12Meses": float(dy),
                    "pvp": float(pvp),
                    "tipo": "Fundo Imobiliário" # Padronizado
                }
                
                # Injeta no seu App
                api_java = requests.post("http://localhost:8080/api/fundos", json=payload)
                
                if api_java.status_code in [200, 201]:
                    print(f"✅ {ticker} injetado no App | Preço: R${preco} | P/VP: {pvp}")
                    fundos_processados += 1
                    
            time.sleep(0.5) # Respeitando o Rate Limit da API
            
        except Exception as e:
            print(f"❌ Erro fatal ao processar o {ticker}: {e}")
            
    print(f"🏁 Sucesso! {fundos_processados} fundos processados e ranqueados pelo Worker.")

if __name__ == "__main__":
    capturar_dados_api()