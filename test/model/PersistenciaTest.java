package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

// Testa o salvamento e a recuperação do estado de uma partida em arquivo texto
// (4ª iteração). Semente fixa garante determinismo do baralho/envelope.
public class PersistenciaTest {

	private static final List<String> NOMES =
		Arrays.asList("Ana", "Bruno", "Carlos", "Duda");

	private Jogo novaPartida() {
		Jogo jogo = new Jogo(99L);
		jogo.iniciarPartida(NOMES);
		return jogo;
	}

	private File arquivoTemp() throws IOException {
		File f = File.createTempFile("detetive-save", ".txt");
		f.deleteOnExit();
		return f;
	}

	@Test
	public void salvarECarregarPreservaOEstadoCompleto() throws IOException {
		Jogo original = novaPartida();
		original.definirDados(3, 5);   // valores de dado conhecidos
		original.proximoJogador();     // vez passa para o índice 1

		File arquivo = arquivoTemp();
		Persistencia.salvar(arquivo, original);
		Jogo carregado = Persistencia.carregar(arquivo);

		// Vez e dados.
		assertEquals(original.getIndiceVez(), carregado.getIndiceVez());
		assertEquals(original.getDado1(), carregado.getDado1());
		assertEquals(original.getDado2(), carregado.getDado2());

		// Envelope confidencial.
		assertEquals(original.getEnvelope().getSuspeito().getNome(),
		             carregado.getEnvelope().getSuspeito().getNome());
		assertEquals(original.getEnvelope().getArma().getNome(),
		             carregado.getEnvelope().getArma().getNome());
		assertEquals(original.getEnvelope().getComodo().getNome(),
		             carregado.getEnvelope().getComodo().getNome());

		// Jogadores: quantidade, mãos e posição do peão da vez.
		assertEquals(original.totalDeJogadores(), carregado.totalDeJogadores());
		for (String nome : NOMES) {
			assertEquals("mão de " + nome,
				original.cartasDoJogador(nome), carregado.cartasDoJogador(nome));
		}
		assertEquals(original.posicaoDoPiaoDaVez(), carregado.posicaoDoPiaoDaVez());
	}

	@Test
	public void posicaoDeUmPiaoMovidoEhPreservada() throws IOException {
		Jogo original = novaPartida();
		// Move o peão da vez (Scarlet, em 24,7) para uma casa alcançável.
		Posicao destino = new Posicao(23, 7);
		assertTrue(original.casasAlcancaveis(1).contains(destino));
		original.deslocarPiao(destino);

		File arquivo = arquivoTemp();
		Persistencia.salvar(arquivo, original);
		Jogo carregado = Persistencia.carregar(arquivo);

		assertEquals(destino, carregado.posicaoDoPiaoDaVez());
	}

	@Test
	public void jogadorEliminadoEhPreservado() throws IOException {
		Jogo original = novaPartida();
		original.eliminarJogadorDaVez(); // elimina o jogador de índice 0

		File arquivo = arquivoTemp();
		Persistencia.salvar(arquivo, original);
		Jogo carregado = Persistencia.carregar(arquivo);

		assertTrue(carregado.getEliminados().contains(0));
	}

	@Test(expected = IOException.class)
	public void arquivoInvalidoLancaIOException() throws IOException {
		File arquivo = arquivoTemp();
		Files.write(arquivo.toPath(), "conteudo qualquer\n".getBytes());
		Persistencia.carregar(arquivo);
	}
}
