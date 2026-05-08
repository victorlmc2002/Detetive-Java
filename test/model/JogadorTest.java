package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class JogadorTest {

	private Tabuleiro tab;

	@Before
	public void setUp() {
		tab = new Tabuleiro();
	}

	private Peao novoPeao(String suspeito) {
		return new Peao(suspeito, tab.getCasaInicial(suspeito));
	}

	@Test
	public void jogadorGuardaNomeEPeao() {
		Peao p = novoPeao("Srta. Scarlet");
		Jogador j = new Jogador("Lucas", p);
		assertEquals("Lucas", j.getNome());
		assertSame(p, j.getPeao());
	}

	@Test
	public void maoComecaVazia() {
		Jogador j = new Jogador("Ana", novoPeao("Sra. Peacock"));
		assertTrue(j.getMao().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejeitaNomeVazio() {
		new Jogador("", novoPeao("Sra. White"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejeitaPeaoNull() {
		new Jogador("Pedro", null);
	}

	@Test
	public void receberCartaAdicionaNaMao() {
		Jogador j = new Jogador("Joana", novoPeao("Sra. White"));
		Carta faca = new Carta(TipoCarta.ARMA, "Faca");
		j.receberCarta(faca);

		assertEquals(1, j.getMao().size());
		assertTrue(j.temCarta(faca));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void maoNaoPodeSerModificadaPorFora() {
		Jogador j = new Jogador("Marcos", novoPeao("Reverendo Green"));
		j.getMao().add(new Carta(TipoCarta.ARMA, "Faca")); // deve explodir
	}

	@Test
	public void cartasQueProvamRetornaSomenteAsQueOJogadorTem() {
		Jogador j = new Jogador("Bruno", novoPeao("Coronel Mustard"));
		Carta scarlet = new Carta(TipoCarta.SUSPEITO, "Srta. Scarlet");
		Carta faca = new Carta(TipoCarta.ARMA, "Faca");
		Carta cozinha = new Carta(TipoCarta.COMODO, "Cozinha");
		j.receberCarta(scarlet);
		j.receberCarta(faca);

		List<Carta> provas = j.cartasQueProvam(scarlet, faca, cozinha);

		assertEquals(2, provas.size());
		assertTrue(provas.contains(scarlet));
		assertTrue(provas.contains(faca));
		assertFalse(provas.contains(cozinha));
	}

	@Test
	public void distribuirEsvaziaOBaralho() {
		List<Jogador> jogadores = Arrays.asList(
			new Jogador("A", novoPeao("Srta. Scarlet")),
			new Jogador("B", novoPeao("Coronel Mustard")),
			new Jogador("C", novoPeao("Sra. White"))
		);
		List<Carta> baralho = new ArrayList<>(Carta.criarBaralhoCompleto());
		Envelope.sortear(baralho, new Random(0L)); // baralho fica com 18

		Jogador.distribuirCartas(jogadores, baralho, new Random(0L));

		assertEquals(0, baralho.size());
	}

	@Test
	public void distribuirEntregaTodasAs18CartasSemPerderNenhuma() {
		List<Jogador> jogadores = Arrays.asList(
			new Jogador("A", novoPeao("Srta. Scarlet")),
			new Jogador("B", novoPeao("Coronel Mustard")),
			new Jogador("C", novoPeao("Sra. White"))
		);
		List<Carta> baralho = new ArrayList<>(Carta.criarBaralhoCompleto());
		Envelope env = Envelope.sortear(baralho, new Random(7L));
		List<Carta> esperado = new ArrayList<>(baralho); // cópia das 18

		Jogador.distribuirCartas(jogadores, baralho, new Random(7L));

		Set<Carta> recebidas = new HashSet<>();
		int total = 0;
		for (Jogador j : jogadores) {
			recebidas.addAll(j.getMao());
			total += j.getMao().size();
		}
		assertEquals(18, total);
		assertEquals(new HashSet<>(esperado), recebidas);
		// E nenhuma carta do envelope está com algum jogador.
		for (Jogador j : jogadores) {
			assertFalse(j.temCarta(env.getSuspeito()));
			assertFalse(j.temCarta(env.getArma()));
			assertFalse(j.temCarta(env.getComodo()));
		}
	}

	@Test
	public void distribuicaoEhEquilibrada() {
		// 18 cartas / 3 jogadores = 6 cartas cada.
		List<Jogador> jogadores = Arrays.asList(
			new Jogador("A", novoPeao("Srta. Scarlet")),
			new Jogador("B", novoPeao("Coronel Mustard")),
			new Jogador("C", novoPeao("Sra. White"))
		);
		List<Carta> baralho = new ArrayList<>(Carta.criarBaralhoCompleto());
		Envelope.sortear(baralho, new Random(1L));

		Jogador.distribuirCartas(jogadores, baralho, new Random(1L));

		for (Jogador j : jogadores) {
			assertEquals(6, j.getMao().size());
		}
	}

	@Test
	public void mesmaSementeProduzMesmaDistribuicao() {
		List<Jogador> jog1 = Arrays.asList(
			new Jogador("A", novoPeao("Srta. Scarlet")),
			new Jogador("B", novoPeao("Coronel Mustard")),
			new Jogador("C", novoPeao("Sra. White"))
		);
		List<Jogador> jog2 = Arrays.asList(
			new Jogador("A", novoPeao("Srta. Scarlet")),
			new Jogador("B", novoPeao("Coronel Mustard")),
			new Jogador("C", novoPeao("Sra. White"))
		);
		List<Carta> b1 = new ArrayList<>(Carta.criarBaralhoCompleto());
		List<Carta> b2 = new ArrayList<>(Carta.criarBaralhoCompleto());
		Envelope.sortear(b1, new Random(99L));
		Envelope.sortear(b2, new Random(99L));

		Jogador.distribuirCartas(jog1, b1, new Random(99L));
		Jogador.distribuirCartas(jog2, b2, new Random(99L));

		for (int i = 0; i < jog1.size(); i++) {
			assertEquals(jog1.get(i).getMao(), jog2.get(i).getMao());
		}
	}
}
