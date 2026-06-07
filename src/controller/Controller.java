package controller;

import java.util.List;

import model.EventoJogo;
import model.Fachada;
import model.Posicao;

// Padrão SINGLETON. Consolida o papel do CONTROLLER (objetivo da 3ª iteração):
// é o responsável por ORGANIZAR A SEQUÊNCIA DE EVENTOS de uma partida de Clue.
//
// A máquina de estados do turno mora aqui (antes estava dentro da View). A View
// apenas envia comandos (cliques de botão e do tabuleiro) ao Controller; este
// valida a transição de estado e aciona a Fachada. A Fachada, por sua vez,
// notifica os observadores (Observer) — e é assim que a interface se atualiza.
public class Controller {

	// Estados pelos quais um turno passa.
	public enum EstadoTurno {
		AGUARDANDO_DADO,       // jogador ainda não lançou os dados
		AGUARDANDO_MOVIMENTO,  // dados lançados; aguarda clique no destino
		FIM_TURNO,             // peão movido; aguarda ação ou "Próximo Jogador"
		FIM_DE_JOGO            // partida encerrada (vitória ou derrota definitiva)
	}

	private static Controller instancia;

	private final Fachada fachada = Fachada.getInstance();
	private EstadoTurno estado = EstadoTurno.AGUARDANDO_DADO;
	// Controla se o jogador da vez já fez palpite neste turno.
	private boolean palpiteFeito = false;

	private Controller() {
	}

	public static Controller getInstance() {
		if (instancia == null) {
			instancia = new Controller();
		}
		return instancia;
	}

	// A View lê o estado para habilitar/desabilitar botões e desenhar destaques.
	public EstadoTurno getEstado() {
		return estado;
	}

	// Inicia uma nova partida e reinicia o ciclo do turno.
	public void iniciarPartida(List<String> nomes) {
		estado = EstadoTurno.AGUARDANDO_DADO;
		fachada.iniciarPartida(nomes);
	}

	// Lança os dois dados de forma aleatória.
	public void lancarDados() {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		estado = EstadoTurno.AGUARDANDO_MOVIMENTO;
		fachada.lancarDados();
	}

	// Define os valores dos dados (modo testador) em vez de randomizar.
	public void definirDados(int d1, int d2) {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		estado = EstadoTurno.AGUARDANDO_MOVIMENTO;
		fachada.definirDados(d1, d2);
	}

	// Tenta mover o peão da vez para (linha, coluna). Só efetiva se o destino
	// estiver entre as casas alcançáveis; caso contrário, ignora (regra do jogo).
	public void tentarMover(int linha, int coluna) {
		if (estado != EstadoTurno.AGUARDANDO_MOVIMENTO) return;
		Posicao destino = new Posicao(linha, coluna);
		if (fachada.casasAlcancaveis(fachada.getTotalDados()).contains(destino)) {
			estado = EstadoTurno.FIM_TURNO;
			fachada.deslocarPiao(destino);
		}
		// Destino inválido: silêncio total, o estado não muda (conforme regra).
	}

	// Passa a vez para o próximo jogador e reinicia o ciclo do turno.
	public void avancarTurno() {
		if (estado != EstadoTurno.FIM_TURNO) return;
		palpiteFeito = false;
		estado = EstadoTurno.AGUARDANDO_DADO;
		fachada.proximoJogador();
	}

	// Faz um palpite: suspeito + arma no cômodo atual (inferido pela Fachada).
	// Só permitido em FIM_TURNO, quando o jogador está em um cômodo,
	// e apenas uma vez por turno.
	// Retorna "NomeJogador|NomeCarta" se alguém refutou, ou null.
	public String fazerPalpite(String suspeito, String arma) {
		if (estado != EstadoTurno.FIM_TURNO) return null;
		if (palpiteFeito) return null;
		if (!fachada.estaEmComodo()) return null;
		palpiteFeito = true;
		return fachada.fazerPalpite(suspeito, arma);
	}

	// Verifica se o palpite pode ser feito agora.
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

	// Dispara o evento de exibição das cartas do jogador da vez.
	public void exibirCartas() {
		if (!fachada.partidaIniciada()) return;
		fachada.notificarObservadores(EventoJogo.EXIBIR_CARTAS);
	}

	// Dispara o evento de exibição das anotações de palpites.
	public void exibirAnotacoes() {
		if (!fachada.partidaIniciada()) return;
		fachada.notificarObservadores(EventoJogo.EXIBIR_ANOTACOES);
	}
}