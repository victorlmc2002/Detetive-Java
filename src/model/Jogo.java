package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Agregado interno do Model. NÃO é mais público: o único ponto de acesso ao
// Model a partir da View/Controller é a Fachada (padrão Façade).
class Jogo {

	// Suspeitos na ordem oficial de jogada (Scarlet começa, item 7 da Preparação).
	private static final String[] SUSPEITOS_NA_ORDEM = {
		"Srta. Scarlet",
		"Coronel Mustard",
		"Sra. White",
		"Reverendo Green",
		"Sra. Peacock",
		"Professor Plum"
	};

	private final Tabuleiro tabuleiro;
	private final Dado dado;
	private final Random random;

	private List<Jogador> jogadores;
	private Envelope envelope;
	private int indiceVez;
	private boolean partidaIniciada = false;
	private int ultimoLancamento = 0;
	private int valorDado1 = 0;
	private int valorDado2 = 0;
	private final Set<Integer> eliminados = new HashSet<>();

	// Listas estáticas — espelham Carta.criarBaralhoCompleto().
	private static final String[] LISTA_SUSPEITOS = {
		"Srta. Scarlet", "Coronel Mustard", "Sra. White",
		"Reverendo Green", "Sra. Peacock", "Professor Plum"
	};
	private static final String[] LISTA_ARMAS = {
		"Corda", "Cano de Chumbo", "Faca",
		"Chave Inglesa", "Castiçal", "Revólver"
	};
	private static final String[] LISTA_COMODOS = {
		"Cozinha", "Sala de Música", "Jardim de Inverno",
		"Sala de Jantar", "Salão de Jogos", "Biblioteca",
		"Sala de Estar", "Entrada", "Escritório"
	};

	public Jogo() {
		this.tabuleiro = new Tabuleiro();
		this.dado = new Dado();
		this.random = new Random();
	}

	// Construtor com semente — uso interno e em testes, para resultados previsíveis.
	Jogo(long semente) {
		this.tabuleiro = new Tabuleiro();
		this.dado = new Dado(semente);
		this.random = new Random(semente);
	}

	// Item 4-7 da Preparação:
	// - cria o baralho com 21 cartas;
	// - sorteia 1 carta de cada tipo para o envelope confidencial;
	// - distribui as 18 restantes entre os jogadores;
	// - posiciona cada peão em sua casa inicial;
	// - define Srta. Scarlet como primeira a jogar.
	public void iniciarPartida(List<String> nomesJogadores) {
		if (nomesJogadores == null || nomesJogadores.size() < 3 || nomesJogadores.size() > 6) {
			throw new IllegalArgumentException("Clue requer de 3 a 6 jogadores");
		}

		// Cria os jogadores, cada um com seu peão posicionado na casa inicial.
		jogadores = new ArrayList<>();
		for (int i = 0; i < nomesJogadores.size(); i++) {
			String suspeito = SUSPEITOS_NA_ORDEM[i];
			Casa casaInicial = tabuleiro.getCasaInicial(suspeito);
			Peao peao = new Peao(suspeito, casaInicial);
			jogadores.add(new Jogador(nomesJogadores.get(i), peao));
		}

		// Item 4: monta baralho, sorteia envelope (3 cartas saem).
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		envelope = Envelope.sortear(baralho, random);

		// Item 5: distribui as 18 cartas restantes entre os jogadores.
		Jogador.distribuirCartas(jogadores, baralho, random);

		// Item 7: Scarlet começa.
		indiceVez = 0;
		partidaIniciada = true;
	}

	public String jogadorDaVez() {
		verificarPartidaIniciada();
		return jogadores.get(indiceVez).getNome();
	}

	public String suspeitoDaVez() {
		verificarPartidaIniciada();
		return jogadores.get(indiceVez).getPeao().getSuspeito();
	}

	public Posicao posicaoDoPiaoDaVez() {
		verificarPartidaIniciada();
		Casa c = jogadores.get(indiceVez).getPeao().getCasa();
		return new Posicao(c.getLinha(), c.getColuna());
	}

	public int lancarDado() {
		ultimoLancamento = dado.lancar();
		return ultimoLancamento;
	}

