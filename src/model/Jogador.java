package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class Jogador {

	private final String nome;
	private final Peao peao;
	private final List<Carta> mao = new ArrayList<>();

	Jogador(String nome, Peao peao) {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException("Nome do jogador não pode ser vazio");
		}
		if (peao == null) {
			throw new IllegalArgumentException("Jogador precisa de um peão");
		}
		this.nome = nome;
		this.peao = peao;
	}

	String getNome() {
		return nome;
	}

	Peao getPeao() {
		return peao;
	}

	// Devolve uma "vista" da mão que NÃO pode ser modificada por fora.
	// Quem chamar este método pode ler as cartas, mas não consegue add/remove.
	List<Carta> getMao() {
		return Collections.unmodifiableList(mao);
	}

	void receberCarta(Carta c) {
		if (c == null) {
			throw new IllegalArgumentException("Carta não pode ser null");
		}
		mao.add(c);
	}

	boolean temCarta(Carta c) {
		return mao.contains(c);
	}

	// Dado um palpite (suspeito + arma + cômodo), devolve as cartas da mão
	// que aparecem nele. Em jogada normal, o jogador escolheria uma dessas
	// para refutar mas isso ser[a decisão do Controller, não do Model.
	List<Carta> cartasQueProvam(Carta suspeito, Carta arma, Carta comodo) {
		List<Carta> provas = new ArrayList<>();
		if (temCarta(suspeito)) provas.add(suspeito);
		if (temCarta(arma)) provas.add(arma);
		if (temCarta(comodo)) provas.add(comodo);
		return provas;
	}

	// Embaralha o baralho e distribui as cartas entre os jogadores em
	// round-robin (jogador 0, jogador 1, ..., jogador n-1, jogador 0, ...).
	// Modifica o baralho: ele fica vazio ao final.
	static void distribuirCartas(List<Jogador> jogadores, List<Carta> baralho, Random random) {
		if (jogadores == null || jogadores.isEmpty()) {
			throw new IllegalArgumentException("Lista de jogadores vazia");
		}
		Collections.shuffle(baralho, random);
		int i = 0;
		for (Carta c : baralho) {
			jogadores.get(i % jogadores.size()).receberCarta(c);
			i++;
		}
		baralho.clear();
	}

	@Override
	public String toString() {
		return nome + " (" + peao.getSuspeito() + ")";
	}
}
