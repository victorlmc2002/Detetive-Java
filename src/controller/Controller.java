package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import model.EventoJogo;
import model.Fachada;
import model.Posicao;

// Singleton. É o Controller do jogo: é ele que cuida da ordem dos eventos
// dentro de uma partida.
//
// A máquina de estados do turno fica aqui. A View só
// repassa os comandos do usuário (cliques de botão e do tabuleiro) pra cá, o
// Controller valida a transição e chama a Fachada. A Fachada avisa os
// observadores, e é assim que a tela se atualiza.
public class Controller {

	// Os estados por que um turno passa.
	public enum EstadoTurno {
		AGUARDANDO_DADO,       // jogador ainda não lançou os dados
		AGUARDANDO_MOVIMENTO,  // dados lançados, espera o clique no destino
		FIM_TURNO,             // peão movido, espera ação ou "Próximo Jogador"
		FIM_DE_JOGO            // partida acabou (vitória ou derrota)
	}

	private static Controller instancia;

	private final Fachada fachada = Fachada.getInstance();
	private EstadoTurno estado = EstadoTurno.AGUARDANDO_DADO;
	// Marca se o jogador da vez já palpitou neste turno.
	private boolean palpiteFeito = false;

	private Controller() {
	}

	public static Controller getInstance() {
		if (instancia == null) {
			instancia = new Controller();
		}
		return instancia;
	}

	// A View usa o estado pra habilitar/desabilitar botões e desenhar destaques.
	public EstadoTurno getEstado() {
		return estado;
	}

	// Começa uma partida nova e reinicia o ciclo do turno.
	public void iniciarPartida(List<String> nomes) {
		estado = EstadoTurno.AGUARDANDO_DADO;
		palpiteFeito = false;
		fachada.iniciarPartida(nomes);
	}

	// Mesma coisa, mas recebendo também os suspeitos escolhidos (listas paralelas).
	public void iniciarPartida(List<String> nomes, List<String> suspeitos) {
		estado = EstadoTurno.AGUARDANDO_DADO;
		palpiteFeito = false;
		fachada.iniciarPartida(nomes, suspeitos);
	}

	// Joga os dois dados aleatoriamente.
	public void lancarDados() {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		estado = EstadoTurno.AGUARDANDO_MOVIMENTO;
		fachada.lancarDados();
	}

	// Fixa os valores dos dados na mão (modo de teste), sem sortear.
	public void definirDados(int d1, int d2) {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		estado = EstadoTurno.AGUARDANDO_MOVIMENTO;
		fachada.definirDados(d1, d2);
	}

	// Tenta mover o peão da vez para (linha, coluna). Só anda se o destino estiver
	// entre as casas alcançáveis, senão, ignora o clique.
	public void tentarMover(int linha, int coluna) {
		if (estado != EstadoTurno.AGUARDANDO_MOVIMENTO) return;
		Posicao destino = new Posicao(linha, coluna);
		if (fachada.casasAlcancaveis(fachada.getTotalDados()).contains(destino)) {
			estado = EstadoTurno.FIM_TURNO;
			fachada.deslocarPiao(destino);
		}
	}

	// Passa a vez pro próximo jogador e reinicia o ciclo do turno.
	public void avancarTurno() {
		if (estado != EstadoTurno.FIM_TURNO) return;
		palpiteFeito = false;
		estado = EstadoTurno.AGUARDANDO_DADO;
		fachada.proximoJogador();
	}

	// Faz um palpite: suspeito + arma no cômodo atual (a Fachada descobre o cômodo).
	// Só rola em FIM_TURNO, com o jogador dentro de um cômodo, e uma vez por turno.
	// Volta "NomeJogador|NomeCarta" se alguém refutou, ou null.
	public String fazerPalpite(String suspeito, String arma) {
		if (estado != EstadoTurno.FIM_TURNO) return null;
		if (palpiteFeito) return null;
		if (!fachada.estaEmComodo()) return null;
		palpiteFeito = true;
		return fachada.fazerPalpite(suspeito, arma);
	}

	// Diz se dá pra palpitar agora.
	public boolean podeFazerPalpite() {
		return estado == EstadoTurno.FIM_TURNO
			&& !palpiteFeito
			&& fachada.partidaIniciada()
			&& fachada.estaEmComodo();
	}

	// Faz uma acusação formal. Pode ser feita em qualquer estado ativo.
	// Retorna true se acertou (jogo termina). Retorna false se errou (eliminado).
	public boolean fazerAcusacao(String suspeito, String arma, String comodo) {
		if (estado == EstadoTurno.FIM_DE_JOGO) return false;
		if (!fachada.partidaIniciada()) return false;
		boolean venceu = fachada.fazerAcusacao(suspeito, arma, comodo);
		if (venceu) {
			estado = EstadoTurno.FIM_DE_JOGO;
		} else {
			// Jogador eliminado: avança o turno automaticamente.
			palpiteFeito = false;
			estado = EstadoTurno.AGUARDANDO_DADO;
			fachada.proximoJogador();
		}
		return venceu;
	}

	// ===================== Salvamento e Recuperação =======================

	// O salvamento só é permitido imediatamente antes do lançamento dos dados,
	// isto é, no início do turno de um jogador (estado AGUARDANDO_DADO). Após o
	// lançamento o botão é desabilitado, reabilitando apenas no próximo turno.
	public boolean podeSalvar() {
		return fachada.partidaIniciada() && estado == EstadoTurno.AGUARDANDO_DADO;
	}

	// Grava o estado da partida no arquivo escolhido pelo usuário.
	public void salvarPartida(File arquivo) throws IOException {
		if (!podeSalvar()) return;
		fachada.salvarPartida(arquivo);
	}

	// Recupera uma partida do arquivo escolhido e reinicia o ciclo do turno.
	public void carregarPartida(File arquivo) throws IOException {
		fachada.carregarPartida(arquivo);
		palpiteFeito = false;
		estado = EstadoTurno.AGUARDANDO_DADO;
	}

	// Dispara o evento de exibição das cartas do jogador da vez.
	public void exibirCartas() {
		if (!fachada.partidaIniciada()) return;
		fachada.notificarObservadores(EventoJogo.EXIBIR_CARTAS);
	}

	// Dispara o evento de exibição do bloco de notas do jogador da vez.
	public void exibirBlocoNotas() {
		if (!fachada.partidaIniciada()) return;
		fachada.notificarObservadores(EventoJogo.EXIBIR_BLOCO_NOTAS);
	}
}