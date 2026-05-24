package model;

// empacota uma coordenada (linha, coluna) do tabuleiro.
public final class Posicao {

	private final int linha;
	private final int coluna;

	public Posicao(int linha, int coluna) {
		this.linha = linha;
		this.coluna = coluna;
	}

	public int getLinha() {
		return linha;
	}

	public int getColuna() {
		return coluna;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Posicao)) return false;
		Posicao outra = (Posicao) o;
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
