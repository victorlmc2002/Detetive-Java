package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Jogo {

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
		indiceVez = (indiceVez + 1) % jogadores.size();
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
