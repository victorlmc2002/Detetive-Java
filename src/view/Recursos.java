package view;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

// Carregador único de imagens e cores compartilhadas entre as telas.
// Deve ser inicializado UMA vez no startup chamando Recursos.carregar().
// Depois disso, imagem(...) e cor(...) apenas leem o cache em memória.
public class Recursos {

	public static final String SCARLET  = "Srta. Scarlet";
	public static final String MUSTARD  = "Coronel Mustard";
	public static final String WHITE    = "Sra. White";
	public static final String GREEN    = "Reverendo Green";
	public static final String PEACOCK  = "Sra. Peacock";
	public static final String PLUM     = "Professor Plum";

	public static final String[] SUSPEITOS = {
		SCARLET, MUSTARD, WHITE, GREEN, PEACOCK, PLUM
	};

	private static final String BASE = "assets/";

	private static final Map<String, BufferedImage> imagens = new HashMap<>();
	private static final Map<String, Color> cores = new HashMap<>();
	private static boolean carregado = false;

	static {
		cores.put(SCARLET, new Color(220, 30, 30));
		cores.put(MUSTARD, new Color(225, 190, 40));
		cores.put(WHITE,   new Color(245, 245, 245));
		cores.put(GREEN,   new Color(40, 140, 60));
		cores.put(PEACOCK, new Color(40, 80, 180));
		cores.put(PLUM,    new Color(130, 50, 160));
	}

	private Recursos() {}

	// Lê todos os arquivos uma única vez. Chamadas seguintes são no-op.
	public static void carregar() {
		if (carregado) return;
		try {
			imagens.put("tabuleiro", lerImagem(BASE + "Tabuleiros/Tabuleiro-Clue-C.jpg"));
			for (int i = 1; i <= 6; i++) {
				imagens.put("dado" + i, lerImagem(BASE + "Tabuleiros/dado" + i + ".jpg"));
			}
			imagens.put(SCARLET, lerImagem(BASE + "Suspeitos/Scarlet.jpg"));
			imagens.put(MUSTARD, lerImagem(BASE + "Suspeitos/Mustard.jpg"));
			imagens.put(WHITE,   lerImagem(BASE + "Suspeitos/White.jpg"));
			imagens.put(GREEN,   lerImagem(BASE + "Suspeitos/Green.jpg"));
			imagens.put(PEACOCK, lerImagem(BASE + "Suspeitos/Peacock.jpg"));
			imagens.put(PLUM,    lerImagem(BASE + "Suspeitos/Plum.jpg"));

			// Cartas de armas - indexadas pelo nome EXATO usado no model (Carta).
			imagens.put("Corda",          lerImagem(BASE + "Armas/Corda.jpg"));
			imagens.put("Cano de Chumbo", lerImagem(BASE + "Armas/Cano.jpg"));
			imagens.put("Faca",           lerImagem(BASE + "Armas/Faca.jpg"));
			imagens.put("Chave Inglesa",  lerImagem(BASE + "Armas/ChaveInglesa.jpg"));
			imagens.put("Castiçal",       lerImagem(BASE + "Armas/Castical.jpg"));
			imagens.put("Revólver",       lerImagem(BASE + "Armas/Revolver.jpg"));

			// Cartas de cômodos - indexadas pelo nome EXATO usado no model (Carta).
			imagens.put("Cozinha",           lerImagem(BASE + "Comodos/Cozinha.jpg"));
			imagens.put("Sala de Música",    lerImagem(BASE + "Comodos/SalaDeMusica.jpg"));
			imagens.put("Jardim de Inverno", lerImagem(BASE + "Comodos/JardimInverno.jpg"));
			imagens.put("Sala de Jantar",    lerImagem(BASE + "Comodos/SalaDeJantar.jpg"));
			imagens.put("Salão de Jogos",    lerImagem(BASE + "Comodos/SalaoDeJogos.jpg"));
			imagens.put("Biblioteca",        lerImagem(BASE + "Comodos/Biblioteca.jpg"));
			imagens.put("Sala de Estar",     lerImagem(BASE + "Comodos/SalaDeEstar.jpg"));
			imagens.put("Entrada",           lerImagem(BASE + "Comodos/Entrada.jpg"));
			imagens.put("Escritório",        lerImagem(BASE + "Comodos/Escritorio.jpg"));

			carregado = true;
		} catch (IOException e) {
			throw new RuntimeException("Falha ao carregar imagens: " + e.getMessage(), e);
		}
	}

	private static BufferedImage lerImagem(String caminho) throws IOException {
		File f = new File(caminho);
		if (!f.exists()) {
			throw new IOException("Imagem não encontrada: " + f.getAbsolutePath());
		}
		return ImageIO.read(f);
	}

	public static BufferedImage imagem(String chave) {
		return imagens.get(chave);
	}

	public static BufferedImage tabuleiro() {
		return imagens.get("tabuleiro");
	}

	public static BufferedImage dado(int valor) {
		return imagens.get("dado" + valor);
	}

	public static BufferedImage cartaSuspeito(String suspeito) {
		return imagens.get(suspeito);
	}

	// Imagem de qualquer carta (suspeito, arma ou cômodo) pelo nome usado no model.
	// Retorna null se não houver imagem cadastrada para o nome informado.
	public static BufferedImage cartaImagem(String nomeCarta) {
		return imagens.get(nomeCarta);
	}

	public static Color cor(String suspeito) {
		return cores.get(suspeito);
	}
}
