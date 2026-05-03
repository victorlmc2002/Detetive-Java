package model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TabuleiroTest {

	private Tabuleiro tab;

	@Before
	public void setUp() {
		tab = new Tabuleiro();
	}

	@Test
	public void noveComodosEstaoRegistrados() {
		assertEquals(9, tab.getComodos().size());
	}

	@Test
	public void cadaComodoTemPeloMenosUmaPorta() {
		for (Comodo c : tab.getComodos().values()) {
			assertFalse("cômodo " + c + " sem porta", c.getPortas().isEmpty());
		}
	}

	@Test
	public void portasSaoCasasDeCorredor() {
		for (Comodo c : tab.getComodos().values()) {
			for (Casa porta : c.getPortas()) {
				assertEquals(
					"porta de " + c + " em " + porta + " deveria ser CORREDOR",
					TipoCasa.CORREDOR,
					porta.getTipo()
				);
			}
		}
	}

	@Test
	public void todosOsSeisSuspeitosTemCasaInicial() {
		String[] suspeitos = {
			"Srta. Scarlet", "Coronel Mustard", "Sra. White",
			"Reverendo Green", "Sra. Peacock", "Professor Plum"
		};
		for (String s : suspeitos) {
			assertNotNull("sem casa inicial: " + s, tab.getCasaInicial(s));
		}
	}

	@Test
	public void mapearComZeroPassosNaoVaiAlugarLugar() {
		Casa origem = tab.getCasaInicial("Srta. Scarlet");
		Set<Casa> alvos = tab.mapearCasas(origem, 0, new HashSet<>());
		assertTrue(alvos.isEmpty());
	}

	@Test
	public void mapearUmPassoRetornaApenasVizinhosDeCorredor() {
		Casa origem = tab.getCasaInicial("Srta. Scarlet"); // (24, 7)
		Set<Casa> alvos = tab.mapearCasas(origem, 1, new HashSet<>());
		// (24, 6) e (23, 7) são corredor adjacentes; (25, 7) está fora;
		// (24, 8) é interior da Entrada e só seria alcançável via porta.
		assertTrue(alvos.contains(tab.getCasa(24, 6)));
		assertTrue(alvos.contains(tab.getCasa(23, 7)));
		assertFalse(
			"interior de cômodo não é vizinho direto sem porta",
			alvos.contains(tab.getCasa(24, 8))
		);
	}

	@Test
	public void naoAtravessaCasaInacessivel() {
		// CLUE central: rows 8-12, cols 10-14, todas inacessíveis.
		Casa central = tab.getCasa(10, 12);
		assertEquals(TipoCasa.INACESSIVEL, central.getTipo());

		Casa origem = tab.getCasa(7, 12); // corredor logo acima
		Set<Casa> alvos = tab.mapearCasas(origem, 5, new HashSet<>());
		assertFalse("não pode entrar no espaço inacessível", alvos.contains(central));
	}

	@Test
	public void portaDeComodoTornaInteriorAlcancavel() {
		// (17, 9) é porta da Entrada. A 1 passo de (17, 9) deveria estar
		// alcançável o interior da Entrada (cells em (18..24, 8..14)).
		Casa origem = tab.getCasa(17, 9);
		Set<Casa> alvos = tab.mapearCasas(origem, 1, new HashSet<>());
		Casa interior = tab.getCasa(18, 9);
		assertEquals(TipoCasa.COMODO_INTERIOR, interior.getTipo());
		assertTrue("interior da Entrada deve estar alcançável", alvos.contains(interior));
	}

	@Test
	public void casaBloqueadaNaoApareceNaBusca() {
		Casa origem = tab.getCasaInicial("Srta. Scarlet"); // (24, 7)
		Casa bloq = tab.getCasa(24, 8);
		Set<Casa> bloqueadas = new HashSet<>();
		bloqueadas.add(bloq);
		Set<Casa> alvos = tab.mapearCasas(origem, 5, bloqueadas);
		assertFalse(alvos.contains(bloq));
	}
}
