package model;

class Casa {
	private final int linha;
	private final int coluna;
	private final TipoCasa tipo;
	private Comodo comodo;

	Casa(int linha, int coluna, TipoCasa tipo) {
		this.linha = linha;
		this.coluna = coluna;
		this.tipo = tipo;
	}

	int getLinha() {
		return linha;
	}

	int getColuna() {
		return coluna;
	}

	TipoCasa getTipo() {
		return tipo;
	}

	Comodo getComodo() {
		return comodo;
	}

	void setComodo(Comodo comodo) {
		this.comodo = comodo;
	}

	boolean ehCorredor() {
		return tipo == TipoCasa.CORREDOR;
	}

	boolean ehInterior() {
		return tipo == TipoCasa.COMODO_INTERIOR;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Casa)) return false;
		Casa outra = (Casa) o;
		return linha == outra.linha && coluna == outra.coluna;
	}

	@Override
	public int hashCode() {
		return linha * 1000 + coluna;
	}

	@Override
	public String toString() {
		return "(" + linha + "," + coluna + ")";
	}
}
