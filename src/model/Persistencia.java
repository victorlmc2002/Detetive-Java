package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Salvamento e recuperação do estado de uma partida em arquivo texto ASCII puro
// (4ª iteração — manipulação de streams de E/S, Cap. 16).
//
// É package-private: a View/Controller só acionam a persistência por meio da
// Fachada (Façade), que delega a esta classe.
//
// FORMATO DO ARQUIVO (uma informação por linha; campos separados por '|'):
//   DETETIVE_SAVE_V1                              <- cabeçalho/identificação
//   NUM_JOGADORES=<n>                             <- quantidade de jogadores (3 a 6)
//   VEZ=<i>                                       <- índice (0-based) do jogador da vez
//   DADO1=<v>                                     <- valor do dado 1 (0 = ainda não lançado)
//   DADO2=<v>                                     <- valor do dado 2 (0 = ainda não lançado)
//   ENVELOPE=<suspeito>|<arma>|<cômodo>           <- cartas do envelope confidencial
//   (para CADA jogador, na ordem de jogada, DUAS linhas:)
//   JOGADOR=<nome>|<suspeito>|<linha>|<coluna>|<eliminado>
//   MAO=<carta1>|<carta2>|...                     <- cartas na mão (pode ser vazia)
//
// Como a opção de salvar só fica disponível imediatamente antes do lançamento
// dos dados, ao recuperar uma partida o turno reinicia em "aguardando dado".
class Persistencia {

	private static final String CABECALHO = "DETETIVE_SAVE_V1";

	private Persistencia() {
	}

	// Grava o estado atual da partida no arquivo informado (texto, UTF-8).
	// Encadeamento de streams (Cap. 16): arquivo -> bytes -> caracteres -> buffer.
	static void salvar(File arquivo, Jogo jogo) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
			List<Jogador> jogadores = jogo.getJogadores();
			Set<Integer> eliminados = jogo.getEliminados();
			Envelope env = jogo.getEnvelope();

			escrever(bw, CABECALHO);
			escrever(bw, "NUM_JOGADORES=" + jogadores.size());
			escrever(bw, "VEZ=" + jogo.getIndiceVez());
			escrever(bw, "DADO1=" + jogo.getDado1());
			escrever(bw, "DADO2=" + jogo.getDado2());
			escrever(bw, "ENVELOPE=" + env.getSuspeito().getNome()
					+ "|" + env.getArma().getNome()
					+ "|" + env.getComodo().getNome());

			for (int i = 0; i < jogadores.size(); i++) {
				Jogador j = jogadores.get(i);
				Casa casa = j.getPeao().getCasa();
				escrever(bw, "JOGADOR=" + j.getNome()
						+ "|" + j.getPeao().getSuspeito()
						+ "|" + casa.getLinha()
						+ "|" + casa.getColuna()
						+ "|" + eliminados.contains(i));

				StringBuilder mao = new StringBuilder("MAO=");
				List<Carta> cartas = j.getMao();
				for (int k = 0; k < cartas.size(); k++) {
					if (k > 0) mao.append("|");
					mao.append(cartas.get(k).getNome());
				}
				escrever(bw, mao.toString());
			}
		}
	}

	// Lê o estado de uma partida do arquivo informado (texto, UTF-8) e devolve
	// um novo Jogo. Encadeamento de streams: arquivo -> bytes -> caracteres -> buffer.
	static Jogo carregar(File arquivo) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
			String cabecalho = br.readLine();
			if (cabecalho == null || !cabecalho.trim().equals(CABECALHO)) {
				throw new IOException("Arquivo de salvamento inválido ou corrompido");
			}

			int numJogadores = Integer.parseInt(valor(br.readLine()));
			int indiceVez = Integer.parseInt(valor(br.readLine()));
			int d1 = Integer.parseInt(valor(br.readLine()));
			int d2 = Integer.parseInt(valor(br.readLine()));

			String[] envp = dividir(valor(br.readLine()));
			Envelope envelope = new Envelope(
					Carta.porNome(envp[0]), Carta.porNome(envp[1]), Carta.porNome(envp[2]));

			Jogo jogo = new Jogo();
			List<Jogador> jogadores = new ArrayList<>();
			Set<Integer> eliminados = new HashSet<>();

			for (int i = 0; i < numJogadores; i++) {
				String[] jp = dividir(valor(br.readLine()));
				String nome = jp[0];
				String suspeito = jp[1];
				int linha = Integer.parseInt(jp[2]);
				int coluna = Integer.parseInt(jp[3]);
				boolean eliminado = Boolean.parseBoolean(jp[4]);

				Peao peao = new Peao(suspeito, jogo.casaEm(linha, coluna));
				Jogador jogador = new Jogador(nome, peao);

				String mao = valor(br.readLine());
				if (!mao.isEmpty()) {
					for (String nomeCarta : dividir(mao)) {
						if (!nomeCarta.isEmpty()) {
							jogador.receberCarta(Carta.porNome(nomeCarta));
						}
					}
				}

				jogadores.add(jogador);
				if (eliminado) eliminados.add(i);
			}

			jogo.carregarEstado(jogadores, envelope, indiceVez, d1, d2, eliminados);
			return jogo;
		} catch (NumberFormatException e) {
			throw new IOException("Arquivo de salvamento com número inválido: " + e.getMessage());
		}
	}

	// ---- Auxiliares de E/S ----

	private static void escrever(BufferedWriter bw, String linha) throws IOException {
		bw.write(linha);
		bw.newLine();
	}

	// Extrai o conteúdo após o '=' de uma linha "CHAVE=valor".
	private static String valor(String linha) throws IOException {
		if (linha == null) {
			throw new IOException("Arquivo de salvamento incompleto");
		}
		int idx = linha.indexOf('=');
		return idx < 0 ? linha.trim() : linha.substring(idx + 1).trim();
	}

	// Divide um valor multi-campo pelo separador '|' preservando campos vazios.
	private static String[] dividir(String valor) {
		return valor.split("\\|", -1);
	}
}
