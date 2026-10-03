package com.example.data

/**
 * Catálogo Léxico Local de Pictogramas DUA / CAA para Educação Infantil e Ensino Fundamental.
 * Fornece mapeamento determinístico, instantâneo e 100% offline de mais de 300 conceitos
 * pedagógicos frequentes em Português do Brasil para seus símbolos visuais correspondentes.
 */
object PictogramCatalog {

    data class Item(
        val key: String,
        val label: String,
        val symbol: String,
        val category: String = "Geral"
    )

    private val catalog: Map<String, Item> by lazy {
        val list = listOf(
            // --- ITENS ESCOLARES E AÇÕES PEDAGÓGICAS ---
            Item("escola", "Escola", "🏫", "Escola"),
            Item("colegio", "Colégio", "🏫", "Escola"),
            Item("sala de aula", "Sala de Aula", "🏫", "Escola"),
            Item("professor", "Professor", "👨‍🏫", "Escola"),
            Item("professora", "Professora", "👩‍🏫", "Escola"),
            Item("aluno", "Aluno", "👦", "Escola"),
            Item("aluna", "Aluna", "👧", "Escola"),
            Item("estudante", "Estudante", "🧑‍🎓", "Escola"),
            Item("lapis", "Lápis", "✏️", "Material"),
            Item("lápis", "Lápis", "✏️", "Material"),
            Item("borracha", "Borracha", "🧼", "Material"),
            Item("caderno", "Caderno", "📓", "Material"),
            Item("livro", "Livro", "📖", "Material"),
            Item("leitura", "Leitura", "📖", "Ação"),
            Item("ler", "Ler", "📖", "Ação"),
            Item("escrever", "Escrever", "✍️", "Ação"),
            Item("escrita", "Escrita", "✍️", "Ação"),
            Item("desenhar", "Desenhar", "🎨", "Ação"),
            Item("desenho", "Desenho", "🎨", "Material"),
            Item("pintar", "Pintar", "🖌️", "Ação"),
            Item("pintura", "Pintura", "🎨", "Material"),
            Item("tesoura", "Tesoura", "✂️", "Material"),
            Item("recortar", "Recortar", "✂️", "Ação"),
            Item("cortar", "Cortar", "✂️", "Ação"),
            Item("colar", "Colar", "🧴", "Ação"),
            Item("cola", "Cola", "🧴", "Material"),
            Item("regua", "Régua", "📏", "Material"),
            Item("régua", "Régua", "📏", "Material"),
            Item("mochila", "Mochila", "🎒", "Material"),
            Item("apontador", "Apontador", "✏️", "Material"),
            Item("papel", "Papel", "📄", "Material"),
            Item("folha", "Folha", "📄", "Material"),
            Item("caneta", "Caneta", "🖊️", "Material"),
            Item("circular", "Circular", "⭕", "Ação"),
            Item("marcar x", "Marcar X", "❌", "Ação"),
            Item("marcar", "Marcar", "☑️", "Ação"),
            Item("ligar", "Ligar", "↔️", "Ação"),
            Item("completar", "Completar", "🧩", "Ação"),
            Item("contar", "Contar", "🔢", "Ação"),
            Item("somar", "Somar", "➕", "Ação"),
            Item("subtrair", "Subtrair", "➖", "Ação"),
            Item("multiplicar", "Multiplicar", "✖️", "Ação"),
            Item("dividir", "Dividir", "➗", "Ação"),
            Item("ouvir", "Ouvir", "👂", "Ação"),
            Item("escutar", "Escutar", "👂", "Ação"),
            Item("olhar", "Olhar", "👀", "Ação"),
            Item("ver", "Ver", "👀", "Ação"),
            Item("falar", "Falar", "🗣️", "Ação"),
            Item("pensar", "Pensar", "💡", "Ação"),
            Item("ajudar", "Ajudar", "🤝", "Ação"),
            Item("brincar", "Brincar", "🪀", "Ação"),
            Item("guardar", "Guardar", "📦", "Ação"),
            Item("silencio", "Silêncio", "🤫", "Ação"),
            Item("silêncio", "Silêncio", "🤫", "Ação"),
            Item("atencao", "Atenção", "⚠️", "Ação"),
            Item("atenção", "Atenção", "⚠️", "Ação"),
            Item("certo", "Correto", "✅", "Conceito"),
            Item("correto", "Correto", "✅", "Conceito"),
            Item("errado", "Errado", "❌", "Conceito"),
            Item("incorreto", "Incorreto", "❌", "Conceito"),

            // --- FAMÍLIA, HISTÓRIA, TEMPO E RELAÇÕES SOCIAIS ---
            Item("familia", "Família", "👨‍👩‍👧‍👦", "Família"),
            Item("família", "Família", "👨‍👩‍👧‍👦", "Família"),
            Item("avo", "Avó", "👵", "Família"),
            Item("avó", "Avó", "👵", "Família"),
            Item("vovo", "Vovó", "👵", "Família"),
            Item("vovó", "Vovó", "👵", "Família"),
            Item("avô", "Avô", "👴", "Família"),
            Item("vovô", "Vovô", "👴", "Família"),
            Item("mae", "Mãe", "👩", "Família"),
            Item("mãe", "Mãe", "👩", "Família"),
            Item("pai", "Pai", "👨", "Família"),
            Item("pais", "Pais (Família)", "👨‍👩‍👧‍👦", "Família"),
            Item("país", "País", "🌎", "Geografia"),
            Item("países", "Países", "🌎", "Geografia"),
            Item("manga fruta", "Manga (Fruta)", "🥭", "Alimentos"),
            Item("manga de camisa", "Manga de Camisa", "👔", "Roupas"),
            Item("mangá", "Mangá (HQ)", "📖", "Cultura"),
            Item("força", "Força Física", "💪", "Ciências"),
            Item("forca", "Jogo da Forca", "🪢", "Lógica"),
            Item("secretária", "Secretária", "👩‍💼", "Profissão"),
            Item("secretaria", "Secretaria Escolar", "🏫", "Escola"),
            Item("sábio", "Sábio", "🧠", "Conhecimento"),
            Item("sabiá", "Sabiá", "🐦", "Animais"),
            Item("irmao", "Irmão", "👦", "Família"),
            Item("irmão", "Irmão", "👦", "Família"),
            Item("irma", "Irmã", "👧", "Família"),
            Item("irmã", "Irmã", "👧", "Família"),
            Item("bebe", "Bebê", "👶", "Família"),
            Item("bebê", "Bebê", "👶", "Família"),
            Item("crianca", "Criança", "🧒", "Família"),
            Item("criança", "Criança", "🧒", "Família"),
            Item("idoso", "Idoso", "👴", "Família"),
            Item("idosa", "Idosa", "👵", "Família"),
            Item("velho", "Mais Velho", "👵", "História"),
            Item("velha", "Mais Velha", "👵", "História"),
            Item("novo", "Novo", "👶", "História"),
            Item("nova", "Nova", "👶", "História"),
            Item("foto", "Foto", "📷", "Mídia"),
            Item("fotografia", "Fotografia", "📷", "Mídia"),
            Item("foto antiga", "Foto Antiga", "📷", "História"),
            Item("fotografia antiga", "Foto Antiga", "📷", "História"),
            Item("foto de hoje", "Foto Atual", "📷", "História"),
            Item("foto atual", "Foto Atual", "📷", "História"),
            Item("retrato", "Retrato", "🖼️", "Mídia"),
            Item("imagem", "Imagem", "🖼️", "Mídia"),
            Item("passado", "Passado", "⏳", "Tempo"),
            Item("antigo", "Antigo", "⏳", "Tempo"),
            Item("antiga", "Antiga", "⏳", "Tempo"),
            Item("antigamente", "Antigamente", "⏳", "Tempo"),
            Item("presente", "Presente", "📅", "Tempo"),
            Item("hoje", "Hoje", "📅", "Tempo"),
            Item("atual", "Atual", "📅", "Tempo"),
            Item("agora", "Agora", "⏱️", "Tempo"),
            Item("futuro", "Futuro", "🚀", "Tempo"),
            Item("historia", "História", "📜", "História"),
            Item("história", "História", "📜", "História"),

            // --- LÓGICA, VERDADEIRO/FALSO E RESPOSTAS ---
            Item("verdadeiro", "Verdadeiro (V)", "✅", "Lógica"),
            Item("verdadeira", "Verdadeira (V)", "✅", "Lógica"),
            Item("verdade", "Verdadeiro", "✅", "Lógica"),
            Item("v", "Verdadeiro", "✅", "Lógica"),
            Item("falso", "Falso (F)", "❌", "Lógica"),
            Item("falsa", "Falsa (F)", "❌", "Lógica"),
            Item("f", "Falso", "❌", "Lógica"),
            Item("sim", "Sim", "👍", "Lógica"),
            Item("nao", "Não", "👎", "Lógica"),
            Item("não", "Não", "👎", "Lógica"),

            // --- NÚMEROS INDIVIDUAIS ---
            Item("1", "1", "1️⃣", "Matemática"),
            Item("2", "2", "2️⃣", "Matemática"),
            Item("3", "3", "3️⃣", "Matemática"),
            Item("4", "4", "4️⃣", "Matemática"),
            Item("5", "5", "5️⃣", "Matemática"),
            Item("6", "6", "6️⃣", "Matemática"),
            Item("7", "7", "7️⃣", "Matemática"),
            Item("8", "8", "8️⃣", "Matemática"),
            Item("9", "9", "9️⃣", "Matemática"),
            Item("10", "10", "🔟", "Matemática"),
            Item("gelo", "Gelo", "🧊", "Ciências"),
            Item("solido", "Sólido", "🧊", "Ciências"),
            Item("sólido", "Sólido", "🧊", "Ciências"),
            Item("gelo: solido", "Gelo (Sólido)", "🧊", "Ciências"),
            Item("gelo: sólido", "Gelo (Sólido)", "🧊", "Ciências"),
            Item("liquido", "Líquido", "💧", "Ciências"),
            Item("líquido", "Líquido", "💧", "Ciências"),
            Item("agua: liquido", "Água (Líquido)", "💧", "Ciências"),
            Item("água: líquido", "Água (Líquido)", "💧", "Ciências"),
            Item("gasoso", "Gasoso", "💨", "Ciências"),
            Item("ar", "Ar", "💨", "Ciências"),
            Item("ar: gasoso", "Ar (Gasoso)", "💨", "Ciências"),
            Item("vapor", "Vapor", "♨️", "Ciências"),
            Item("fumaca", "Fumaça", "💨", "Ciências"),
            Item("fumaça", "Fumaça", "💨", "Ciências"),
            Item("temperatura", "Temperatura", "🌡️", "Ciências"),
            Item("quente", "Quente", "🔥", "Ciências"),
            Item("frio", "Frio", "❄️", "Ciências"),
            Item("neve", "Neve", "❄️", "Ciências"),
            Item("experimento", "Experimento", "🧪", "Ciências"),
            Item("laboratorio", "Laboratório", "🔬", "Ciências"),
            Item("laboratório", "Laboratório", "🔬", "Ciências"),
            Item("ciencias", "Ciências", "🔬", "Ciências"),
            Item("ciências", "Ciências", "🔬", "Ciências"),
            Item("arvore", "Árvore", "🌳", "Natureza"),
            Item("árvore", "Árvore", "🌳", "Natureza"),
            Item("planta", "Planta", "🌱", "Natureza"),
            Item("flor", "Flor", "🌸", "Natureza"),
            Item("sol", "Sol", "☀️", "Natureza"),
            Item("lua", "Lua", "🌙", "Natureza"),
            Item("estrela", "Estrela", "⭐", "Natureza"),
            Item("nuvem", "Nuvem", "☁️", "Natureza"),
            Item("chuva", "Chuva", "🌧️", "Natureza"),
            Item("rio", "Rio", "🌊", "Natureza"),
            Item("mar", "Mar", "🌊", "Natureza"),
            Item("terra", "Terra", "🌍", "Natureza"),
            Item("planeta", "Planeta", "🪐", "Natureza"),
            Item("fogo", "Fogo", "🔥", "Natureza"),
            Item("vento", "Vento", "💨", "Natureza"),
            Item("cachorro", "Cachorro", "🐶", "Animais"),
            Item("cao", "Cão", "🐶", "Animais"),
            Item("cão", "Cão", "🐶", "Animais"),
            Item("gato", "Gato", "🐱", "Animais"),
            Item("passaro", "Pássaro", "🐦", "Animais"),
            Item("pássaro", "Pássaro", "🐦", "Animais"),
            Item("ave", "Ave", "🐦", "Animais"),
            Item("peixe", "Peixe", "🐟", "Animais"),
            Item("vaca", "Vaca", "🐮", "Animais"),
            Item("boi", "Boi", "🐂", "Animais"),
            Item("cavalo", "Cavalo", "🐴", "Animais"),
            Item("porco", "Porco", "🐷", "Animais"),
            Item("galinha", "Galinha", "🐔", "Animais"),
            Item("galo", "Galo", "🐓", "Animais"),
            Item("pato", "Pato", "🦆", "Animais"),
            Item("sapo", "Sapo", "🐸", "Animais"),
            Item("tartaruga", "Tartaruga", "🐢", "Animais"),
            Item("coelho", "Coelho", "🐰", "Animais"),
            Item("leao", "Leão", "🦁", "Animais"),
            Item("leão", "Leão", "🦁", "Animais"),
            Item("tigre", "Tigre", "🐯", "Animais"),
            Item("elefante", "Elefante", "🐘", "Animais"),
            Item("girafa", "Girafa", "🦒", "Animais"),
            Item("macaco", "Macaco", "🐵", "Animais"),
            Item("cobra", "Cobra", "🐍", "Animais"),
            Item("urso", "Urso", "🐻", "Animais"),
            Item("borboleta", "Borboleta", "🦋", "Animais"),
            Item("abelha", "Abelha", "🐝", "Animais"),
            Item("formiga", "Formiga", "🐜", "Animais"),
            Item("aranha", "Aranha", "🕷️", "Animais"),

            // --- ALIMENTOS E BEBIDAS ---
            Item("maca", "Maçã", "🍎", "Alimentos"),
            Item("maçã", "Maçã", "🍎", "Alimentos"),
            Item("banana", "Banana", "🍌", "Alimentos"),
            Item("laranja", "Laranja", "🍊", "Alimentos"),
            Item("uva", "Uva", "🍇", "Alimentos"),
            Item("morango", "Morango", "🍓", "Alimentos"),
            Item("melancia", "Melancia", "🍉", "Alimentos"),
            Item("abacaxi", "Abacaxi", "🍍", "Alimentos"),
            Item("pera", "Pêra", "🍐", "Alimentos"),
            Item("pêra", "Pêra", "🍐", "Alimentos"),
            Item("limao", "Limão", "🍋", "Alimentos"),
            Item("limão", "Limão", "🍋", "Alimentos"),
            Item("cenoura", "Cenoura", "🥕", "Alimentos"),
            Item("tomate", "Tomate", "🍅", "Alimentos"),
            Item("batata", "Batata", "🥔", "Alimentos"),
            Item("milho", "Milho", "🌽", "Alimentos"),
            Item("arroz", "Arroz", "🍚", "Alimentos"),
            Item("feijao", "Feijão", "🫘", "Alimentos"),
            Item("feijão", "Feijão", "🫘", "Alimentos"),
            Item("pao", "Pão", "🍞", "Alimentos"),
            Item("pão", "Pão", "🍞", "Alimentos"),
            Item("leite", "Leite", "🥛", "Alimentos"),
            Item("agua", "Água", "💧", "Alimentos"),
            Item("água", "Água", "💧", "Alimentos"),
            Item("suco", "Suco", "🧃", "Alimentos"),
            Item("queijo", "Queijo", "🧀", "Alimentos"),
            Item("ovo", "Ovo", "🥚", "Alimentos"),
            Item("carne", "Carne", "🥩", "Alimentos"),
            Item("frango", "Frango", "🍗", "Alimentos"),
            Item("peixe comida", "Peixe", "🐟", "Alimentos"),
            Item("bolo", "Bolo", "🎂", "Alimentos"),
            Item("biscoito", "Biscoito", "🍪", "Alimentos"),
            Item("sorvete", "Sorvete", "🍦", "Alimentos"),

            // --- CORPO HUMANO E HIGIENE ---
            Item("corpo", "Corpo", "🧍", "Corpo"),
            Item("cabeca", "Cabeça", "🗣️", "Corpo"),
            Item("cabeça", "Cabeça", "🗣️", "Corpo"),
            Item("olhos", "Olhos", "👀", "Corpo"),
            Item("olho", "Olho", "👁️", "Corpo"),
            Item("boca", "Boca", "👄", "Corpo"),
            Item("nariz", "Nariz", "👃", "Corpo"),
            Item("ouvido", "Ouvido", "👂", "Corpo"),
            Item("orelha", "Orelha", "👂", "Corpo"),
            Item("mao", "Mão", "✋", "Corpo"),
            Item("mão", "Mão", "✋", "Corpo"),
            Item("pe", "Pé", "🦶", "Corpo"),
            Item("pé", "Pé", "🦶", "Corpo"),
            Item("perna", "Perna", "🦵", "Corpo"),
            Item("braco", "Braço", "💪", "Corpo"),
            Item("braço", "Braço", "💪", "Corpo"),
            Item("dente", "Dente", "🦷", "Corpo"),
            Item("coracao", "Coração", "❤️", "Corpo"),
            Item("coração", "Coração", "❤️", "Corpo"),
            Item("lavar as maos", "Lavar as Mãos", "🧼", "Higiene"),
            Item("banho", "Banho", "🚿", "Higiene"),
            Item("escovar os dentes", "Escovar os Dentes", "🪥", "Higiene"),
            Item("escova", "Escova", "🪥", "Higiene"),

            // --- TRANSPORTES E CIDADE ---
            Item("carro", "Carro", "🚗", "Transporte"),
            Item("automovel", "Automóvel", "🚗", "Transporte"),
            Item("onibus", "Ônibus", "🚌", "Transporte"),
            Item("ônibus", "Ônibus", "🚌", "Transporte"),
            Item("bicicleta", "Bicicleta", "🚲", "Transporte"),
            Item("moto", "Moto", "🏍️", "Transporte"),
            Item("caminhao", "Caminhão", "🚚", "Transporte"),
            Item("caminhão", "Caminhão", "🚚", "Transporte"),
            Item("trem", "Trem", "🚆", "Transporte"),
            Item("metro", "Metrô", "🚇", "Transporte"),
            Item("aviao", "Avião", "✈️", "Transporte"),
            Item("avião", "Avião", "✈️", "Transporte"),
            Item("barco", "Barco", "⛵", "Transporte"),
            Item("navio", "Navio", "🚢", "Transporte"),
            Item("casa", "Casa", "🏠", "Cidade"),
            Item("moradia", "Moradia", "🏠", "Cidade"),
            Item("predio", "Prédio", "🏢", "Cidade"),
            Item("hospital", "Hospital", "🏥", "Cidade"),
            Item("parque", "Parque", "🏞️", "Cidade"),
            Item("rua", "Rua", "🛣️", "Cidade"),
            Item("semaforo", "Semáforo", "🚦", "Cidade"),
            Item("semáforo", "Semáforo", "🚦", "Cidade"),
            Item("transito", "Trânsito", "🚗🚕", "Cidade"),
            Item("trânsito", "Trânsito", "🚗🚕", "Cidade"),

            // --- EMOÇÕES E ROTINA ---
            Item("feliz", "Feliz", "😊", "Emoção"),
            Item("alegre", "Alegre", "😃", "Emoção"),
            Item("triste", "Triste", "😢", "Emoção"),
            Item("choro", "Chorando", "😭", "Emoção"),
            Item("bravo", "Bravo", "😠", "Emoção"),
            Item("irritado", "Irritado", "😡", "Emoção"),
            Item("calmo", "Calmo", "😌", "Emoção"),
            Item("cansado", "Cansado", "🥱", "Emoção"),
            Item("sono", "Sono", "😴", "Emoção"),
            Item("dormir", "Dormir", "😴", "Rotina"),
            Item("acordar", "Acordar", "⏰", "Rotina"),
            Item("comer", "Comer", "🍽️", "Rotina"),
            Item("beber", "Beber", "🥤", "Rotina"),
            Item("banheiro", "Banheiro", "🚻", "Rotina"),
            Item("recreio", "Recreio", "🛝", "Rotina"),

            // --- MATEMÁTICA, FORMAS E CORES ---
            Item("circulo", "Círculo", "🔴", "Forma"),
            Item("círculo", "Círculo", "🔴", "Forma"),
            Item("quadrado", "Quadrado", "🟦", "Forma"),
            Item("triangulo", "Triângulo", "🔺", "Forma"),
            Item("triângulo", "Triângulo", "🔺", "Forma"),
            Item("retangulo", "Retângulo", "▭", "Forma"),
            Item("retângulo", "Retângulo", "▭", "Forma"),
            Item("vermelho", "Vermelho", "🔴", "Cor"),
            Item("azul", "Azul", "🔵", "Cor"),
            Item("amarelo", "Amarelo", "🟡", "Cor"),
            Item("verde", "Verde", "🟢", "Cor"),
            Item("laranja cor", "Laranja", "🟠", "Cor"),
            Item("roxo", "Roxo", "🟣", "Cor"),
            Item("preto", "Preto", "⚫", "Cor"),
            Item("branco", "Branco", "⚪", "Cor"),
            Item("um", "1", "1️⃣", "Número"),
            Item("dois", "2", "2️⃣", "Número"),
            Item("tres", "3", "3️⃣", "Número"),
            Item("três", "3", "3️⃣", "Número"),
            Item("quatro", "4", "4️⃣", "Número"),
            Item("cinco", "5", "5️⃣", "Número"),
            Item("seis", "6", "6️⃣", "Número"),
            Item("sete", "7", "7️⃣", "Número"),
            Item("oito", "8", "8️⃣", "Número"),
            Item("nove", "9", "9️⃣", "Número"),
            Item("dez", "10", "🔟", "Número"),
            Item("dinheiro", "Dinheiro", "💵", "Matemática"),
            Item("moeda", "Moeda", "🪙", "Matemática"),
            Item("relogio", "Relógio", "⏰", "Matemática"),
            Item("relógio", "Relógio", "⏰", "Matemática"),
            Item("tempo", "Tempo", "⏳", "Matemática"),
            Item("calendario", "Calendário", "📅", "Matemática"),
            Item("calendário", "Calendário", "📅", "Matemática"),

            // --- FAMÍLIA E SOCIEDADE ---
            Item("familia", "Família", "👨‍👩‍👧‍👦", "Família"),
            Item("família", "Família", "👨‍👩‍👧‍👦", "Família"),
            Item("mae", "Mãe", "👩", "Família"),
            Item("mãe", "Mãe", "👩", "Família"),
            Item("pai", "Pai", "👨", "Família"),
            Item("irmao", "Irmão", "👦", "Família"),
            Item("irmão", "Irmão", "👦", "Família"),
            Item("irma", "Irmã", "👧", "Família"),
            Item("irmã", "Irmã", "👧", "Família"),
            Item("bebe", "Bebê", "👶", "Família"),
            Item("bebê", "Bebê", "👶", "Família"),
            Item("vovo", "Vovô", "👴", "Família"),
            Item("vovô", "Vovô", "👴", "Família"),
            Item("vovó", "Vovó", "👵", "Família"),
            Item("amigo", "Amigo", "🧒🧒", "Família"),
            Item("amiga", "Amiga", "👧👧", "Família"),

            // --- ALFABETIZAÇÃO, LETRAS E VOGAIS ---
            Item("alfabeto", "Alfabeto", "🔤", "Alfabetização"),
            Item("letra", "Letras", "🔤", "Alfabetização"),
            Item("letras", "Letras", "🔤", "Alfabetização"),
            Item("vogal", "Vogais", "🔤", "Alfabetização"),
            Item("vogais", "Vogais", "🔤", "Alfabetização"),
            Item("consoante", "Consoantes", "🔤", "Alfabetização"),
            Item("consoantes", "Consoantes", "🔤", "Alfabetização"),
            Item("palavra", "Palavra", "📝", "Alfabetização"),
            Item("palavras", "Palavras", "📝", "Alfabetização"),
            Item("silaba", "Sílaba", "🧩", "Alfabetização"),
            Item("sílaba", "Sílaba", "🧩", "Alfabetização"),
            Item("silabas", "Sílabas", "🧩", "Alfabetização"),
            Item("sílabas", "Sílabas", "🧩", "Alfabetização"),

            // --- MATEMÁTICA CONCRETA, NÚMEROS, FRAÇÕES E DECIMAIS ---
            Item("numero", "Número", "🔢", "Matemática"),
            Item("número", "Número", "🔢", "Matemática"),
            Item("numeros", "Números", "🔢", "Matemática"),
            Item("números", "Números", "🔢", "Matemática"),
            Item("fracao", "Fração", "🍕", "Matemática"),
            Item("fração", "Fração", "🍕", "Matemática"),
            Item("fracoes", "Frações", "🍕", "Matemática"),
            Item("frações", "Frações", "🍕", "Matemática"),
            Item("fracao de pizza", "Fração de Pizza", "🍕", "Matemática"),
            Item("fração de pizza", "Fração de Pizza", "🍕", "Matemática"),
            Item("decimal", "Decimal", "🔢", "Matemática"),
            Item("decimais", "Decimais", "🔢", "Matemática"),
            Item("decimo", "Décimo", "🔢", "Matemática"),
            Item("décimo", "Décimo", "🔢", "Matemática"),
            Item("centesimo", "Centésimo", "🔢", "Matemática"),
            Item("centésimo", "Centésimo", "🔢", "Matemática"),
            Item("soma", "Soma", "➕", "Matemática"),
            Item("adicao", "Adição", "➕", "Matemática"),
            Item("adição", "Adição", "➕", "Matemática"),
            Item("subtracao", "Subtração", "➖", "Matemática"),
            Item("subtração", "Subtração", "➖", "Matemática"),
            Item("igual", "Igual", "🟰", "Matemática"),

            // --- BRINQUEDOS, LUGARES E COTIDIANO INFANTIL ---
            Item("bola", "Bola", "⚽", "Brinquedo"),
            Item("bolas", "Bolas", "⚽", "Brinquedo"),
            Item("boneca", "Boneca", "🪆", "Brinquedo"),
            Item("boneco", "Boneco", "🧸", "Brinquedo"),
            Item("carrinho", "Carrinho", "🚗", "Brinquedo"),
            Item("brinquedo", "Brinquedo", "🧸", "Brinquedo"),
            Item("brinquedos", "Brinquedos", "🧸", "Brinquedo"),
            Item("pipoca", "Pipoca", "🍿", "Alimento"),
            Item("igreja", "Igreja", "⛪", "Lugar"),
            Item("parque", "Parque", "🏞️", "Lugar"),
            Item("praca", "Praça", "🌳", "Lugar"),
            Item("praça", "Praça", "🌳", "Lugar"),
            Item("casa", "Casa", "🏠", "Lugar"),
            Item("gato", "Gato", "🐱", "Animais"),
            Item("cachorro", "Cachorro", "🐶", "Animais"),
            Item("passaro", "Pássaro", "🐦", "Animais"),
            Item("pássaro", "Pássaro", "🐦", "Animais"),
            Item("peixe", "Peixe", "🐟", "Animais")
        )

        list.associateBy { it.key.lowercase().trim() }
    }