	// Lança os DOIS dados aleatoriamente (Clue usa 2 dados). Guarda os valores
	// individuais — para a View exibir as duas imagens — e o total, usado pela
	// movimentação. Retorna o total.
	public int lancarDados() {
		valorDado1 = dado.lancar();
		valorDado2 = dado.lancar();
		ultimoLancamento = valorDado1 + valorDado2;
		return ultimoLancamento;
	}

	// Permite que o TESTADOR defina os valores dos dados (requisito da 3ª
	// iteração), em vez de obtê-los por randomização. Retorna o total.
	public int definirDados(int d1, int d2) {
		if (d1 < 1 || d1 > 6 || d2 < 1 || d2 > 6) {
			throw new IllegalArgumentException("Valores de dado devem estar entre 1 e 6");
		}
		valorDado1 = d1;
		valorDado2 = d2;
		ultimoLancamento = d1 + d2;
		return ultimoLancamento;
	}

	public int getDado1() {
		return valorDado1;
	}

	public int getDado2() {
		return valorDado2;
	}

	public int passosRestantes() {
		return ultimoLancamento;
	}

	public Posicao posicaoDoPiao(String suspeito) {
		verificarPartidaIniciada();
		for (Jogador j : jogadores) {
			if (j.getPeao().getSuspeito().equals(suspeito)) {
				Casa c = j.getPeao().getCasa();
				return new Posicao(c.getLinha(), c.getColuna());
			}
		}
		throw new IllegalArgumentException("Suspeito não está em jogo: " + suspeito);
	}

	// Para debug/View: identifica o tipo de uma casa por um caractere curto.
	//   '.' = corredor   'P' = porta   'X' = inacessivel/fora
	//   inicial do comodo (K,M,J,D,G,B,L,E,O) = interior de comodo
	public char tipoCasa(int linha, int coluna) {
		Casa c = tabuleiro.getCasa(linha, coluna);
		if (c == null || c.getTipo() == TipoCasa.INACESSIVEL) return 'X';
		if (c.ehInterior()) {
			String nome = c.getComodo().getNome();
			if (nome.equals("Cozinha"))            return 'K';
			if (nome.equals("Sala de Música"))     return 'M';
			if (nome.equals("Jardim de Inverno"))  return 'J';
			if (nome.equals("Sala de Jantar"))     return 'D';
			if (nome.equals("Salão de Jogos"))     return 'G';
			if (nome.equals("Biblioteca"))         return 'B';
			if (nome.equals("Sala de Estar"))      return 'L';
			if (nome.equals("Entrada"))            return 'E';
			if (nome.equals("Escritório"))         return 'O';
			return '?';
		}
		// Corredor: verifica se é porta de algum cômodo.
		for (Comodo cm : tabuleiro.getComodos().values()) {
			if (cm.getPortas().contains(c)) return 'P';
		}
		return '.';
	}

	public List<String> suspeitosEmJogo() {
		verificarPartidaIniciada();
		List<String> nomes = new ArrayList<>();
		for (Jogador j : jogadores) {
			nomes.add(j.getPeao().getSuspeito());
		}
		return nomes;
	}

	// Item 3: mapeia todas as casas alcançáveis pelo peão da vez,
	// dado o número de passos do dado. Considera os outros peões como bloqueio.
	public Set<Posicao> casasAlcancaveis(int passos) {
		verificarPartidaIniciada();
		Casa origem = jogadores.get(indiceVez).getPeao().getCasa();
		Set<Casa> bloqueadas = casasOcupadasPorOutros();

		Set<Casa> alvos = tabuleiro.mapearCasas(origem, passos, bloqueadas);

		Set<Posicao> resultado = new HashSet<>();
		for (Casa c : alvos) {
			resultado.add(new Posicao(c.getLinha(), c.getColuna()));
		}
		return resultado;
	}

	// Item 4: desloca o peão da vez para uma das casas mapeadas.
	public void deslocarPiao(Posicao destino) {
		verificarPartidaIniciada();
		if (destino == null) {
			throw new IllegalArgumentException("Destino não pode ser null");
		}
		Casa casaDestino = tabuleiro.getCasa(destino.getLinha(), destino.getColuna());
		if (casaDestino == null) {
			throw new IllegalArgumentException("Posição fora do tabuleiro: " + destino);
		}
		jogadores.get(indiceVez).getPeao().mover(casaDestino);
	}

