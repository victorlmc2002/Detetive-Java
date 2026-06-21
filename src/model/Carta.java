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

	// Reconstrói uma carta a partir do seu nome, inferindo o tipo pelo baralho
	// completo. Usado na recuperação de uma partida salva em arquivo texto.
	static Carta porNome(String nome) {
		for (Carta c : criarBaralhoCompleto()) {
			if (c.getNome().equals(nome)) return c;
		}
		throw new IllegalArgumentException("Carta desconhecida: " + nome);
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
