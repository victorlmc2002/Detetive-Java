package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.EventoJogo;
import model.Fachada;
import model.IObservador;

/**
 * Janela que exibe as cartas na mão do jogador da vez.
 *
 * OBSERVER: implementa IObservador e se registra na Fachada.
 * Abre/atualiza quando recebe o evento EXIBIR_CARTAS.
 * Todo desenho de imagem usa exclusivamente Graphics2D.drawImage() (requisito).
 */
public class JanelaCartas extends JFrame implements IObservador {

	private static final long serialVersionUID = 1L;

	private final Fachada fachada = Fachada.getInstance();
	private JPanel painelCartas;
	private JLabel labelTitulo;

	public JanelaCartas() {
		super("Minhas Cartas");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout(8, 8));
		getContentPane().setBackground(new Color(30, 25, 25));

		labelTitulo = new JLabel("", JLabel.CENTER);
		labelTitulo.setForeground(Color.WHITE);
		labelTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
		labelTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
		add(labelTitulo, BorderLayout.NORTH);

		painelCartas = new JPanel();
		painelCartas.setOpaque(false);
		add(painelCartas, BorderLayout.CENTER);

		// Observer: registra-se na Fachada para receber eventos.
		fachada.adicionarObservador(this);
	}

	// =========================================================================
	// Observer
	// =========================================================================
	@Override
	public void notificar(model.IObservado origem, Object evento) {
		if (evento == EventoJogo.EXIBIR_CARTAS) {
			atualizar();
			setVisible(true);
			toFront();
		}
	}

	// =========================================================================
	// Atualização do conteúdo
	// =========================================================================
	private void atualizar() {
		if (!fachada.partidaIniciada()) return;

		String jogador  = fachada.jogadorDaVez();
		String suspeito = fachada.suspeitoDaVez();
		Color cor = Recursos.cor(suspeito);

		labelTitulo.setText("Cartas de " + jogador + " (" + suspeito + ")");
		if (cor != null) labelTitulo.setForeground(cor);

		List<String> cartas = fachada.cartasDoJogador(jogador);

		painelCartas.removeAll();
		int n = cartas.size();
		int cols = Math.min(n, 6); // no máximo 6 por linha
		int rows = (n == 0) ? 1 : (int) Math.ceil(n / (double) cols);
		painelCartas.setLayout(new GridLayout(rows, cols, 6, 6));
		painelCartas.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

		for (String nomeCarta : cartas) {
			painelCartas.add(new PainelCarta(nomeCarta));
		}

		pack();
		setMinimumSize(new Dimension(Math.max(300, cols * 120 + 20), rows * 160 + 70));
		pack();
		setLocationRelativeTo(null);
		painelCartas.revalidate();
		painelCartas.repaint();
	}

	// =========================================================================
	// Inner class: painel de uma única carta desenhada via Java2D
	// =========================================================================
	private static class PainelCarta extends JPanel {

		private static final long serialVersionUID = 1L;

		private final String nome;
		private final BufferedImage img;

		PainelCarta(String nome) {
			this.nome = nome;
			// Tenta obter imagem do suspeito; para armas e cômodos retornará null
			// por enquanto — o nome é escrito textualmente.
			this.img = Recursos.cartaSuspeito(nome);
			setPreferredSize(new Dimension(110, 150));
			setBackground(new Color(50, 40, 40));
			setBorder(BorderFactory.createLineBorder(new Color(120, 100, 80), 1));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				int w = getWidth(), h = getHeight();

				if (img != null) {
					// Desenha a imagem ocupando ~75% da altura do painel.
					int ih = (int) (h * 0.72);
					int iw = img.getWidth() * ih / img.getHeight();
					int ix = (w - iw) / 2;
					g2.drawImage(img, ix, 4, iw, ih, null);
				} else {
					// Placeholder para cartas sem imagem.
					g2.setColor(new Color(70, 60, 50));
					g2.fillRoundRect(8, 8, w - 16, (int) (h * 0.70), 8, 8);
					g2.setColor(new Color(160, 130, 100));
					g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
					g2.drawString("?", w / 2 - 3, (int) (h * 0.38));
				}

				// Nome da carta na parte inferior.
				g2.setColor(Color.WHITE);
				g2.setFont(new Font("SansSerif", Font.BOLD, 10));
				java.awt.FontMetrics fm = g2.getFontMetrics();
				// Quebra nome longo em 2 linhas se necessário.
				String[] partes = quebrarTexto(nome, fm, w - 8);
				int yBase = h - (partes.length * 13) - 4;
				for (String parte : partes) {
					int tx = (w - fm.stringWidth(parte)) / 2;
					g2.drawString(parte, tx, yBase);
					yBase += 13;
				}
			} finally {
				g2.dispose();
			}
		}

		private static String[] quebrarTexto(String texto, java.awt.FontMetrics fm, int maxW) {
			if (fm.stringWidth(texto) <= maxW) return new String[]{texto};
			// Tenta quebrar na primeira palavra
			int espaco = texto.indexOf(' ');
			if (espaco < 0) return new String[]{texto};
			return new String[]{texto.substring(0, espaco), texto.substring(espaco + 1)};
		}
	}
}
