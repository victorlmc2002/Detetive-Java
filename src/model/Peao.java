package model;

class Peao {
	private final String suspeito;
	private Casa casa;

	Peao(String suspeito, Casa casaInicial) {
		this.suspeito = suspeito;
		this.casa = casaInicial;
	}

	String getSuspeito() {
		return suspeito;
	}

	Casa getCasa() {
		return casa;
	}

	void mover(Casa novaCasa) {
		this.casa = novaCasa;
	}
}
