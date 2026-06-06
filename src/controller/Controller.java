package controller;

import java.util.List;

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
		FIM_TURNO              // peão movido; aguarda "Próximo Jogador"
	}

	private static Controller instancia;

	private final Fachada fachada = Fachada.getInstance();
	private EstadoTurno estado = EstadoTurno.AGUARDANDO_DADO;

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
		estado = EstadoTurno.AGUARDANDO_DADO;
		fachada.proximoJogador();
	}
}