from seleniumbase import SB
import json
import time

def capturar_tesouro_api_sb():
    print("📡 [SRE] Acionando Motor de Vanguarda: SeleniumBase (Bypass Turnstile)...")
    
    titulos_processados = []
    
    # uc=True: Ativa a camuflagem pesada anti-bot
    # headless=True: Roda no escuro (Ideal para seu servidor Linux/WSL)
    with SB(uc=True, headless=True) as sb:
        url = "https://www.tesourodireto.com.br/o/rentabilidade/investir"
        print("⏳ Navegador invisível acessando a B3... Resolvendo cálculos do WAF...")
        
        # O uc_open_with_reconnect foi criado especificamente para enganar o Cloudflare
        sb.uc_open_with_reconnect(url, 4)
        
        # O "Golpe de Mestre": Se o Cloudflare desenhar a caixinha "Verifique se é humano", o robô clica nela!
        try:
            sb.uc_gui_click_captcha()
        except:
            pass # Se não aparecer, seguimos em frente
            
        time.sleep(3) # Aguarda o JSON ser renderizado na tela do navegador
        
        try:
            # Puxamos todo o texto que está sendo exibido na página
            conteudo = sb.get_text("body")
            
            # Fallback: Se o Chrome envelopar o JSON numa tag de formatação
            if "TesouroLegado" not in conteudo:
                conteudo = sb.get_text("pre")
                
            print("🔓 Bypass concluído! Mapeando a API JSON...")
            dados_json = json.loads(conteudo)
            
            titulos_legado = dados_json.get("TesouroLegado", [])
            titulos_24x7 = dados_json.get("Tesouro24x7", [])
            todos_titulos = titulos_legado + titulos_24x7
            
            for titulo in todos_titulos:
                nome = titulo.get("treasuryBondName", "")
                taxa_str = titulo.get("investmentProfitabilityIndexerName", "")
                vencimento_str = titulo.get("maturityDate", "") 
                
                if not nome or not taxa_str or not vencimento_str:
                    continue
                    
                try:
                    if "+" in taxa_str:
                        valor_str = taxa_str.split("+")[1]
                    else:
                        valor_str = taxa_str
                        
                    taxa_limpa = float(valor_str.replace("%", "").strip().replace(",", "."))
                    data_vencimento = vencimento_str.split("T")[0]
                    
                    indexador = "PREFIXADO"
                    if "IPCA" in nome.upper(): indexador = "IPCA"
                    elif "SELIC" in nome.upper(): indexador = "CDI"
                    
                    payload = {
                        "nomeTitulo": nome,
                        "tipoAtivo": "TESOURO",
                        "emissor": "Governo Federal",
                        "indexador": indexador,
                        "taxaRendimento": taxa_limpa,
                        "dataVencimento": data_vencimento,
                        "liquidezDiaria": True,
                        "isentoIR": False,
                        "temFGC": False
                    }
                    titulos_processados.append(payload)
                except Exception:
                    continue
                    
            print(f"✅ Sucesso Absoluto: {len(titulos_processados)} Títulos capturados direto da fonte!")
            return titulos_processados
            
        except Exception as e:
            print(f"❌ O WAF bloqueou a leitura do JSON ou a página não carregou. Detalhe: {e}")
            return []

def simular_captura_credito_privado():
    print("📡 [SRE] Simulando captura de Crédito Privado (Bancos/Corretoras)...")
    return [
        {"nomeTitulo": "CDB Banco Master", "tipoAtivo": "CDB", "emissor": "Banco Master", "indexador": "CDI", "taxaRendimento": 118.0, "dataVencimento": "2027-12-01", "liquidezDiaria": False, "isentoIR": False, "temFGC": True},
        {"nomeTitulo": "LCA Banco do Brasil", "tipoAtivo": "LCA", "emissor": "Banco do Brasil", "indexador": "CDI", "taxaRendimento": 94.0, "dataVencimento": "2025-10-15", "liquidezDiaria": False, "isentoIR": True, "temFGC": True},
        {"nomeTitulo": "CRA JBS S.A.", "tipoAtivo": "CRA", "emissor": "JBS", "indexador": "IPCA", "taxaRendimento": 8.5, "dataVencimento": "2032-05-20", "liquidezDiaria": False, "isentoIR": True, "temFGC": False}
    ]

def enviar_para_api(payloads):
    import requests
    url_api = "http://localhost:8080/api/rendafixa"
    for payload in payloads:
        try:
            res = requests.post(url_api, json=payload)
            if res.status_code in [200, 201, 202]:
                print(f"🚀 Injetado no Java: {payload['nomeTitulo']} | Taxa Bruta: {payload['taxaRendimento']}")
        except requests.exceptions.ConnectionError:
            print("❌ ERRO: A API Java local não respondeu.")
            return
        time.sleep(0.1)

if __name__ == "__main__":
    print("🤖 Iniciando Motor de Renda Fixa SRE (Bypass Turnstile)...")
    titulos_tesouro = capturar_tesouro_api_sb()
    enviar_para_api(titulos_tesouro)
    
    titulos_privados = simular_captura_credito_privado()
    enviar_para_api(titulos_privados)
    print("\n🏁 Varredura finalizada!")