	public void proximoJogador() {
		verificarPartidaIniciada();
		int n = jogadores.size();
		// Avança até o próximo jogador não eliminado.
		for (int i = 1; i <= n; i++) {
			int candidato = (indiceVez + i) % n;
			if (!eliminados.contains(candidato)) {
				indiceVez = candidato;
				return;
			}
		}
		// Todos eliminados (não deveria acontecer; mantém o índice atual).
	}

	public List<String> cartasDoJogador(String nomeJogador) {
		verificarPartidaIniciada();
		Jogador j = encontrarJogador(nomeJogador);
		List<String> nomes = new ArrayList<>();
		for (Carta c : j.getMao()) {
			nomes.add(c.getNome());
		}
		return nomes;
	}

	public int totalDeJogadores() {
		verificarPartidaIniciada();
		return jogadores.size();
	}

	// ---- Palpite / Acusação ----

	boolean estaEmComodo() {
		verificarPartidaIniciada();
		return jogadores.get(indiceVez).getPeao().getCasa().ehInterior();
	}

	String comodoAtual() {
		verificarPartidaIniciada();
		Casa c = jogadores.get(indiceVez).getPeao().getCasa();
		if (!c.ehInterior()) {
			throw new IllegalStateException("Jogador não está em cômodo");
		}
		return c.getComodo().getNome();
	}

	List<String> getSuspeitos() {
		List<String> lista = new ArrayList<>();
		for (String s : LISTA_SUSPEITOS) lista.add(s);
		return lista;
	}

	List<String> getArmas() {
		List<String> lista = new ArrayList<>();
		for (String a : LISTA_ARMAS) lista.add(a);
		return lista;
	}

	List<String> getComodos() {
		List<String> lista = new ArrayList<>();
		for (String c : LISTA_COMODOS) lista.add(c);
		return lista;
	}

	// Tenta refutar o palpite percorrendo os outros jogadores em ordem de turno.
	// Retorna "NomeJogador|NomeCarta" do primeiro que pode refutar, ou null.
	String tentarRefutar(String nomeSuspeito, String nomeArma, String nomeComodo) {
		verificarPartidaIniciada();
		Carta cs = new Carta(TipoCarta.SUSPEITO, nomeSuspeito);
		Carta ca = new Carta(TipoCarta.ARMA,     nomeArma);
		Carta cc = new Carta(TipoCarta.COMODO,   nomeComodo);
		int n = jogadores.size();
		for (int i = 1; i < n; i++) {
			int idx = (indiceVez + i) % n;
			Jogador j = jogadores.get(idx);
			List<Carta> provas = j.cartasQueProvam(cs, ca, cc);
			if (!provas.isEmpty()) {
				// Escolhe a primeira carta disponível (regra simplificada).
				return j.getNome() + "|" + provas.get(0).getNome();
			}
		}
		return null; // ninguém refutou
	}

	// Verifica se a acusação bate com o envelope.
	boolean verificarAcusacao(String nomeSuspeito, String nomeArma, String nomeComodo) {
		verificarPartidaIniciada();
		return envelope.getSuspeito().getNome().equals(nomeSuspeito)
			&& envelope.getArma().getNome().equals(nomeArma)
			&& envelope.getComodo().getNome().equals(nomeComodo);
	}

	// Elimina o jogador da vez (acusação errada).
	void eliminarJogadorDaVez() {
		verificarPartidaIniciada();
		eliminados.add(indiceVez);
	}

	boolean jogadorDaVezEliminado() {
		verificarPartidaIniciada();
		return eliminados.contains(indiceVez);
	}

	// ---- Métodos internos (package-private) usados nos testes ----

	Envelope getEnvelope() {
		return envelope;
	}

	// ---- Auxiliares privados ----

	private Set<Casa> casasOcupadasPorOutros() {
		Set<Casa> ocupadas = new HashSet<>();
		for (int i = 0; i < jogadores.size(); i++) {
			if (i == indiceVez) continue;
			ocupadas.add(jogadores.get(i).getPeao().getCasa());
		}
		return ocupadas;
	}

	private Jogador encontrarJogador(String nome) {
		for (Jogador j : jogadores) {
			if (j.getNome().equals(nome)) return j;
		}
		throw new IllegalArgumentException("Jogador não encontrado: " + nome);
	}

	private void verificarPartidaIniciada() {
		if (!partidaIniciada) {
			throw new IllegalStateException("Partida não foi iniciada");
		}
	}
}