    /**
     * Busca um pictograma no catálogo local com normalização avançada.
     */
    fun find(term: String): Item? {
        val normalized = term.lowercase().trim()
            .replace("[", "")
            .replace("]", "")
            .replace("arasaac:", "")
            .replace("arasaac", "")
            .replace("pictograma:", "")
            .replace("imagem:", "")
            .replace("pictograma", "")
            .replace("imagem", "")
            .replace(".", "")
            .trim()

        if (normalized.isBlank()) return null

        // 1. Match exato direto
        catalog[normalized]?.let { return it }

        // 2. Se tiver dois pontos ou barra (ex: "gelo: sólido", "gelo/sólido")
        if (normalized.contains(":") || normalized.contains("/")) {
            val parts = normalized.split(":", "/").map { it.trim() }
            for (p in parts) {
                catalog[p]?.let { return it }
            }
        }

        // 3. Match por palavras individuais
        val words = normalized.split(" ", "-", "_").filter { it.length > 2 }
        for (w in words) {
            catalog[w]?.let { return it }
        }

        // 4. Match por contenção apenas para termos longos (> 3 letras) onde a chave inteira esteja contida
        if (normalized.length >= 4) {
            for ((key, item) in catalog) {
                if (key.length >= 4 && normalized == key) {
                    return item
                }
            }
        }

        // 5. Fallback semântico determinístico estrito (sem substring ambígua como 'ar')
        val cleanLabel = term
            .replace(Regex("""\[(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card)(?:[/\s\-_]+(?:Pictograma|Imagem|Foto|Fotografia|Desenho|CAA|Visual|Ícone|Icone|Símbolo|Simbolo|Card))?:\s*""", RegexOption.IGNORE_CASE), "")
            .replace("[", "")
            .replace("]", "")
            .replace("ARASAAC:", "")
            .replace(Regex("""[\p{So}\p{Sk}\p{Sm}\p{Cs}\p{Cn}]"""), "")
            .trim()

        val exactWords = normalized.split(" ", "-", "_").filter { it.isNotBlank() }

        return when {
            exactWords.any { it in listOf("foto", "fotografia", "retrato", "camera", "câmera") } -> Item(normalized, cleanLabel.ifBlank { "Foto" }, "📷", "Mídia")
            exactWords.any { it in listOf("familia", "família", "parente", "parentes") } -> Item(normalized, cleanLabel.ifBlank { "Família" }, "👨‍👩‍👧‍👦", "Família")
            exactWords.any { it in listOf("avo", "avó", "vovo", "vovó", "idosa") } -> Item(normalized, cleanLabel.ifBlank { "Avó" }, "👵", "Família")
            exactWords.any { it in listOf("avô", "vovô", "idoso") } -> Item(normalized, cleanLabel.ifBlank { "Avô" }, "👴", "Família")
            exactWords.any { it in listOf("velho", "velha", "idosos") } -> Item(normalized, cleanLabel.ifBlank { "Mais Velho" }, "👵", "História")
            exactWords.any { it in listOf("novo", "nova", "crianca", "criança", "bebe", "bebê") } -> Item(normalized, cleanLabel.ifBlank { "Novo" }, "👶", "História")
            exactWords.any { it in listOf("verdade", "verdadeiro", "verdadeira") } || normalized == "v" -> Item(normalized, cleanLabel.ifBlank { "Verdadeiro (V)" }, "✅", "Lógica")
            exactWords.any { it in listOf("falso", "falsa", "falsidade", "mentira") } || normalized == "f" -> Item(normalized, cleanLabel.ifBlank { "Falso (F)" }, "❌", "Lógica")
            exactWords.any { it in listOf("antigo", "antiga", "passado", "historia", "história") } -> Item(normalized, cleanLabel.ifBlank { "História" }, "⏳", "História")
            exactWords.any { it in listOf("hoje", "atual", "presente", "agora") } -> Item(normalized, cleanLabel.ifBlank { "Atual" }, "📅", "Tempo")
            exactWords.any { it in listOf("ver", "olhar", "olho", "olhos", "enxergar") } -> Item(normalized, cleanLabel.ifBlank { "Ver" }, "👀", "Sentidos")
            exactWords.any { it in listOf("ouvir", "escutar", "ouvido", "orelha") } -> Item(normalized, cleanLabel.ifBlank { "Ouvir" }, "👂", "Sentidos")
            exactWords.any { it in listOf("falar", "dizer", "contar") } -> Item(normalized, cleanLabel.ifBlank { "Falar" }, "🗣️", "Comunicação")
            exactWords.any { it in listOf("solido", "sólido", "gelo") } -> Item(normalized, cleanLabel.ifBlank { "Sólido" }, "🧊", "Ciências")
            exactWords.any { it in listOf("liquido", "líquido", "agua", "água") } -> Item(normalized, cleanLabel.ifBlank { "Líquido" }, "💧", "Ciências")
            exactWords.any { it in listOf("gas", "gasoso", "vapor", "oxigênio", "oxigenio") } || exactWords.contains("ar") -> Item(normalized, cleanLabel.ifBlank { "Gasoso" }, "💨", "Ciências")
            exactWords.any { it in listOf("quente", "fogo", "calor") } -> Item(normalized, cleanLabel.ifBlank { "Quente" }, "🔥", "Ciências")
            exactWords.any { it in listOf("frio", "neve", "gelado") } -> Item(normalized, cleanLabel.ifBlank { "Frio" }, "❄️", "Ciências")
            normalized.all { it.isDigit() } -> Item(normalized, cleanLabel.ifBlank { normalized }, "🔢", "Matemática")
            else -> null
        }
    }

    /**
     * Resolve um símbolo/emoji determinístico para um termo ou expressão textual.
     */
    fun resolveSymbol(term: String): String {
        return find(term)?.symbol ?: ""
    }

    /**
     * Retorna a lista de todos os itens cadastrados.
     */
    fun getAll(): List<Item> = catalog.values.distinctBy { it.key }
}
