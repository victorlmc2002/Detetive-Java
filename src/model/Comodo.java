package model;

import java.util.LinkedList;
import java.util.List;

class Comodo {
	private final String nome;
	private final List<Casa> interior = new LinkedList<>();
	private final List<Casa> portas = new LinkedList<>();

	Comodo(String nome) {
		this.nome = nome;
	}

	String getNome() {
		return nome;
	}

	void adicionarInterior(Casa casa) {
		interior.add(casa);
		casa.setComodo(this);
	}

	void adicionarPorta(Casa casaCorredor) {
		portas.add(casaCorredor);
	}

	List<Casa> getInterior() {
		return interior;
	}

	List<Casa> getPortas() {
		return portas;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Comodo)) return false;
		return nome.equals(((Comodo) o).nome);
	}

	@Override
	public int hashCode() {
		return nome.hashCode();
	}

	@Override
	public String toString() {
		return nome;
	}
}
