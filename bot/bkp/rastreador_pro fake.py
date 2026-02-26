import requests
import time

# Lista simulada de dados coletados da raspagem
dados_mercado = [
    {"ticker": "MXRF11", "preco": 10.45, "dy_anual": 1.32, "pvp": 0.98, "tipo": "Papel"},
    {"ticker": "HGLG11", "preco": 165.50, "dy_anual": 13.20, "pvp": 1.05, "tipo": "Tijolo (Logística)"},
    {"ticker": "VISC11", "preco": 120.20, "dy_anual": 10.80, "pvp": 0.89, "tipo": "Tijolo (Shopping)"},
    {"ticker": "KNIP11", "preco": 95.10, "dy_anual": 11.40, "pvp": 1.01, "tipo": "Papel"},
]

def alimentar_inteligencia():
    print("🤖 Iniciando Agente de Alimentação de Dados...")
    for item in dados_mercado:
        payload = {
            "ticker": item["ticker"],
            "cotacaoAtual": item["preco"],
            "dividendoUltimos12Meses": item["dy_anual"],
            "pvp": item["pvp"],
            "tipo": item["tipo"]
        }
        try:
            r = requests.post("http://localhost:8080/api/fundos", json=payload)
            print(f"✅ {item['ticker']} enviado com sucesso!")
        except Exception as e:
            print(f"❌ Erro ao conectar na API Java: {e}")
        time.sleep(0.5)

if __name__ == "__main__":
    alimentar_inteligencia()