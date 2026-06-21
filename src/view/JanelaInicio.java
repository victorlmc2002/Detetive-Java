package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.Controller;

// Primeira janela do jogo: oferece "Novo Jogo" e "Continuar".
// "Continuar" recupera uma partida previamente salva em arquivo texto.
public class JanelaInicio extends JFrame {

	private static final long serialVersionUID = 1L;

	private final JButton botaoNovo = new JButton("Novo Jogo");
	private final JButton botaoContinuar = new JButton("Continuar");

	public JanelaInicio() {
		super("Clue - Início");
		Recursos.carregar();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		PainelFundo fundo = new PainelFundo();
		fundo.setLayout(new BorderLayout());

		JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
		painelBotoes.setOpaque(false);
		botaoNovo.setPreferredSize(new Dimension(140, 36));
		botaoContinuar.setPreferredSize(new Dimension(140, 36));
		painelBotoes.add(botaoNovo);
		painelBotoes.add(botaoContinuar);
		fundo.add(painelBotoes, BorderLayout.SOUTH);

		add(fundo, BorderLayout.CENTER);

		botaoNovo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JanelaPersonagens prox = new JanelaPersonagens();
				prox.setVisible(true);
				JanelaInicio.this.dispose();
			}
		});

		botaoContinuar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				carregarPartida();
			}
		});

		setSize(640, 480);
		setLocationRelativeTo(null);
	}

	/**
	 * Recupera uma partida salva: abre um JFileChooser para o usuário escolher o
	 * arquivo texto (.txt), pede ao Controller que reconstrua o estado e abre a
	 * janela do tabuleiro já refletindo a partida recuperada.
	 */
	private void carregarPartida() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Carregar Partida");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Arquivo de texto (*.txt)", "txt"));

		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

		File arquivo = chooser.getSelectedFile();
		try {
			Controller.getInstance().carregarPartida(arquivo);
			JanelaTabuleiro tabuleiro = new JanelaTabuleiro();
			tabuleiro.setVisible(true);
			dispose();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
				"Erro ao carregar a partida:\n" + ex.getMessage(),
				"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Painel desenhado via Java2D. Por enquanto exibe título "Clue"; pode receber
	// uma imagem de capa no futuro.
	private static class PainelFundo extends JPanel {

		private static final long serialVersionUID = 1L;

		PainelFundo() {
			setBorder(BorderFactory.createEmptyBorder());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				int w = getWidth();
				int h = getHeight();

				g2.setColor(new Color(30, 25, 25));
				g2.fillRect(0, 0, w, h);

				g2.setColor(new Color(200, 30, 30));
				g2.setFont(new Font("Serif", Font.BOLD, 96));
				String titulo = "Clue";
				int largura = g2.getFontMetrics().stringWidth(titulo);
				g2.drawString(titulo, (w - largura) / 2, h / 2 - 20);

				g2.setColor(new Color(230, 220, 200));
				g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
				String subtitulo = "INF - 1636 PUC-RIO";
				int subLarg = g2.getFontMetrics().stringWidth(subtitulo);
				g2.drawString(subtitulo, (w - subLarg) / 2, h / 2 + 20);
			} finally {
				g2.dispose();
			}
		}
	}
}
