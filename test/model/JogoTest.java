package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class JogoTest {

	private List<String> tresJogadores() {
		return Arrays.asList("Lucas", "Ana", "Pedro");
	}

	@Test(expected = IllegalStateException.class)
	public void naoPodeLancarDadoAntesDeIniciar() {
		new Jogo().jogadorDaVez();
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejeitaPartidaComUmJogador() {
		new Jogo().iniciarPartida(Arrays.asList("Lucas"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejeitaPartidaComSeteJogadores() {
		new Jogo().iniciarPartida(Arrays.asList("a", "b", "c", "d", "e", "f", "g"));
	}

	@Test
	public void scarletEhSempreOPrimeiroAJogar() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(tresJogadores());
		assertEquals("Lucas", jogo.jogadorDaVez());
		assertEquals("Srta. Scarlet", jogo.suspeitoDaVez());
	}

	@Test
	public void proximoJogadorRotaciona() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(tresJogadores());
		assertEquals("Lucas", jogo.jogadorDaVez());
		jogo.proximoJogador();
		assertEquals("Ana", jogo.jogadorDaVez());
		jogo.proximoJogador();
		assertEquals("Pedro", jogo.jogadorDaVez());
		jogo.proximoJogador();
		assertEquals("Lucas", jogo.jogadorDaVez()); // volta pro começo
	}

	@Test
	public void totalDeJogadoresBate() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(Arrays.asList("a", "b", "c", "d"));
		assertEquals(4, jogo.totalDeJogadores());
	}

	@Test
	public void lancarDadoFicaEntreUmESeis() {
		Jogo jogo = new Jogo();
		for (int i = 0; i < 200; i++) {
			int v = jogo.lancarDado();
			assertTrue(v >= 1 && v <= 6);
		}
	}

	@Test
	public void casasAlcancaveisRespeitamPassosDoDado() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(tresJogadores());
		// Scarlet começa na (24,7).
		Set<Posicao> alvos = jogo.casasAlcancaveis(1);
		// Com 1 passo: corredor (24,6) e (23,7) são adjacentes.
		assertTrue(alvos.contains(new Posicao(24, 6)));
		assertTrue(alvos.contains(new Posicao(23, 7)));
	}

	@Test
	public void deslocarPiaoMudaPosicao() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(tresJogadores());
		Posicao destino = new Posicao(23, 7);
		assertTrue(jogo.casasAlcancaveis(1).contains(destino));

		jogo.deslocarPiao(destino);

		assertEquals(destino, jogo.posicaoDoPiaoDaVez());
	}

	@Test(expected = IllegalArgumentException.class)
	public void deslocarParaForaDoTabuleiroFalha() {
		Jogo jogo = new Jogo();
		jogo.iniciarPartida(tresJogadores());
		jogo.deslocarPiao(new Posicao(999, 999));
	}

	@Test
	public void cartasDoJogadorRetornaApenasASuaMao() {
		Jogo jogo = new Jogo(42L); // semente fixa
		jogo.iniciarPartida(tresJogadores());

		int totalCartasDistribuidas = 0;
		for (String nome : Arrays.asList("Lucas", "Ana", "Pedro")) {
			totalCartasDistribuidas += jogo.cartasDoJogador(nome).size();
		}
		assertEquals(18, totalCartasDistribuidas);
	}

	@Test
	public void nenhumaCartaDistribuidaEstaNoEnvelope() {
		Jogo jogo = new Jogo(7L);
		jogo.iniciarPartida(tresJogadores());

		Set<String> nomesNoEnvelope = new HashSet<>();
		nomesNoEnvelope.add(jogo.getEnvelope().getSuspeito().getNome());
		nomesNoEnvelope.add(jogo.getEnvelope().getArma().getNome());
		nomesNoEnvelope.add(jogo.getEnvelope().getComodo().getNome());

		for (String jog : Arrays.asList("Lucas", "Ana", "Pedro")) {
			for (String nomeCarta : jogo.cartasDoJogador(jog)) {
				assertFalse(
					"carta " + nomeCarta + " do jogador " + jog + " também está no envelope",
					nomesNoEnvelope.contains(nomeCarta)
				);
			}
		}
	}

	@Test
	public void respeitaSuspeitosEscolhidosEOrdenaPelaRegra() {
		Jogo jogo = new Jogo(5L);
		// Suspeitos fora da ordem canônica; nomes pareados por posição.
		jogo.iniciarPartida(
			Arrays.asList("Ana", "Bruno", "Carla"),
			Arrays.asList("Sra. Peacock", "Srta. Scarlet", "Sra. White"));

		// Scarlet sempre começa; o nome pareado a ela ("Bruno") deve ser a vez.
		assertEquals("Srta. Scarlet", jogo.suspeitoDaVez());
		assertEquals("Bruno", jogo.jogadorDaVez());

		// Os peões em jogo são exatamente os escolhidos (nem mais, nem menos).
		assertEquals(
			new HashSet<>(Arrays.asList("Srta. Scarlet", "Sra. White", "Sra. Peacock")),
			new HashSet<>(jogo.suspeitosEmJogo()));

		// O peão de Peacock nasce na sua casa inicial (6,23).
		assertEquals(new Posicao(6, 23), jogo.posicaoDoPiao("Sra. Peacock"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejeitaSuspeitosRepetidos() {
		new Jogo().iniciarPartida(
			Arrays.asList("A", "B", "C"),
			Arrays.asList("Srta. Scarlet", "Srta. Scarlet", "Sra. White"));
	}

	@Test
	public void mesmaSementeProduzMesmoEstadoInicial() {
		Jogo a = new Jogo(123L);
		Jogo b = new Jogo(123L);
		a.iniciarPartida(tresJogadores());
		b.iniciarPartida(tresJogadores());

		assertEquals(
			a.getEnvelope().getSuspeito().getNome(),
			b.getEnvelope().getSuspeito().getNome()
		);
		assertEquals(
			a.cartasDoJogador("Lucas"),
			b.cartasDoJogador("Lucas")
		);
	}
}
