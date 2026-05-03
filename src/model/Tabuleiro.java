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
		"KKKKKK....MMMMMMM..JJJJJ",
		"KKKKKK....MMMMMMM..JJJJJ",
		"KKKKKK....MMMMMMM..JJJJJ",
		"KKKKKK....MMMMMMM..JJJJJ",
		"KKKKKK....MMMMMMM..JJJJJ",
		"KKKKKK....MMMMMMM.......",
		"........................",
		"........................",
		"DDDDDDDD..XXXXX..GGGGGGG",
		"DDDDDDDD..XXXXX..GGGGGGG",
		"DDDDDDDD..XXXXX..GGGGGGG",
		"DDDDDDDD..XXXXX..GGGGGGG",
		"DDDDDDDD..XXXXX..GGGGGGG",
		"........................",
		"........................",
		"..................BBBBBB",
		"..................BBBBBB",
		"..................BBBBBB",
		"LLLLLL..EEEEEEE...BBBBBB",
		"LLLLLL..EEEEEEE.........",
		"LLLLLL..EEEEEEE..OOOOOOO",
		"LLLLLL..EEEEEEE..OOOOOOO",
		"LLLLLL..EEEEEEE..OOOOOOO",
		"LLLLLL..EEEEEEE..OOOOOOO",
		"........EEEEEEE..OOOOOOO"
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

	Tabuleiro() {
		construirGrade();
		registrarPortas();
		registrarCasasIniciais();
	}

	private void construirGrade() {
		// fix: padronizar todas as linhas em COLUNAS chars
		for (int l = 0; l < LINHAS; l++) {
			String linha = LAYOUT[l];
//			if (linha.length() < COLUNAS) {
//				StringBuilder sb = new StringBuilder(linha);
//				while (sb.length() < COLUNAS) sb.append('.');
//				linha = sb.toString();
//			}
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
	// As coordenadas escolhidas batem com setas verdes do Tabuleiro
	private void registrarPortas() {
		ligarPorta("Cozinha", 6, 4);
		ligarPorta("Sala de Música", 6, 10);
		ligarPorta("Sala de Música", 6, 15);
		ligarPorta("Jardim de Inverno", 4, 18);
		ligarPorta("Sala de Jantar", 8, 8);
		ligarPorta("Sala de Jantar", 12, 8);
		ligarPorta("Salão de Jogos", 8, 16);
		ligarPorta("Salão de Jogos", 12, 16);
		ligarPorta("Biblioteca", 14, 18);
		ligarPorta("Biblioteca", 17, 17);
		ligarPorta("Sala de Estar", 18, 6);
		ligarPorta("Entrada", 17, 9);
		ligarPorta("Entrada", 17, 13);
		ligarPorta("Escritório", 19, 18);
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
		if (origem.ehInterior()) {
			Comodo comodo = origem.getComodo();
			for (Casa porta : comodo.getPortas()) {
				if (!bloqueadas.contains(porta)) {
					distancia.put(porta, 0);
					fila.add(porta);
					alcancaveis.add(porta);
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
				// Não continuamos a BFS a partir do interior — regra: parou ao entrar.
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
