from curl_cffi import requests

def testar_bypass_rede():
    print("🕵️‍♂️ [SRE Wget Hacker] Disparando pacote com assinatura do Chrome 120...")
    url = "https://www.tesourodireto.com.br/o/rentabilidade/investir"
    
    try:
        # O 'impersonate' clona a criptografia TLS exata de um Chrome real
        response = requests.get(url, impersonate="chrome120")
        
        print(f"📡 Status da Resposta: {response.status_code}")
        
        if response.status_code == 200:
            dados = response.json()
            qtd_titulos = len(dados.get("TesouroLegado", [])) + len(dados.get("Tesouro24x7", []))
            print(f"✅ SUCESSO ABSOLUTO! O WAF foi enganado.")
            print(f"💰 Foram capturados {qtd_titulos} títulos reais em milissegundos.")
            return True
        else:
            print(f"❌ O WAF bloqueou (Erro {response.status_code}).")
            print("🔒 Motivo: O Cloudflare está exigindo o cálculo de JavaScript (cf_clearance).")
            return False
            
    except Exception as e:
        print(f"❌ Erro na requisição: {e}")
        return False

if __name__ == "__main__":
    testar_bypass_rede()