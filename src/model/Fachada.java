package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Padrões FAÇADE + SINGLETON + OBSERVER (lado Observado).
//
// É o ÚNICO ponto público do Model: a View e o Controller só conversam com a
// Fachada, nunca com Jogo/Tabuleiro/Carta diretamente. Assim, os detalhes do
// Model ficam escondidos (Façade) e há uma única instância global (Singleton).
//
// Toda mutação do estado da partida (lançar dados, mover peão, trocar a vez)
// dispara a notificação dos observadores (Observer, Cap. 17) — é por meio dela
// que a interface gráfica é atualizada para refletir a mudança de estado.
public class Fachada implements IObservado {

	private static Fachada instancia;

	private final List<IObservador> observadores = new ArrayList<>();
	private Jogo jogo;

	private Fachada() {
	}

	public static Fachada getInstance() {
		if (instancia == null) {
			instancia = new Fachada();
		}
		return instancia;
	}

	// ===================== Observer (lado Observado) =====================

	@Override
	public void adicionarObservador(IObservador o) {
		if (o != null && !observadores.contains(o)) {
			observadores.add(o);
		}
	}

	@Override
	public void removerObservador(IObservador o) {
		observadores.remove(o);
	}

	@Override
	public void notificarObservadores(Object evento) {
		// Cópia defensiva: um observador pode se registrar/remover ao ser avisado.
		for (IObservador o : new ArrayList<>(observadores)) {
			o.notificar(this, evento);
		}
	}

	// ===================== Comandos (mutam o Model e notificam) ===========

	// Cria uma nova partida do zero e avisa os observadores.
	public void iniciarPartida(List<String> nomesJogadores) {
		jogo = new Jogo();
		jogo.iniciarPartida(nomesJogadores);
		notificarObservadores(EventoJogo.PARTIDA_INICIADA);
	}

	// Cria uma nova partida com nomes E suspeitos escolhidos (listas paralelas).
	public void iniciarPartida(List<String> nomesJogadores, List<String> suspeitos) {
		jogo = new Jogo();
		jogo.iniciarPartida(nomesJogadores, suspeitos);
		notificarObservadores(EventoJogo.PARTIDA_INICIADA);
	}

	// Lança os dois dados aleatoriamente.
	public int lancarDados() {
		int total = jogo.lancarDados();
		notificarObservadores(EventoJogo.DADOS_LANCADOS);
		return total;
	}

	// Define os valores dos dados (modo testador).
	public int definirDados(int d1, int d2) {
		int total = jogo.definirDados(d1, d2);
		notificarObservadores(EventoJogo.DADOS_LANCADOS);
		return total;
	}

	// Move o peão da vez para o destino informado.
	public void deslocarPiao(Posicao destino) {
		jogo.deslocarPiao(destino);
		notificarObservadores(EventoJogo.PIAO_MOVIDO);
	}

	// Passa a vez para o próximo jogador.
	public void proximoJogador() {
		jogo.proximoJogador();
		notificarObservadores(EventoJogo.TURNO_AVANCADO);
	}

	// Faz um palpite (suspeito + arma no cômodo atual).
	// Convoca o peão do suspeito citado para o cômodo (regra do Clue) e tenta
	// obter uma refutação. Retorna "NomeJogador|NomeCarta" se alguém refutou,
	// ou null se ninguém pôde refutar.
	public String fazerPalpite(String suspeito, String arma) {
		String comodo = jogo.comodoAtual();
		jogo.moverSuspeitoParaComodoAtual(suspeito);
		String resultado = jogo.tentarRefutar(suspeito, arma, comodo);
		// O peão pode ter mudado de lugar: avisa para o tabuleiro se redesenhar.
		notificarObservadores(EventoJogo.PIAO_MOVIDO);
		notificarObservadores(EventoJogo.PALPITE_FEITO);
		return resultado;
	}

	// Faz uma acusação formal. Retorna true se o jogador venceu.
	// Se errou, elimina o jogador da vez e notifica FIM_DE_JOGO.
	public boolean fazerAcusacao(String suspeito, String arma, String comodo) {
		boolean acertou = jogo.verificarAcusacao(suspeito, arma, comodo);
		if (!acertou) {
			jogo.eliminarJogadorDaVez();
		}
		notificarObservadores(EventoJogo.FIM_DE_JOGO);
		return acertou;
	}

	public boolean estaEmComodo() {
		return jogo.estaEmComodo();
	}

	public String comodoAtual() {
		return jogo.comodoAtual();
	}

	public List<String> getSuspeitos() {
		return jogo.getSuspeitos();
	}

	public List<String> getArmas() {
		return jogo.getArmas();
	}

	public List<String> getComodos() {
		return jogo.getComodos();
	}

	public boolean jogadorDaVezEliminado() {
		return jogo.jogadorDaVezEliminado();
	}

	// ===================== Salvamento e Recuperação (Cap. 16) =============

	// Grava o estado atual da partida em um arquivo texto escolhido pelo usuário.
	public void salvarPartida(File arquivo) throws IOException {
		Persistencia.salvar(arquivo, jogo);
	}

	// Recupera uma partida de um arquivo texto e a torna a partida corrente.
	// Notifica os observadores para que a interface reflita o estado recuperado.
	public void carregarPartida(File arquivo) throws IOException {
		jogo = Persistencia.carregar(arquivo);
		notificarObservadores(EventoJogo.PARTIDA_INICIADA);
	}

	// ===================== Consultas (somente leitura) ====================

	public boolean partidaIniciada() {
		return jogo != null;
	}

	public String jogadorDaVez() {
		return jogo.jogadorDaVez();
	}

	public String suspeitoDaVez() {
		return jogo.suspeitoDaVez();
	}

	public List<String> suspeitosEmJogo() {
		return jogo.suspeitosEmJogo();
	}

	public Posicao posicaoDoPiao(String suspeito) {
		return jogo.posicaoDoPiao(suspeito);
	}

	public Posicao posicaoDoPiaoDaVez() {
		return jogo.posicaoDoPiaoDaVez();
	}

	public Set<Posicao> casasAlcancaveis(int passos) {
		return jogo.casasAlcancaveis(passos);
	}

	public int getDado1() {
		return jogo.getDado1();
	}

	public int getDado2() {
		return jogo.getDado2();
	}

	public int getTotalDados() {
		return jogo.passosRestantes();
	}

	public char tipoCasa(int linha, int coluna) {
		return jogo.tipoCasa(linha, coluna);
	}

	public List<String> cartasDoJogador(String nomeJogador) {
		return jogo.cartasDoJogador(nomeJogador);
	}

	public List<String> cartasDoJogadorDaVez() {
		return jogo.cartasDoJogadorDaVez();
	}

	// Gabarito (modo teste): suspeito, arma e cômodo do envelope confidencial.
	public List<String> getGabarito() {
		return jogo.getGabarito();
	}
}