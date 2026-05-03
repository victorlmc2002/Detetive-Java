package model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DadoTest {

	@Test
	public void valorSempreEntreUmESeis() {
		Dado d = new Dado();
		for (int i = 0; i < 1000; i++) {
			int v = d.lancar();
			assertTrue("valor fora da faixa: " + v, v >= 1 && v <= 6);
		}
	}

	@Test
	public void todosOsSeisValoresAparecemEm1000Lancamentos() {
		Dado d = new Dado();
		Set<Integer> vistos = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			vistos.add(d.lancar());
		}
		assertEquals(Set.of(1, 2, 3, 4, 5, 6), vistos);
	}

	@Test
	public void mesmaSementeProduzMesmaSequencia() {
		Dado a = new Dado(123L);
		Dado b = new Dado(123L);
		for (int i = 0; i < 50; i++) {
			assertEquals(a.lancar(), b.lancar());
		}
	}
}
