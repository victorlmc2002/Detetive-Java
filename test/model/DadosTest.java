package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

// Cobre o requisito da 3a iteracao: os valores dos dados podem ser DEFINIDOS
// pelo testador, em vez de obtidos por randomizacao. Testa o agregado Jogo
// diretamente (mesmo pacote), que e o que a Fachada delega.
public class DadosTest {

	private Jogo partida() {
		Jogo j = new Jogo();
		j.iniciarPartida(Arrays.asList("A", "B", "C"));
		return j;
	}

	@Test
	public void definirDadosUsaOsValoresDoTestador() {
		Jogo j = partida();
		int total = j.definirDados(2, 5);
		assertEquals(2, j.getDado1());
		assertEquals(5, j.getDado2());
		assertEquals(7, total);
		assertEquals(7, j.passosRestantes());
	}

	@Test(expected = IllegalArgumentException.class)
	public void definirDadosRejeitaValorAbaixoDeUm() {
		partida().definirDados(0, 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void definirDadosRejeitaValorAcimaDeSeis() {
		partida().definirDados(3, 7);
	}

	@Test
	public void lancarDadosFicaEntreDoisEDoze() {
		Jogo j = partida();
		for (int i = 0; i < 300; i++) {
			int total = j.lancarDados();
			assertTrue(total >= 2 && total <= 12);
			assertTrue(j.getDado1() >= 1 && j.getDado1() <= 6);
			assertTrue(j.getDado2() >= 1 && j.getDado2() <= 6);
		}
	}
}