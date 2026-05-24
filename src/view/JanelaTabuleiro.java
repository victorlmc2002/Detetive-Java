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
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Jogo;
import model.Posicao;

/**
 * Janela principal do jogo.
 *
 * Layout BorderLayout:
 *   CENTER -> PainelTabuleiro  (tabuleiro + peoes, 100% Java2D)
 *   EAST   -> painel de controles (Swing: botoes, label, combos, dados)
 *
 * Maquina de estados do turno (campo 'estado'):
 *   AGUARDANDO_DADO  ->  AGUARDANDO_MOVIMENTO  ->  FIM_TURNO
 *                            ^ (clique invalido: fica aqui)
 *   FIM_TURNO  ->  AGUARDANDO_DADO  (ao clicar "Proximo Jogador")
 */
public class JanelaTabuleiro extends JFrame {

	private static final long serialVersionUID = 1L;

	// Dimensoes logicas do tabuleiro (espelham model.Tabuleiro)
	private static final int LINHAS  = 25;
	private static final int COLUNAS = 24;

	// =========================================================================
	// Maquina de estados do turno
	// =========================================================================
	private enum EstadoTurno {
		AGUARDANDO_DADO,       // Jogador ainda nao lancou os dados
		AGUARDANDO_MOVIMENTO,  // Dados lancados; aguarda clique no destino
		FIM_TURNO              // Peao movido; aguarda "Proximo Jogador"
	}

	// =========================================================================
	// Estado interno
	// =========================================================================
	private final Jogo   jogo;
	private final Random rng = new Random();

	private EstadoTurno  estado     = EstadoTurno.AGUARDANDO_DADO;
	private Set<Posicao> alcancaveis;   // casas que o jogador pode alcancar
	private int          valorD1 = 0;  // valor do dado 1 (para exibir imagem)
	private int          valorD2 = 0;  // valor do dado 2

	// =========================================================================
	// Componentes Swing do painel lateral
	// =========================================================================
	private final PainelTabuleiro painelTabuleiro;
	private JLabel              labelJogador;
	private JPanel              painelDados;
	private JButton             botaoDado;
	private JButton             botaoDefinir;
	private JButton             botaoProximo;
	private JComboBox<Integer>  comboD1;
	private JComboBox<Integer>  comboD2;

