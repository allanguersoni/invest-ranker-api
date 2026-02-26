import pandas as pd
import urllib.request
import requests
import time
import io

def capturar_mercado_real():
    print("📡 [SRE] Iniciando motor de extração profunda (Bypass Cloudflare + ISO-8859-1)...")
    url = "https://www.fundamentus.com.br/fii_resultado.php"
    
    # Cabeçalho para simular um navegador real perfeitamente
    req = urllib.request.Request(
        url,
        headers={
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
        }
    )
    
    try:
        # 1. Puxa os bytes brutos da página
        html_bytes = urllib.request.urlopen(req).read()
        
        # 2. DECODIFICAÇÃO CIRÚRGICA: Traduz do formato antigo do site para texto limpo
        html_str = html_bytes.decode('ISO-8859-1')
        
        # 3. PARSING: O Pandas lê o texto como se fosse um arquivo, identificando pontuação BR
        tabelas = pd.read_html(io.StringIO(html_str), decimal=',', thousands='.')
        df = tabelas[0]
        
        print(f"✅ Download concluído! Analisando {len(df)} fundos listados na bolsa...")

        # --- LIMPEZA DE DADOS (Sanitização) ---
        df['Cotação'] = pd.to_numeric(df['Cotação'], errors='coerce')
        df['P/VP'] = pd.to_numeric(df['P/VP'], errors='coerce')
        df['Liquidez'] = pd.to_numeric(df['Liquidez'], errors='coerce')
        
        # O Yield vem como string (ex: "12,50%"). Limpamos e convertemos para float.
        df['Dividend Yield'] = df['Dividend Yield'].astype(str).str.replace('%', '').str.replace(',', '.').astype(float)
        
        # --- FILTRO SRE (O que agrega valor real ao seu App) ---
        # Regras de Negócio:
        # 1. Fundo precisa estar ativo (Cotação > 0)
        # 2. Precisa ter preço justo mapeado (P/VP > 0)
        # 3. Precisa ser seguro para sair (Liquidez > R$ 200.000/dia)
        df_limpo = df[(df['Cotação'] > 0) & (df['Liquidez'] > 200000) & (df['P/VP'] > 0)].copy()
        
        fundos_processados = 0
        print(f"🔍 Qualidade garantida: {len(df_limpo)} FIIs passaram no filtro de segurança. Injetando no App...")
        
        for index, row in df_limpo.iterrows():
            ticker = str(row['Papel'])
            preco = float(row['Cotação'])
            dy = float(row['Dividend Yield'])
            pvp = float(row['P/VP'])
            
            # Validação cruzada: Se não tiver segmento, classificamos como 'Outros'
            segmento = str(row['Segmento']) if pd.notna(row['Segmento']) and row['Segmento'] != '' else 'Outros'
            
            payload = {
                "ticker": ticker,
                "cotacaoAtual": preco,
                "dividendoUltimos12Meses": dy,
                "pvp": pvp,
                "tipo": segmento
            }
            
            # Envia para o "Cérebro" Java processar o Score
            try:
                res_java = requests.post("http://localhost:8080/api/fundos", json=payload)
                if res_java.status_code in [200, 201, 202]:
                    print(f"🚀 Injetado: {ticker} | R$ {preco:.2f} | P/VP: {pvp:.2f} | DY: {dy:.2f}%")
                    fundos_processados += 1
            except requests.exceptions.ConnectionError:
                print("\n❌ ERRO: A API Java não está rodando! Ligue o Spring Boot primeiro.")
                return
            
            time.sleep(0.1) # Pausa para o Kafka respirar
            
        print(f"\n🏁 SUCESSO ABSOLUTO! {fundos_processados} ativos processados.")
        print("📊 Verifique seu Grafana: Os dashboards devem estar disparando agora!")
        
    except Exception as e:
        print(f"❌ Erro fatal na extração: {e}")

if __name__ == "__main__":
    capturar_mercado_real()