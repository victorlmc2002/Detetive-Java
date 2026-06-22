package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class Tabuleiro {

	static final int LINHAS = 25;
	static final int COLUNAS = 24;

	// Layout do Tabuleiro-Clue-B.jpg.
	//  '.' = CORREDOR, demais letras = COMODO_INTERIOR.
	// K=Cozinha, M=Sala de Música, J=Jardim de Inverno,
	// D=Sala de Jantar, X=sala central CLUE (inacessível),
	// G=Salão de Jogos, B=Biblioteca,
	// L=Sala de Estar, E=Entrada, O=Escritório
	private static final String[] LAYOUT = {
		"XXXXXXXXX.XXXX.XXXXXXXXX",
		"KKKKKKX...MMMM...XJJJJJJ",
		"KKKKKK..MMMMMMMM..JJJJJJ",
		"KKKKKK..MMMMMMMM..JJJJJJ",
		"KKKKKK..MMMMMMMM..JJJJJJ",
		"KKKKKK..MMMMMMMM...JJJJX",
		"XKKKKK..MMMMMMMM........",
		"........MMMMMMMM.......X",
		"X.................GGGGGG",
		"DDDDD.............GGGGGG",
		"DDDDDDDD..XXXXX...GGGGGG",
		"DDDDDDDD..XXXXX...GGGGGG",
		"DDDDDDDD..XXXXX...GGGGGG",
		"DDDDDDDD..XXXXX........X",
		"DDDDDDDD..XXXXX...BBBBBX",
		"DDDDDDDD..XXXXX..BBBBBBB",
		"X.........XXXXX..BBBBBBB",
		".................BBBBBBB",
		"X........EEEEEE...BBBBBX",
		"LLLLLLL..EEEEEE.........",
		"LLLLLLL..EEEEEE........X",
		"LLLLLLL..EEEEEE..OOOOOOO",
		"LLLLLLL..EEEEEE..OOOOOOO",
		"LLLLLLL..EEEEEE..OOOOOOO",
		"LLLLLLX.XXEEEEXX.XOOOOOO"
	};

	private static final Map<Character, String> NOMES_COMODOS = Map.of(
		'K', "Cozinha",
		'M', "Sala de Música",
		'J', "Jardim de Inverno",
		'D', "Sala de Jantar",
		'G', "Salão de Jogos",
		'B', "Biblioteca",
		'L', "Sala de Estar",
		'E', "Entrada",
		'O', "Escritório"
	);

	private final Casa[][] grade = new Casa[LINHAS][COLUNAS];
	private final Map<String, Comodo> comodosPorNome = new HashMap<>();
	private final Map<String, Casa> casasIniciais = new HashMap<>();
	private final Map<Comodo, Comodo> passagensSecretas = new HashMap<>();

	Tabuleiro() {
		construirGrade();
		registrarPortas();
		registrarPassagensSecretas();
		registrarCasasIniciais();
	}

	// Passagens Secretas: Cozinha <-> Escritório, Sala de Estar <-> Jardim de Inverno.
	private void registrarPassagensSecretas() {
		ligarPassagem("Cozinha", "Escritório");
		ligarPassagem("Sala de Estar", "Jardim de Inverno");
	}

	private void ligarPassagem(String comodoA, String comodoB) {
		Comodo a = comodosPorNome.get(comodoA);
		Comodo b = comodosPorNome.get(comodoB);
		passagensSecretas.put(a, b);
		passagensSecretas.put(b, a);
	}

	private void construirGrade() {
		// fix: padronizar todas as linhas em COLUNAS chars 
		for (int l = 0; l < LINHAS; l++) {
			String linha = LAYOUT[l];
			if (linha.length() < COLUNAS) {
				StringBuilder sb = new StringBuilder(linha);
				while (sb.length() < COLUNAS) sb.append('.');
				linha = sb.toString();
			}
			for (int c = 0; c < COLUNAS; c++) {
				char ch = linha.charAt(c);
				Casa casa;
				if (ch == '#' || ch == 'X') {
					casa = new Casa(l, c, TipoCasa.INACESSIVEL);
				} else if (ch == '.') {
					casa = new Casa(l, c, TipoCasa.CORREDOR);
				} else {
					casa = new Casa(l, c, TipoCasa.COMODO_INTERIOR);
					String nome = NOMES_COMODOS.get(ch);
					Comodo comodo = comodosPorNome.computeIfAbsent(nome, Comodo::new);
					comodo.adicionarInterior(casa);
				}
				grade[l][c] = casa;
			}
		}
	}

	// Liga casas de portas aos respectivos cômodos
	private void registrarPortas() {
		ligarPorta("Cozinha", 7, 4);
		ligarPorta("Sala de Música", 5, 7);
		ligarPorta("Sala de Música", 5, 16);
		ligarPorta("Sala de Música", 8, 9);
		ligarPorta("Sala de Música", 8, 14);
		ligarPorta("Jardim de Inverno", 5, 18);
		ligarPorta("Sala de Jantar", 12, 8);
		ligarPorta("Sala de Jantar", 16, 6);
		ligarPorta("Salão de Jogos", 9, 17);
		ligarPorta("Salão de Jogos", 13, 22);
		ligarPorta("Biblioteca", 13, 20);
		ligarPorta("Biblioteca", 16, 16);
		ligarPorta("Sala de Estar", 18, 6);
		ligarPorta("Entrada", 17, 11);
		ligarPorta("Entrada", 17, 12);
		ligarPorta("Entrada", 20, 15);
		ligarPorta("Escritório", 20, 17);
	}

	private void ligarPorta(String nomeComodo, int linha, int coluna) {
		Comodo comodo = comodosPorNome.get(nomeComodo);
		Casa casa = grade[linha][coluna];
		comodo.adicionarPorta(casa);
	}

	private void registrarCasasIniciais() {
		// Posições marcadas no Tabuleiro
		casasIniciais.put("Srta. Scarlet", grade[24][7]);
		casasIniciais.put("Coronel Mustard", grade[17][0]);
		casasIniciais.put("Sra. White", grade[0][9]);
		casasIniciais.put("Reverendo Green", grade[0][14]);
		casasIniciais.put("Sra. Peacock", grade[6][23]);
		casasIniciais.put("Professor Plum", grade[19][23]);
	}

	Casa getCasa(int linha, int coluna) {
		if (linha < 0 || linha >= LINHAS || coluna < 0 || coluna >= COLUNAS) {
			return null;
		}
		return grade[linha][coluna];
	}

	Comodo getComodo(String nome) {
		return comodosPorNome.get(nome);
	}

	Map<String, Comodo> getComodos() {
		return comodosPorNome;
	}

	Casa getCasaInicial(String nomeSuspeito) {
		return casasIniciais.get(nomeSuspeito);
	}

	// BFS: retorna todas as casas (corredor) e cômodos alcançáveis
	// com até n passos de movimentos a partir de 'origem',
	// ignorando casas ocupadas em 'bloqueadas'.
	// O peão da vez (origem) não é considerado bloqueio.
	Set<Casa> mapearCasas(Casa origem, int passos, Set<Casa> bloqueadas) {
		Set<Casa> alcancaveis = new HashSet<>();
		if (origem == null || passos <= 0) return alcancaveis;

		Map<Casa, Integer> distancia = new HashMap<>();
		Queue<Casa> fila = new LinkedList<>();

		// Se peão começa dentro de um cômodo, suas saídas são as portas do cômodo.
		// Se houver passagem secreta, as portas do cômodo ligado também viram
		// pontos de origem (distância 0), permitindo a "teletransporte" via passagem.
		if (origem.ehInterior()) {
			Comodo comodo = origem.getComodo();
			for (Casa porta : comodo.getPortas()) {
				if (!bloqueadas.contains(porta)) {
					distancia.put(porta, 0);
					fila.add(porta);
					alcancaveis.add(porta);
				}
			}
			Comodo ligado = passagensSecretas.get(comodo);
			if (ligado != null) {
				// Interior do cômodo ligado também é alcançável (entrar pela passagem).
				for (Casa interior : ligado.getInterior()) {
					alcancaveis.add(interior);
				}
				for (Casa porta : ligado.getPortas()) {
					if (!bloqueadas.contains(porta) && !distancia.containsKey(porta)) {
						distancia.put(porta, 0);
						fila.add(porta);
						alcancaveis.add(porta);
					}
				}
			}
		} else {
			distancia.put(origem, 0);
			fila.add(origem);
		}

		int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

		while (!fila.isEmpty()) {
			Casa atual = fila.poll();
			int d = distancia.get(atual);
			if (d >= passos) continue;

			// Movimento normal por corredor.
			for (int[] dir : dirs) {
				Casa vizinha = getCasa(atual.getLinha() + dir[0], atual.getColuna() + dir[1]);
				if (vizinha == null) continue;
				if (vizinha.getTipo() == TipoCasa.INACESSIVEL) continue;
				if (vizinha.ehInterior()) continue;
				if (bloqueadas.contains(vizinha)) continue;
				if (distancia.containsKey(vizinha)) continue;
				distancia.put(vizinha, d + 1);
				alcancaveis.add(vizinha);
				fila.add(vizinha);
			}

			// Se a casa atual é uma porta, o jogador pode entrar no cômodo.
			Comodo c = comodoPorPorta(atual);
			if (c != null && (origem.getComodo() != c)) {
				for (Casa interior : c.getInterior()) {
					alcancaveis.add(interior);
				}
				// Não continuamos a BFS a partir do interior - regra: parou ao entrar.
			}
		}

		return alcancaveis;
	}

	private Comodo comodoPorPorta(Casa casa) {
		for (Comodo c : comodosPorNome.values()) {
			if (c.getPortas().contains(casa)) return c;
		}
		return null;
	}

	// Verifica se um destino (casa de corredor ou casa interior de cômodo)
	// está entre os alcançáveis a partir de 'origem' em 'passos' passos.
	boolean ehAlcancavel(Casa origem, Casa destino, int passos, Set<Casa> bloqueadas) {
		Set<Casa> alvos = mapearCasas(origem, passos, bloqueadas);
		return alvos.contains(destino);
	}

	Set<Casa> todasAsCasas() {
		Set<Casa> set = new HashSet<>();
		for (int l = 0; l < LINHAS; l++) {
			for (int c = 0; c < COLUNAS; c++) {
				set.add(grade[l][c]);
			}
		}
		return set;
	}
}
