package model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CartaTest {

	@Test
	public void cartaGuardaTipoENomeCorretamente() {
		Carta c = new Carta(TipoCarta.ARMA, "Faca");
		assertEquals(TipoCarta.ARMA, c.getTipo());
		assertEquals("Faca", c.getNome());
	}

	@Test
	public void duasCartasComMesmoTipoENomeSaoIguais() {
		Carta a = new Carta(TipoCarta.SUSPEITO, "Srta. Scarlet");
		Carta b = new Carta(TipoCarta.SUSPEITO, "Srta. Scarlet");
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void cartasComNomesDiferentesSaoDiferentes() {
		Carta a = new Carta(TipoCarta.ARMA, "Faca");
		Carta b = new Carta(TipoCarta.ARMA, "Revólver");
		assertNotEquals(a, b);
	}

	@Test
	public void baralhoCompletoTemVinteEUmaCartas() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		assertEquals(21, baralho.size());
	}

	@Test
	public void baralhoCompletoTemSeisSuspeitosSeisArmasNoveComodos() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		int suspeitos = 0, armas = 0, comodos = 0;
		for (Carta c : baralho) {
			if (c.getTipo() == TipoCarta.SUSPEITO) suspeitos++;
			else if (c.getTipo() == TipoCarta.ARMA) armas++;
			else if (c.getTipo() == TipoCarta.COMODO) comodos++;
		}
		assertEquals(6, suspeitos);
		assertEquals(6, armas);
		assertEquals(9, comodos);
	}

	@Test
	public void baralhoCompletoNaoTemCartasRepetidas() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		Set<Carta> distintas = new HashSet<>(baralho);
		assertEquals(baralho.size(), distintas.size());
	}
}
