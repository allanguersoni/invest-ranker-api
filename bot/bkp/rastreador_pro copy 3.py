import requests
import time

def capturar_mercado_real():
    print("📡 Abandonando APIs limitadas. Iniciando extração profunda via API Interna do Status Invest...")
    
    # Rota secreta do Status Invest que retorna todos os FIIs da bolsa de uma vez em JSON
    url = "https://statusinvest.com.br/category/advancedboardsearchresult?search=%7B%22Sector%22%3A%22%22%2C%22SubSector%22%3A%22%22%2C%22Segment%22%3A%22%22%2C%22my_range%22%3A%220%3B20%22%7D&CategoryType=2"
    
    # Disfarce para não ser bloqueado
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/121.0.0.0 Safari/537.36',
        'Accept': 'application/json'
    }

    try:
        res = requests.get(url, headers=headers)
        
        if res.status_code != 200:
            print(f"❌ Erro na extração: O servidor retornou {res.status_code}")
            return
            
        lista_fundos = res.json()
        fundos_processados = 0
        
        print(f"🔍 Sucesso! {len(lista_fundos)} fundos encontrados. Aplicando filtros SRE de qualidade...")
        
        for fundo in lista_fundos:
            ticker = fundo.get('ticker', '')
            preco = fundo.get('price', 0.0)
            dy = fundo.get('dy', 0.0)
            pvp = fundo.get('p_vp', 0.0)
            liquidez = fundo.get('liquidezmediadiaria', 0.0)
            segmento = fundo.get('segment', 'Outros')
            
            # 🛑 O FILTRO DE QUALIDADE (Agregando Valor)
            # 1. Preço > 0 (Fundo Ativo)
            # 2. P/VP > 0 (Filtra erros contábeis)
            # 3. Liquidez > 200k (Você consegue resgatar seu dinheiro amanhã se precisar)
            if preco > 0 and pvp > 0 and liquidez > 200000:
                payload = {
                    "ticker": ticker,
                    "cotacaoAtual": float(preco),
                    "dividendoUltimos12Meses": float(dy),
                    "pvp": float(pvp),
                    "tipo": str(segmento)
                }
                
                # Envia para a API Java (O Cérebro)
                try:
                    res_java = requests.post("http://localhost:8080/api/fundos", json=payload)
                    if res_java.status_code in [200, 201]:
                        print(f"✅ {ticker} classificado | P/VP: {pvp:.2f} | Risco Liq: OK")
                        fundos_processados += 1
                except requests.exceptions.ConnectionError:
                    print("❌ Erro: A API Java (Spring Boot) está desligada! Inicie o servidor primeiro.")
                    return
                
                time.sleep(0.1) # Respiro de rede para não travar o Kafka e o Banco
                
        print(f"🏁 Extração concluída! {fundos_processados} FIIs de alta qualidade foram para o Banco de Dados.")
        
    except Exception as e:
        print(f"❌ Erro fatal: {e}")

if __name__ == "__main__":
    capturar_mercado_real()