	// =========================================================================
	// Construtor
	// =========================================================================
	public JanelaTabuleiro(Jogo jogo) {
		super("Clue - Jogo");
		this.jogo = jogo;

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

		// --- Botao: lancar dados aleatoriamente ---
		botaoDado = new JButton("Lancar Dados");
		estilizarBotao(botaoDado);
		botaoDado.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lancarAleatoriamente();
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

		// Botao que usa os valores dos combos em vez de rolar aleatoriamente
		botaoDefinir = new JButton("Usar Combos");
		estilizarBotao(botaoDefinir);
		botaoDefinir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lancarComCombos();
			}
		});

		// --- Botao: proximo jogador (fica no fundo do painel) ---
		botaoProximo = new JButton("Proximo Jogador");
		estilizarBotao(botaoProximo);
		botaoProximo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				avancarTurno();
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
	// Logica de turno
	// =========================================================================

	/** Lanca os dados de forma aleatoria. */
	private void lancarAleatoriamente() {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		valorD1 = rng.nextInt(6) + 1;
		valorD2 = rng.nextInt(6) + 1;
		processarLancamento();
	}

	/** Usa os valores dos JComboBox (modo teste). */
	private void lancarComCombos() {
		if (estado != EstadoTurno.AGUARDANDO_DADO) return;
		valorD1 = (Integer) comboD1.getSelectedItem();
		valorD2 = (Integer) comboD2.getSelectedItem();
		processarLancamento();
	}

	/**
	 * Logica comum apos definir valorD1 e valorD2:
	 * calcula as casas alcancaveis e passa para AGUARDANDO_MOVIMENTO.
	 */
	private void processarLancamento() {
		int total = valorD1 + valorD2;
		alcancaveis = jogo.casasAlcancaveis(total);
		estado = EstadoTurno.AGUARDANDO_MOVIMENTO;
		atualizarInterface();
		painelTabuleiro.repaint();
	}

	/**
	 * Chamado quando o jogador clica em uma celula do tabuleiro.
	 * Se o destino for alcancavel: move o peao e vai para FIM_TURNO.
	 * Se nao for: nada acontece (sem mensagem, sem troca de vez - conforme regra).
	 */
	private void tentarMover(int linha, int coluna) {
		if (estado != EstadoTurno.AGUARDANDO_MOVIMENTO) return;
		Posicao destino = new Posicao(linha, coluna);
		if (alcancaveis != null && alcancaveis.contains(destino)) {
			jogo.deslocarPiao(destino);
			alcancaveis = null;
			estado = EstadoTurno.FIM_TURNO;
			atualizarInterface();
			painelTabuleiro.repaint();
		}
		// Destino invalido: silencio total (conforme regra)
	}

	/** Passa a vez para o proximo jogador e reinicia o ciclo do turno. */
	private void avancarTurno() {
		if (estado != EstadoTurno.FIM_TURNO) return;
		jogo.proximoJogador();
		alcancaveis = null;
		valorD1 = 0;
		valorD2 = 0;
		estado = EstadoTurno.AGUARDANDO_DADO;
		atualizarInterface();
		painelTabuleiro.repaint();
	}

	/**
	 * Sincroniza todos os componentes Swing com o estado atual do jogo.
	 * Deve ser chamado sempre que o estado do turno mudar.
	 */
	private void atualizarInterface() {
		String suspeito = jogo.suspeitoDaVez();
		Color cor = Recursos.cor(suspeito);

		// Texto e cor do label do jogador da vez
		labelJogador.setText("<html><center>Vez de:<br><b>" + suspeito + "</b></center></html>");
		if (cor != null) labelJogador.setForeground(cor);

		// Atualiza as imagens dos dados (aparecem so depois de lancar)
		painelDados.removeAll();
		if (valorD1 > 0) {
			painelDados.add(new PainelDado(Recursos.dado(valorD1)));
			painelDados.add(new PainelDado(Recursos.dado(valorD2)));
		}
		painelDados.revalidate();
		painelDados.repaint();

		// Habilita/desabilita botoes de acordo com o estado do turno
		botaoDado.setEnabled(estado == EstadoTurno.AGUARDANDO_DADO);
		botaoDefinir.setEnabled(estado == EstadoTurno.AGUARDANDO_DADO);
		botaoProximo.setEnabled(estado == EstadoTurno.FIM_TURNO);
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

		PainelTabuleiro() {
			setBackground(new Color(15, 10, 10));

			/*
			 * MouseListener detecta o clique do jogador.
			 * Converte pixel -> celula usando a proporcao do ultimo paintComponent.
			 * Usa classe anonima de MouseAdapter (nao usa lambda - proibido pelo enunciado).
			 */
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (imgW == 0) return; // ainda nao foi pintado
					double cw = imgW / (double) COLUNAS;
					double ch = imgH / (double) LINHAS;
					int col = (int) ((e.getX() - imgX) / cw);
					int lin = (int) ((e.getY() - imgY) / ch);
					// Ignora clique fora da grade
					if (lin >= 0 && lin < LINHAS && col >= 0 && col < COLUNAS) {
						tentarMover(lin, col);
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
		}

		// -----------------------------------------------------------------
		// 2) Destaques: pinta as casas alcancaveis em amarelo translucido
		// -----------------------------------------------------------------
		private void desenharDestaques(Graphics2D g2) {
			if (alcancaveis == null || alcancaveis.isEmpty()) return;
			double cw = imgW / (double) COLUNAS;
			double ch = imgH / (double) LINHAS;

			// Preenchimento suave
			g2.setColor(new Color(255, 230, 0, 65));
			for (Posicao p : alcancaveis) {
				int px = imgX + (int) (p.getColuna() * cw);
				int py = imgY + (int) (p.getLinha()  * ch);
				g2.fillRect(px, py, (int) cw + 1, (int) ch + 1);
			}
			// Borda para destacar a grade
			g2.setColor(new Color(255, 200, 0, 170));
			g2.setStroke(new BasicStroke(1f));
			for (Posicao p : alcancaveis) {
				int px = imgX + (int) (p.getColuna() * cw);
				int py = imgY + (int) (p.getLinha()  * ch);
				g2.drawRect(px, py, (int) cw, (int) ch);
			}
		}

		// -----------------------------------------------------------------
		// 3) Peoes: circulo colorido com sombra + anel no jogador ativo
		// -----------------------------------------------------------------
		private void desenharPeoes(Graphics2D g2) {
			double cw = imgW / (double) COLUNAS;
			double ch = imgH / (double) LINHAS;
			String ativo = jogo.suspeitoDaVez();

			List<String> suspeitos = jogo.suspeitosEmJogo();
			for (String s : suspeitos) {
				Posicao pos = jogo.posicaoDoPiao(s);
				Color cor = Recursos.cor(s);

				// Centro do circulo no meio da celula
				double cx = imgX + (pos.getColuna() + 0.5) * cw;
				double cy = imgY + (pos.getLinha()  + 0.5) * ch;
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
			Color cor = Recursos.cor(jogo.suspeitoDaVez());
			if (cor == null) return;
			g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 210));
			g2.fillRoundRect(imgX, imgY, imgW, 7, 4, 4);
		}
	}
}
