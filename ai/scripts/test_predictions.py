import sys
import os
from pathlib import Path

root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))

from ai.src.services.prediction_service import PredictionService


def main():
    print("="*80)
    print("🧪 TESTE DE PREVISÕES")
    print("="*80)
    
    # Inicializar serviço
    print("\n1️⃣ Carregando modelo...")
    service = PredictionService()
    
    # Mostrar info do modelo
    print("\n2️⃣ Informações do modelo:")
    info = service.get_model_info()
    print(f"   Modelo: {info['model_type']}")
    print(f"   Features: {info['num_features']}")
    print(f"   R²: {info['metadata'].get('r2_score', 'N/A'):.4f}")
    print(f"   MAE: {info['metadata'].get('mae', 'N/A'):.2f}")
    
    # Conectar ao banco
    print("\n3️⃣ Conectando ao banco de dados...")
    db_url = "postgresql://postgres:fatec@localhost:5433/teste"
    service.connect_database(db_url)
    
    # Gerar previsões para todos os itens
    print("\n4️⃣ Gerando previsões para todos os itens...")
    predictions = service.predict_all_items()
    
    # Mostrar resultados
    print(f"\n✅ {len(predictions)} previsões geradas!")
    print("\n" + "="*80)
    print("📊 PRIMEIRAS 5 PREVISÕES")
    print("="*80)
    
    for i, pred in enumerate(predictions[:5], 1):
        print(f"\n{i}. Item ID: {pred['item_id']}")
        print(f"   Quantidade prevista: {pred['predicted_quantity']:.2f}")
        print(f"   Estoque atual: {pred['current_stock']:.2f}")
        print(f"   Precisa repor? {'SIM' if pred['needs_restock'] else 'NÃO'}")
        if pred['needs_restock']:
            print(f"   Quantidade a repor: {pred['restock_quantity']:.2f}")
        print(f"   Previsão para: {pred['prediction_month']}/{pred['prediction_year']}")
        print(f"   Confiança: {pred['confidence_score']:.2%}")
    
    # Resumo estatístico
    print("\n" + "="*80)
    print("📈 RESUMO ESTATÍSTICO")
    print("="*80)
    
    total_items = len(predictions)
    items_need_restock = sum(1 for p in predictions if p['needs_restock'])
    avg_predicted = sum(p['predicted_quantity'] for p in predictions) / total_items
    total_restock_needed = sum(p['restock_quantity'] for p in predictions if p['needs_restock'])
    
    print(f"Total de itens: {total_items}")
    print(f"Itens que precisam reposição: {items_need_restock} ({items_need_restock/total_items*100:.1f}%)")
    print(f"Demanda média prevista: {avg_predicted:.2f}")
    print(f"Total de reposição necessária: {total_restock_needed:.2f}")
    
    print("\n✅ Teste concluído!")


if __name__ == "__main__":
    main()
