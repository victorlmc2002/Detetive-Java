package model;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

public class EnvelopeTest {

	@Test
	public void envelopeGuardaUmaCartaDeCadaTipo() {
		Carta s = new Carta(TipoCarta.SUSPEITO, "Srta. Scarlet");
		Carta a = new Carta(TipoCarta.ARMA, "Faca");
		Carta c = new Carta(TipoCarta.COMODO, "Cozinha");

		Envelope env = new Envelope(s, a, c);

		assertEquals(s, env.getSuspeito());
		assertEquals(a, env.getArma());
		assertEquals(c, env.getComodo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void construtorRejeitaCartaDeTipoErradoNoSuspeito() {
		Carta cartaArma = new Carta(TipoCarta.ARMA, "Faca");
		Carta arma = new Carta(TipoCarta.ARMA, "Revólver");
		Carta comodo = new Carta(TipoCarta.COMODO, "Cozinha");
		new Envelope(cartaArma, arma, comodo); // deve lançar exceção
	}

	@Test(expected = IllegalArgumentException.class)
	public void construtorRejeitaNull() {
		new Envelope(null, null, null);
	}

	@Test
	public void contemReconheceCartasDoEnvelope() {
		Carta s = new Carta(TipoCarta.SUSPEITO, "Sra. Peacock");
		Carta a = new Carta(TipoCarta.ARMA, "Castiçal");
		Carta c = new Carta(TipoCarta.COMODO, "Biblioteca");
		Envelope env = new Envelope(s, a, c);

		assertTrue(env.contem(s));
		assertTrue(env.contem(a));
		assertTrue(env.contem(c));
	}

	@Test
	public void contemRetornaFalsoParaCartaForaDoEnvelope() {
		Envelope env = new Envelope(
			new Carta(TipoCarta.SUSPEITO, "Sra. Peacock"),
			new Carta(TipoCarta.ARMA, "Castiçal"),
			new Carta(TipoCarta.COMODO, "Biblioteca")
		);
		Carta foraDoEnvelope = new Carta(TipoCarta.ARMA, "Faca");
		assertFalse(env.contem(foraDoEnvelope));
	}

	@Test
	public void sortearRetiraTresCartasDoBaralho() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		assertEquals(21, baralho.size());

		Envelope.sortear(baralho, new Random());

		assertEquals(18, baralho.size());
	}

	@Test
	public void sortearProduzUmaCartaDeCadaTipo() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		Envelope env = Envelope.sortear(baralho, new Random());

		assertEquals(TipoCarta.SUSPEITO, env.getSuspeito().getTipo());
		assertEquals(TipoCarta.ARMA, env.getArma().getTipo());
		assertEquals(TipoCarta.COMODO, env.getComodo().getTipo());
	}

	@Test
	public void cartasDoEnvelopeNaoEstaoMaisNoBaralho() {
		List<Carta> baralho = Carta.criarBaralhoCompleto();
		Envelope env = Envelope.sortear(baralho, new Random());

		assertFalse(baralho.contains(env.getSuspeito()));
		assertFalse(baralho.contains(env.getArma()));
		assertFalse(baralho.contains(env.getComodo()));
	}

	@Test
	public void mesmaSementeProduzMesmoEnvelope() {
		List<Carta> baralho1 = Carta.criarBaralhoCompleto();
		List<Carta> baralho2 = Carta.criarBaralhoCompleto();

		Envelope a = Envelope.sortear(baralho1, new Random(42L));
		Envelope b = Envelope.sortear(baralho2, new Random(42L));

		assertEquals(a.getSuspeito(), b.getSuspeito());
		assertEquals(a.getArma(), b.getArma());
		assertEquals(a.getComodo(), b.getComodo());
	}
}
