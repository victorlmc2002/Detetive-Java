package model;

import java.util.ArrayList;
import java.util.List;

class Carta {

	private final TipoCarta tipo;
	private final String nome;

	Carta(TipoCarta tipo, String nome) {
		this.tipo = tipo;
		this.nome = nome;
	}

	TipoCarta getTipo() {
		return tipo;
	}

	String getNome() {
		return nome;
	}

	static List<Carta> criarBaralhoCompleto() {
		List<Carta> baralho = new ArrayList<>();

		String[] suspeitos = {
			"Srta. Scarlet",
			"Coronel Mustard",
			"Sra. White",
			"Reverendo Green",
			"Sra. Peacock",
			"Professor Plum"
		};
		for (String s : suspeitos) {
			baralho.add(new Carta(TipoCarta.SUSPEITO, s));
		}

		String[] armas = {
			"Corda",
			"Cano de Chumbo",
			"Faca",
			"Chave Inglesa",
			"Castiçal",
			"Revólver"
		};
		for (String a : armas) {
			baralho.add(new Carta(TipoCarta.ARMA, a));
		}

		String[] comodos = {
			"Cozinha",
			"Sala de Música",
			"Jardim de Inverno",
			"Sala de Jantar",
			"Salão de Jogos",
			"Biblioteca",
			"Sala de Estar",
			"Entrada",
			"Escritório"
		};
		for (String c : comodos) {
			baralho.add(new Carta(TipoCarta.COMODO, c));
		}

		return baralho;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Carta)) return false;
		Carta outra = (Carta) o;
		return tipo == outra.tipo && nome.equals(outra.nome);
	}

	@Override
	public int hashCode() {
		return tipo.hashCode() * 31 + nome.hashCode();
	}

	@Override
	public String toString() {
		return nome + " (" + tipo + ")";
	}
}
