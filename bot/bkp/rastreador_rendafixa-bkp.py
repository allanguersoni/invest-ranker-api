import requests
import json
from datetime import datetime
import time

def capturar_tesouro_direto():
    print("📡 [SRE] Conectando à API Oculta do Tesouro Nacional (Bypass WAF)...")
    url = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json"
    
    # Camuflagem nível Hard: Parecendo um Chrome real
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36',
        'Accept': 'application/json, text/plain, */*',
        'Referer': 'https://www.tesourodireto.com.br/titulos/precos-e-taxas.htm'
    }
    
    try:
        response = requests.get(url, headers=headers)
        
        # Proteção SRE: Verifica se fomos bloqueados (200 é OK)
        if response.status_code != 200:
            print(f"⚠️ Alerta: O servidor do Tesouro bloqueou a requisição (Status {response.status_code}).")
            return []

        dados = response.json()
        titulos_processados = []
        lista_titulos = dados.get('response', {}).get('TrsrBdTradgList', [])
        
        for item in lista_titulos:
            titulo = item.get('TrsrBd', {})
            nome = titulo.get('nm', '')
            taxa = titulo.get('anulInvstmtRate', 0.0)
            vencimento_str = titulo.get('mtrtyDt', '') 
            
            if taxa > 0 and vencimento_str:
                vencimento_obj = datetime.strptime(vencimento_str.split('T')[0], '%Y-%m-%d')
                data_vencimento = vencimento_obj.strftime('%Y-%m-%d')
                
                indexador = "PREFIXADO"
                if "IPCA" in nome.upper(): indexador = "IPCA"
                elif "SELIC" in nome.upper(): indexador = "CDI"
                
                payload = {
                    "nomeTitulo": nome,
                    "tipoAtivo": "TESOURO",
                    "emissor": "Governo Federal",
                    "indexador": indexador,
                    "taxaRendimento": float(taxa),
                    "dataVencimento": data_vencimento,
                    "liquidezDiaria": True,
                    "isentoIR": False,
                    "temFGC": False 
                }
                titulos_processados.append(payload)
                
        print(f"✅ Sucesso: {len(titulos_processados)} Títulos do Tesouro Direto mapeados.")
        return titulos_processados
        
    except Exception as e:
        print(f"❌ Erro ao capturar Tesouro Direto: Servidor pode estar instável. Detalhe: {e}")
        return []

def simular_captura_credito_privado():
    print("📡 [SRE] Simulando captura de Crédito Privado (Bancos/Corretoras)...")
    return [
        {"nomeTitulo": "CDB Banco Master", "tipoAtivo": "CDB", "emissor": "Banco Master", "indexador": "CDI", "taxaRendimento": 118.0, "dataVencimento": "2027-12-01", "liquidezDiaria": False, "isentoIR": False, "temFGC": True},
        {"nomeTitulo": "LCA Banco do Brasil", "tipoAtivo": "LCA", "emissor": "Banco do Brasil", "indexador": "CDI", "taxaRendimento": 94.0, "dataVencimento": "2025-10-15", "liquidezDiaria": False, "isentoIR": True, "temFGC": True},
        {"nomeTitulo": "CRA JBS S.A.", "tipoAtivo": "CRA", "emissor": "JBS", "indexador": "IPCA", "taxaRendimento": 8.5, "dataVencimento": "2032-05-20", "liquidezDiaria": False, "isentoIR": True, "temFGC": False}
    ]

def enviar_para_api(payloads):
    url_api = "http://localhost:8080/api/rendafixa"
    for payload in payloads:
        try:
            res = requests.post(url_api, json=payload)
            if res.status_code in [200, 201, 202]:
                print(f"🚀 Injetado: {payload['nomeTitulo']} | Taxa Bruta: {payload['taxaRendimento']}")
        except requests.exceptions.ConnectionError:
            print(f"❌ ERRO: A API Java não está rodando na rota /api/rendafixa ainda.")
            return
        time.sleep(0.1)

if __name__ == "__main__":
    print("🤖 Iniciando Motor de Renda Fixa SRE...")
    titulos_tesouro = capturar_tesouro_direto()
    enviar_para_api(titulos_tesouro)
    
    titulos_privados = simular_captura_credito_privado()
    enviar_para_api(titulos_privados)
    print("\n🏁 Varredura de Renda Fixa finalizada!")