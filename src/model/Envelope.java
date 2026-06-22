package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Envelope {

	private final Carta suspeito;
	private final Carta arma;
	private final Carta comodo;

	Envelope(Carta suspeito, Carta arma, Carta comodo) {
		if (suspeito == null || suspeito.getTipo() != TipoCarta.SUSPEITO) {
			throw new IllegalArgumentException("Carta de suspeito inválida");
		}
		if (arma == null || arma.getTipo() != TipoCarta.ARMA) {
			throw new IllegalArgumentException("Carta de arma inválida");
		}
		if (comodo == null || comodo.getTipo() != TipoCarta.COMODO) {
			throw new IllegalArgumentException("Carta de cômodo inválida");
		}
		this.suspeito = suspeito;
		this.arma = arma;
		this.comodo = comodo;
	}

	Carta getSuspeito() {
		return suspeito;
	}

	Carta getArma() {
		return arma;
	}

	Carta getComodo() {
		return comodo;
	}

	// Verifica se uma carta qualquer está dentro do envelope.
	// Será usado depois, quando os jogadores fizerem palpites/acusações.
	boolean contem(Carta c) {
		return c.equals(suspeito) || c.equals(arma) || c.equals(comodo);
	}

	// Sorteia o envelope a partir de um baralho completo.
	// IMPORTANTE: modifica a lista 'baralho' - remove as 3 cartas escolhidas,
	// deixando apenas as 18 que sobraram para serem distribuídas aos jogadores.
	static Envelope sortear(List<Carta> baralho, Random random) {
		Carta suspeito = sortearDoTipo(baralho, TipoCarta.SUSPEITO, random);
		Carta arma = sortearDoTipo(baralho, TipoCarta.ARMA, random);
		Carta comodo = sortearDoTipo(baralho, TipoCarta.COMODO, random);
		return new Envelope(suspeito, arma, comodo);
	}

	// Auxiliar: filtra as cartas de um tipo, sorteia uma e remove do baralho.
	private static Carta sortearDoTipo(List<Carta> baralho, TipoCarta tipo, Random random) {
		List<Carta> doTipo = new ArrayList<>();
		for (Carta c : baralho) {
			if (c.getTipo() == tipo) {
				doTipo.add(c);
			}
		}
		if (doTipo.isEmpty()) {
			throw new IllegalStateException("Baralho não contém cartas do tipo " + tipo);
		}
		Carta escolhida = doTipo.get(random.nextInt(doTipo.size()));
		baralho.remove(escolhida);
		return escolhida;
	}

	@Override
	public String toString() {
		return "Envelope[" + suspeito + ", " + arma + ", " + comodo + "]";
	}
}
