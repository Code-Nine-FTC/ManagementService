import random

item_type_names = [f"Tipo Militar {i}" for i in range(1, 51)] + [f"Tipo Farmácia {i}" for i in range(1, 51)]

almoxarifado_items = [
    "Calça Camuflada", "Radio HT Motorola", "Fuzil IA2", "Cartucho 5.56mm",
    "Chave Inglesa", "Barraca Militar", "Prancheta de Ordem", "Capacete Balístico",
    "Cinto Tático", "Bota Militar", "Lanterna Tática", "Mochila Militar"
]

farmacia_items = [
    "Kit Primeiros Socorros", "Termômetro Digital", "Álcool Gel", "Luvas Cirúrgicas",
    "Máscara Descartável", "Soro Fisiológico", "Esparadrapo", "Medicamento Analgésico",
    "Gaze Estéril", "Seringa", "Antisséptico", "Bandagem"
]

cores = ["Verde", "Preto", "Cinza", "Azul"]
tamanhos = ["P", "M", "G", "GG"]

supplier_names = [
    "Indústria Militar Brasileira", "Fábrica de Munições Caçapava", "Hospital Militar Regional", "Oficina de Manutenção Militar"
]

section_names = ["Almoxarifado", "Farmácia"]

user_names = [
    "Capitão Silva", "Sargento Souza", "Tenente Lima", "Soldado Pereira", "Major Costa",
    "Coronel Ramos", "Sargento Oliveira", "Soldado Santos", "Tenente Braga", "Capitão Almeida"
]

NUM_ITEM_TYPES = len(item_type_names)
NUM_SUPPLIERS = len(supplier_names)
NUM_USERS = len(user_names)
NUM_SECTIONS = len(section_names)
NUM_ALMOXARIFADO = 7000
NUM_FARMACIA = 3000

with open("data.sql", "w", encoding="utf-8") as f:
    # ItemType
    for i, name in enumerate(item_type_names, 1):
        f.write(f"INSERT INTO item_type (id, name) VALUES ({i}, '{name}');\n")
    f.write("COMMIT;\n")

    # SupplierCompany
    for i, name in enumerate(supplier_names, 1):
        f.write(f"INSERT INTO supplier_company (id, name) VALUES ({i}, '{name}');\n")
    f.write("COMMIT;\n")

    # Section
    for i, name in enumerate(section_names, 1):
        f.write(f"INSERT INTO section (id, title) VALUES ({i}, '{name}');\n")
    f.write("COMMIT;\n")

    # User
    for i, name in enumerate(user_names, 1):
        email = f"{name.lower().replace(' ','.')}@exercito.mil.br"
        if "Capitão" in name:
            role = "ADMIN"
        elif "Tenente" in name or "Major" in name:
            role = "MANAGER"
        else:
            role = "ASSISTANT"
        f.write(f"INSERT INTO user (id, name, email, password, role) VALUES ({i}, '{name}', '{email}', '{{noop}}senha{i}', '{role}');\n")
    f.write("COMMIT;\n")

    # Itens Almoxarifado
    for i in range(1, NUM_ALMOXARIFADO + 1):
        item_type_id = (i % 50) + 1  # Tipos militares
        supplier_id = (i % NUM_SUPPLIERS) + 1
        user_id = (i % NUM_USERS) + 1
        section_id = 1
        name_base = random.choice(almoxarifado_items)
        cor = random.choice(cores)
        tamanho = random.choice(tamanhos)
        nome = f"{name_base} {cor} {tamanho} {i}"
        min_stock = random.randint(1, 20)
        max_stock = random.randint(min_stock + 10, min_stock + 200)
        current_stock = random.randint(min_stock, max_stock)
        f.write(
            f"INSERT INTO item (id, name, measure, expire_date, current_stock, minimum_stock, maximum_stock, qr_code, is_active, last_user_id, supplier_company_id, section_id, item_type_id) "
            f"VALUES ({i}, '{nome}', 'unidade', '2026-12-31', {current_stock}, {min_stock}, {max_stock}, 'QR{i:05d}', true, {user_id}, {supplier_id}, {section_id}, {item_type_id});\n"
        )
        if i % 1000 == 0:
            f.write("COMMIT;\n")

    # Itens Farmácia
    for i in range(NUM_ALMOXARIFADO + 1, NUM_ALMOXARIFADO + NUM_FARMACIA + 1):
        item_type_id = ((i - NUM_ALMOXARIFADO - 1) % 50) + 51  # Tipos farmácia
        supplier_id = (i % NUM_SUPPLIERS) + 1
        user_id = (i % NUM_USERS) + 1
        section_id = 2
        name_base = random.choice(farmacia_items)
        lote = f"Lote {i:04d}"
        nome = f"{name_base} {lote}"
        min_stock = random.randint(1, 20)
        max_stock = random.randint(min_stock + 10, min_stock + 200)
        current_stock = random.randint(min_stock, max_stock)
        f.write(
            f"INSERT INTO item (id, name, measure, expire_date, current_stock, minimum_stock, maximum_stock, qr_code, is_active, last_user_id, supplier_company_id, section_id, item_type_id) "
            f"VALUES ({i}, '{nome}', 'unidade', '2026-12-31', {current_stock}, {min_stock}, {max_stock}, 'QR{i:05d}', true, {user_id}, {supplier_id}, {section_id}, {item_type_id});\n"
        )
        if (i - NUM_ALMOXARIFADO) % 1000 == 0:
            f.write("COMMIT;\n")