package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Controller;
import model.Fachada;
import model.IObservador;
import model.Posicao;

/**
 * Janela principal do jogo.
 *
 * Esta classe é um OBSERVADOR (padrão Observer, Cap. 17): registra-se na
 * Fachada e, a cada notificação de mudança de estado da partida, relê o estado
 * e se redesenha. Ela NÃO contém mais lógica de turno — todos os comandos do
 * usuário são encaminhados ao Controller, que organiza a sequência de eventos.
 *
 * Layout BorderLayout:
 *   CENTER -> PainelTabuleiro  (tabuleiro + peoes, 100% Java2D)
 *   EAST   -> painel de controles (Swing: botoes, label, combos, dados)
 *
 * A máquina de estados do turno vive no Controller; a View apenas consulta
 * controller.getEstado() para habilitar botões e decidir o que desenhar.
 */
public class JanelaTabuleiro extends JFrame implements IObservador {

	private static final long serialVersionUID = 1L;

	// Dimensoes logicas do tabuleiro (espelham model.Tabuleiro)
	private static final int LINHAS  = 25;
	private static final int COLUNAS = 24;

	// Calibracao: fracao da imagem ocupada pelas margens (borda
	// cinza, rotulos dos comodos, abas de partida dos peoes). A grade jogavel
	// 25x24 fica dentro do retangulo (FRAC_LEFT, FRAC_TOP) -> (1-FRAC_RIGHT, 1-FRAC_BOTTOM)
	private static final double FRAC_LEFT   = 0.070;
	private static final double FRAC_TOP    = 0.070;
	private static final double FRAC_RIGHT  = 0.070;
	private static final double FRAC_BOTTOM = 0.070;

	// Ligue para true para desenhar a grade 25x24 sobre a imagem (calibracao).
	private static final boolean DEBUG_GRADE = false;

	// =========================================================================
	// Colaboradores (padroes): unico ponto de contato com o Model/Controller
	// =========================================================================
	private final Fachada    fachada    = Fachada.getInstance();
	private final Controller controller = Controller.getInstance();

	// Casas destacadas durante a escolha do destino (estado AGUARDANDO_MOVIMENTO).
	private Set<Posicao> alcancaveis;

	// =========================================================================
	// Componentes Swing do painel lateral
	// =========================================================================
	private final PainelTabuleiro painelTabuleiro;
	private JLabel              labelJogador;
	private JPanel              painelDados;
	private JButton             botaoDado;
	private JButton             botaoDefinir;
	private JButton             botaoProximo;
	private JButton             botaoCartas;
	private JButton             botaoAnotacoes;
	private JButton             botaoPalpite;
	private JButton             botaoAcusacao;
	private JComboBox<Integer>  comboD1;
	private JComboBox<Integer>  comboD2;

	// Janelas auxiliares (Observer)
	private final JanelaCartas    janelaCartas    = new JanelaCartas();
	private final JanelaAnotacoes janelaAnotacoes = new JanelaAnotacoes();

	// =========================================================================
	// Construtor
	// =========================================================================
	public JanelaTabuleiro() {
		super("Clue - Jogo");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		// Painel central (tabuleiro desenhado em Java2D)
		painelTabuleiro = new PainelTabuleiro();
		add(painelTabuleiro, BorderLayout.CENTER);

		// Painel lateral direito (controles Swing)
		add(criarPainelControles(), BorderLayout.EAST);

		setSize(1400, 1050);
		setLocationRelativeTo(null);

		// Observer: passa a refletir mudancas de estado da partida.
		fachada.adicionarObservador(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				fachada.removerObservador(JanelaTabuleiro.this);
			}
		});

		atualizarInterface();
	}

	// =========================================================================
	// Observer: chamado pela Fachada a cada mudanca de estado da partida.
	// =========================================================================
	@Override
	public void notificar(Object evento) {
		// Estamos sempre na EDT (a Fachada e acionada por handlers Swing),
		// entao basta reler o estado e redesenhar.
		atualizarInterface();
	}

	// =========================================================================
	// Montagem do painel lateral de controles
	// =========================================================================
	private JPanel criarPainelControles() {
		JPanel painel = new JPanel();
		painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
		painel.setBorder(BorderFactory.createEmptyBorder(24, 14, 24, 14));
		painel.setPreferredSize(new Dimension(220, 0));
		painel.setBackground(new Color(25, 20, 20));

		// --- Label: jogador da vez ---
		labelJogador = new JLabel("Vez de: ...");
		labelJogador.setForeground(Color.WHITE);
		labelJogador.setFont(new Font("SansSerif", Font.BOLD, 13));
		labelJogador.setAlignmentX(Component.CENTER_ALIGNMENT);

		// --- Painel dos dados (dois PainelDado lado a lado) ---
		painelDados = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
		painelDados.setOpaque(false);
		painelDados.setMaximumSize(new Dimension(200, 95));

		// --- Botao: lancar dados aleatoriamente (delega ao Controller) ---
		botaoDado = new JButton("Lancar Dados");
		estilizarBotao(botaoDado);
		botaoDado.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.lancarDados();
			}
		});

		// --- Secao de teste ---
		JLabel lblSep = new JLabel("-- Modo Teste --");
		lblSep.setForeground(new Color(100, 90, 80));
		lblSep.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblSep.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel lblTeste = new JLabel("Forcar valores dos dados:");
		lblTeste.setForeground(new Color(160, 150, 140));
		lblTeste.setFont(new Font("SansSerif", Font.PLAIN, 11));
		lblTeste.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Dois JComboBox com inteiros de 1 a 6 (sugestao explicita do enunciado)
		Integer[] vals = { 1, 2, 3, 4, 5, 6 };
		comboD1 = new JComboBox<Integer>(vals);
		comboD2 = new JComboBox<Integer>(vals);

		JLabel ld1 = new JLabel("D1:");
		ld1.setForeground(Color.LIGHT_GRAY);
		JLabel ld2 = new JLabel("D2:");
		ld2.setForeground(Color.LIGHT_GRAY);

		JPanel painelCombos = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
		painelCombos.setOpaque(false);
		painelCombos.add(ld1);
		painelCombos.add(comboD1);
		painelCombos.add(ld2);
		painelCombos.add(comboD2);
		painelCombos.setMaximumSize(new Dimension(200, 36));

		// Botao que usa os valores dos combos (modo testador) em vez de randomizar.
		botaoDefinir = new JButton("Usar Combos");
		estilizarBotao(botaoDefinir);
		botaoDefinir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int d1 = (Integer) comboD1.getSelectedItem();
				int d2 = (Integer) comboD2.getSelectedItem();
				controller.definirDados(d1, d2);
			}
		});

		// --- Botoes de regras ---
		botaoCartas = new JButton("Minhas Cartas");
		estilizarBotao(botaoCartas);
		botaoCartas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.exibirCartas();
			}
		});

		botaoAnotacoes = new JButton("Anotações");
		estilizarBotao(botaoAnotacoes);
		botaoAnotacoes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.exibirAnotacoes();
			}
		});

		botaoPalpite = new JButton("Fazer Palpite");
		estilizarBotao(botaoPalpite);
		botaoPalpite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirDialogPalpite();
			}
		});

		botaoAcusacao = new JButton("Fazer Acusação");
		estilizarBotao(botaoAcusacao);
		botaoAcusacao.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirDialogAcusacao();
			}
		});

		// --- Botao: proximo jogador (fica no fundo do painel) ---
		botaoProximo = new JButton("Proximo Jogador");
		estilizarBotao(botaoProximo);
		botaoProximo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.avancarTurno();
			}
		});

		// --- Montagem vertical ---
		painel.add(Box.createVerticalStrut(10));
		painel.add(labelJogador);
		painel.add(Box.createVerticalStrut(14));
		painel.add(painelDados);
		painel.add(Box.createVerticalStrut(10));
		painel.add(botaoDado);
		painel.add(Box.createVerticalStrut(20));
		painel.add(lblSep);
		painel.add(Box.createVerticalStrut(6));
		painel.add(lblTeste);
		painel.add(Box.createVerticalStrut(4));
		painel.add(painelCombos);
		painel.add(Box.createVerticalStrut(6));
		painel.add(botaoDefinir);
		painel.add(Box.createVerticalStrut(20));
		painel.add(botaoCartas);
		painel.add(Box.createVerticalStrut(6));
		painel.add(botaoAnotacoes);
		painel.add(Box.createVerticalStrut(6));
		painel.add(botaoPalpite);
		painel.add(Box.createVerticalStrut(6));
		painel.add(botaoAcusacao);
		painel.add(Box.createVerticalGlue());
		painel.add(botaoProximo);
		painel.add(Box.createVerticalStrut(10));

		return painel;
	}

	/** Aplica estilo visual uniforme aos botoes do painel lateral. */
	private void estilizarBotao(JButton b) {
		b.setAlignmentX(Component.CENTER_ALIGNMENT);
		b.setMaximumSize(new Dimension(170, 36));
		b.setPreferredSize(new Dimension(170, 36));
	}

	// =========================================================================
	// Sincronizacao da interface com o estado atual (lido da Fachada/Controller)
	// =========================================================================
	/**
	 * Relê o estado da partida (Fachada) e do turno (Controller) e atualiza
	 * todos os componentes. Chamado no construtor e a cada notificacao Observer.
	 */
	private void atualizarInterface() {
		Controller.EstadoTurno estado = controller.getEstado();

		String suspeito = fachada.suspeitoDaVez();
		Color cor = Recursos.cor(suspeito);

		// Texto e cor do label do jogador da vez
		labelJogador.setText("<html><center>Vez de:<br><b>" + suspeito + "</b></center></html>");
		if (cor != null) labelJogador.setForeground(cor);

		// Dados: aparecem apos o lancamento (qualquer estado != AGUARDANDO_DADO).
		painelDados.removeAll();
		if (estado != Controller.EstadoTurno.AGUARDANDO_DADO) {
			painelDados.add(new PainelDado(Recursos.dado(fachada.getDado1())));
			painelDados.add(new PainelDado(Recursos.dado(fachada.getDado2())));
		}
		painelDados.revalidate();
		painelDados.repaint();

		// Destaques: apenas durante a escolha do destino.
		if (estado == Controller.EstadoTurno.AGUARDANDO_MOVIMENTO) {
			alcancaveis = fachada.casasAlcancaveis(fachada.getTotalDados());
		} else {
			alcancaveis = null;
		}

		boolean partidaAtiva = fachada.partidaIniciada()
				&& estado != Controller.EstadoTurno.FIM_DE_JOGO;

		// Habilita/desabilita botoes de acordo com o estado do turno.
		botaoDado.setEnabled(estadoAtivo(estado, Controller.EstadoTurno.AGUARDANDO_DADO));
		botaoDefinir.setEnabled(estadoAtivo(estado, Controller.EstadoTurno.AGUARDANDO_DADO));
		botaoProximo.setEnabled(estadoAtivo(estado, Controller.EstadoTurno.FIM_TURNO));
		botaoCartas.setEnabled(partidaAtiva);
		botaoAnotacoes.setEnabled(partidaAtiva);
		botaoPalpite.setEnabled(controller.podeFazerPalpite());
		botaoAcusacao.setEnabled(partidaAtiva);

		painelTabuleiro.repaint();
	}

	/** Auxiliar: true se a partida está ativa e o estado bate. */
	private boolean estadoAtivo(Controller.EstadoTurno atual, Controller.EstadoTurno esperado) {
		return fachada.partidaIniciada()
			&& atual != Controller.EstadoTurno.FIM_DE_JOGO
			&& atual == esperado;
	}

	// =========================================================================
	// Dialogs de Palpite e Acusação
	// =========================================================================

	/**
	 * Abre um JOptionPane com dois JComboBox (suspeito + arma).
	 * O cômodo é inferido automaticamente (cômodo atual do jogador).
	 * Chama controller.fazerPalpite() e exibe o resultado.
	 */
	private void abrirDialogPalpite() {
		List<String> suspeitos = fachada.getSuspeitos();
		List<String> armas     = fachada.getArmas();

		JComboBox<String> cbSuspeito = new JComboBox<String>(suspeitos.toArray(new String[0]));
		JComboBox<String> cbArma     = new JComboBox<String>(armas.toArray(new String[0]));

		String comodo = fachada.comodoAtual();

		JPanel painel = new JPanel();
		painel.setLayout(new javax.swing.BoxLayout(painel, javax.swing.BoxLayout.Y_AXIS));
		painel.add(new JLabel("Cômodo: " + comodo));
		painel.add(Box.createVerticalStrut(6));
		painel.add(new JLabel("Suspeito:"));
		painel.add(cbSuspeito);
		painel.add(Box.createVerticalStrut(6));
		painel.add(new JLabel("Arma:"));
		painel.add(cbArma);

		int opcao = JOptionPane.showConfirmDialog(this, painel,
				"Fazer Palpite", JOptionPane.OK_CANCEL_OPTION);
		if (opcao != JOptionPane.OK_OPTION) return;

		String suspeito = (String) cbSuspeito.getSelectedItem();
		String arma     = (String) cbArma.getSelectedItem();
		String jogador  = fachada.jogadorDaVez();

		// Registra antes de chamar o Controller (para ter os dados no evento PALPITE_FEITO).
		String resultado = controller.fazerPalpite(suspeito, arma);

		String refutador = null, carta = null;
		if (resultado != null) {
			String[] partes = resultado.split("\\|", 2);
			refutador = partes[0];
			carta = partes.length > 1 ? partes[1] : "?";
		}

		// Registra nas anotações (o evento PALPITE_FEITO já foi disparado pelo Model,
		// mas os dados do resultado só chegam aqui; por isso atualizamos diretamente).
		janelaAnotacoes.registrarPalpite(jogador, suspeito, arma, comodo, refutador, carta);

		// Exibe resultado ao jogador da vez.
		if (resultado == null) {
			JOptionPane.showMessageDialog(this,
				"<html>Nenhum jogador pôde refutar o palpite!<br>" +
				"<b>" + suspeito + "</b> com <b>" + arma + "</b> no <b>" + comodo + "</b></html>",
				"Palpite não refutado", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this,
				"<html><b>" + refutador + "</b> mostrou a carta:<br><b>" + carta + "</b></html>",
				"Palpite refutado", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Abre um JOptionPane com três JComboBox (suspeito + arma + cômodo).
	 * Chama controller.fazerAcusacao() e exibe vitória ou eliminação.
	 */
	private void abrirDialogAcusacao() {
		List<String> suspeitos = fachada.getSuspeitos();
		List<String> armas     = fachada.getArmas();
		List<String> comodos   = fachada.getComodos();

		JComboBox<String> cbSuspeito = new JComboBox<String>(suspeitos.toArray(new String[0]));
		JComboBox<String> cbArma     = new JComboBox<String>(armas.toArray(new String[0]));
		JComboBox<String> cbComodo   = new JComboBox<String>(comodos.toArray(new String[0]));

		JPanel painel = new JPanel();
		painel.setLayout(new javax.swing.BoxLayout(painel, javax.swing.BoxLayout.Y_AXIS));
		painel.add(new JLabel("Suspeito:"));
		painel.add(cbSuspeito);
		painel.add(Box.createVerticalStrut(6));
		painel.add(new JLabel("Arma:"));
		painel.add(cbArma);
		painel.add(Box.createVerticalStrut(6));
		painel.add(new JLabel("Cômodo:"));
		painel.add(cbComodo);

		int opcao = JOptionPane.showConfirmDialog(this, painel,
				"Fazer Acusação", JOptionPane.OK_CANCEL_OPTION);
		if (opcao != JOptionPane.OK_OPTION) return;

		String suspeito = (String) cbSuspeito.getSelectedItem();
		String arma     = (String) cbArma.getSelectedItem();
		String comodo   = (String) cbComodo.getSelectedItem();
		String jogador  = fachada.jogadorDaVez();

		boolean venceu = controller.fazerAcusacao(suspeito, arma, comodo);

		if (venceu) {
			JOptionPane.showMessageDialog(this,
				"<html><b>" + jogador + "</b> venceu!<br>" +
				"Foi <b>" + suspeito + "</b> com <b>" + arma + "</b> no <b>" + comodo + "</b>!</html>",
				"🏆 Vitória!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this,
				"<html>Acusação errada! <b>" + jogador + "</b> foi eliminado.<br>" +
				"A partida continua para os demais jogadores.</html>",
				"Eliminado", JOptionPane.WARNING_MESSAGE);
		}
	}

	// =========================================================================
	// Inner class: minipainel de um dado desenhado via Java2D
	// =========================================================================
	/**
	 * Exibe a imagem de um dado usando Graphics2D.drawImage().
	 * Usar um JPanel custom (em vez de JLabel com ImageIcon) e obrigatorio
	 * pelo enunciado: imagens de elementos de jogo DEVEM usar drawImage().
	 */
	private static class PainelDado extends JPanel {

		private static final long serialVersionUID = 1L;
		private final BufferedImage img;

		PainelDado(BufferedImage img) {
			this.img = img;
			setPreferredSize(new Dimension(80, 80));
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				if (img != null) g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
			} finally {
				g2.dispose(); // sempre liberar o contexto clonado
			}
		}
	}

	// =========================================================================
	// Inner class: painel central com tabuleiro + peoes (100% Java2D)
	// =========================================================================
	/**
	 * Todo o desenho usa EXCLUSIVAMENTE Graphics2D.
	 * Sem JLabel, JButton ou qualquer componente Swing para mostrar imagens.
	 *
	 * Coordenadas:
	 *   A imagem e escalada para caber no painel (letterbox).
	 *   imgX, imgY, imgW, imgH sao calculados a cada repaint e guardados
	 *   para mapear o clique do mouse -> celula (linha, coluna) da grade 25x24.
	 */
	private class PainelTabuleiro extends JPanel {

		private static final long serialVersionUID = 1L;

		// Posicao e tamanho reais da imagem dentro do painel (atualizados no repaint)
		private int imgX, imgY, imgW, imgH;
		// Sub-retangulo da imagem que contem a grade jogavel 25x24 (sem margens decorativas).
		private int gridX, gridY, gridW, gridH;

		PainelTabuleiro() {
			setBackground(new Color(15, 10, 10));

			/*
			 * MouseListener detecta o clique do jogador e o encaminha ao Controller.
			 * Converte pixel -> celula usando a proporcao do ultimo paintComponent.
			 * Usa classe anonima de MouseAdapter (nao usa lambda - proibido pelo enunciado).
			 */
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (gridW == 0) return; // ainda nao foi pintado
					double cw = gridW / (double) COLUNAS;
					double ch = gridH / (double) LINHAS;
					int col = (int) ((e.getX() - gridX) / cw);
					int lin = (int) ((e.getY() - gridY) / ch);
					// Ignora clique fora da grade
					if (lin >= 0 && lin < LINHAS && col >= 0 && col < COLUNAS) {
						controller.tentarMover(lin, col);
					}
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// Ordem de desenho: tabuleiro -> destaques -> peoes -> indicador de vez
				desenharTabuleiro(g2);
				if (DEBUG_GRADE) desenharGradeDebug(g2);
				desenharDestaques(g2);
				desenharPeoes(g2);
				desenharIndicadorVez(g2);

			} finally {
				g2.dispose();
			}
		}

		// -----------------------------------------------------------------
		// 1) Tabuleiro: escala a imagem para caber no painel (letterbox)
		// -----------------------------------------------------------------
		private void desenharTabuleiro(Graphics2D g2) {
			BufferedImage img = Recursos.tabuleiro();
			int pw = getWidth(), ph = getHeight();

			// Fator de escala que preserva a proporcao da imagem
			double escala = Math.min(pw / (double) img.getWidth(),
			                         ph / (double) img.getHeight());
			imgW = (int) (img.getWidth()  * escala);
			imgH = (int) (img.getHeight() * escala);
			// Centraliza dentro do painel
			imgX = (pw - imgW) / 2;
			imgY = (ph - imgH) / 2;

			g2.drawImage(img, imgX, imgY, imgW, imgH, null);

			// Recalcula o sub-retangulo da grade jogavel dentro da imagem.
			gridX = imgX + (int) (imgW * FRAC_LEFT);
			gridY = imgY + (int) (imgH * FRAC_TOP);
			gridW = imgW - (int) (imgW * (FRAC_LEFT + FRAC_RIGHT));
			gridH = imgH - (int) (imgH * (FRAC_TOP + FRAC_BOTTOM));
		}

		// -----------------------------------------------------------------
		// 1.5) DEBUG: desenha a grade 25x24 em vermelho para calibrar os FRAC_*
		// -----------------------------------------------------------------
		private void desenharGradeDebug(Graphics2D g2) {
			double cw = gridW / (double) COLUNAS;
			double ch = gridH / (double) LINHAS;
			g2.setColor(new Color(255, 0, 0, 180));
			g2.setStroke(new BasicStroke(1f));
			// Linhas verticais
			for (int c = 0; c <= COLUNAS; c++) {
				int x = gridX + (int) (c * cw);
				g2.drawLine(x, gridY, x, gridY + gridH);
			}
			// Linhas horizontais
			for (int l = 0; l <= LINHAS; l++) {
				int y = gridY + (int) (l * ch);
				g2.drawLine(gridX, y, gridX + gridW, y);
			}
			// Borda externa mais grossa
			g2.setStroke(new BasicStroke(2f));
			g2.drawRect(gridX, gridY, gridW, gridH);

			// Letra com o tipo de cada casa, centralizada na celula.
			int tam = Math.max(8, (int) (Math.min(cw, ch) * 0.55));
			g2.setFont(new Font("SansSerif", Font.BOLD, tam));
			java.awt.FontMetrics fm = g2.getFontMetrics();
			int ascent = fm.getAscent();
			for (int l = 0; l < LINHAS; l++) {
				for (int c = 0; c < COLUNAS; c++) {
					char tipo = fachada.tipoCasa(l, c);
					String s = String.valueOf(tipo);
					int tw = fm.stringWidth(s);
					int cx = gridX + (int) ((c + 0.5) * cw);
					int cy = gridY + (int) ((l + 0.5) * ch);
					// Fundo branco semi-transparente para contraste.
					g2.setColor(new Color(255, 255, 255, 160));
					g2.fillRect(cx - tw / 2 - 1, cy - ascent / 2, tw + 2, ascent);
					// Letra vermelha por cima.
					g2.setColor(new Color(180, 0, 0));
					g2.drawString(s, cx - tw / 2, cy + ascent / 2 - 2);
				}
			}
		}

		// -----------------------------------------------------------------
		// 2) Destaques: pinta as casas alcancaveis em amarelo translucido
		// -----------------------------------------------------------------
		private void desenharDestaques(Graphics2D g2) {
			if (alcancaveis == null || alcancaveis.isEmpty()) return;
			double cw = gridW / (double) COLUNAS;
			double ch = gridH / (double) LINHAS;

			// Preenchimento suave
			g2.setColor(new Color(255, 230, 0, 65));
			for (Posicao p : alcancaveis) {
				int px = gridX + (int) (p.getColuna() * cw);
				int py = gridY + (int) (p.getLinha()  * ch);
				g2.fillRect(px, py, (int) cw + 1, (int) ch + 1);
			}
			// Borda para destacar a grade
			g2.setColor(new Color(255, 200, 0, 170));
			g2.setStroke(new BasicStroke(1f));
			for (Posicao p : alcancaveis) {
				int px = gridX + (int) (p.getColuna() * cw);
				int py = gridY + (int) (p.getLinha()  * ch);
				g2.drawRect(px, py, (int) cw, (int) ch);
			}
		}

		// -----------------------------------------------------------------
		// 3) Peoes: circulo colorido com sombra + anel no jogador ativo
		// -----------------------------------------------------------------
		private void desenharPeoes(Graphics2D g2) {
			double cw = gridW / (double) COLUNAS;
			double ch = gridH / (double) LINHAS;
			String ativo = fachada.suspeitoDaVez();

			List<String> suspeitos = fachada.suspeitosEmJogo();
			for (String s : suspeitos) {
				Posicao pos = fachada.posicaoDoPiao(s);
				Color cor = Recursos.cor(s);

				// Centro do circulo no meio da celula
				double cx = gridX + (pos.getColuna() + 0.5) * cw;
				double cy = gridY + (pos.getLinha()  + 0.5) * ch;
				int r = (int) (Math.min(cw, ch) * 0.36); // raio = 36% da celula

				// Sombra deslocada 2px
				g2.setColor(new Color(0, 0, 0, 110));
				g2.fillOval((int) cx - r + 2, (int) cy - r + 2, r * 2, r * 2);

				// Corpo do peao (cor do suspeito)
				g2.setColor(cor != null ? cor : Color.GRAY);
				g2.fillOval((int) cx - r, (int) cy - r, r * 2, r * 2);

				// Borda branca fina
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(1.3f));
				g2.drawOval((int) cx - r, (int) cy - r, r * 2, r * 2);

				// Anel externo branco no jogador da vez
				if (s.equals(ativo)) {
					g2.setColor(Color.WHITE);
					g2.setStroke(new BasicStroke(2.5f));
					g2.drawOval((int) cx - r - 3, (int) cy - r - 3,
					            r * 2 + 6, r * 2 + 6);
				}
			}
		}

		// -----------------------------------------------------------------
		// 4) Indicador de vez: faixa colorida fina no topo do tabuleiro
		// -----------------------------------------------------------------
		private void desenharIndicadorVez(Graphics2D g2) {
			Color cor = Recursos.cor(fachada.suspeitoDaVez());
			if (cor == null) return;
			g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 210));
			g2.fillRoundRect(imgX, imgY, imgW, 7, 4, 4);
		}
	}
}