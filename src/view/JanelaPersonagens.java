package view;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.Controller;

// Segunda janela: o usuário escolhe quais suspeitos participam clicando nas cartas.
// Scarlet sempre joga, demais são opcionais. Mínimo de 3 jogadores no total.
public class JanelaPersonagens extends JFrame {

	private static final long serialVersionUID = 1L;

	private final PainelCarta[] cartas = new PainelCarta[Recursos.SUSPEITOS.length];
	private final JButton botaoJogar = new JButton("Jogar");
	private final JLabel contador = new JLabel();

	public JanelaPersonagens() {
		super("Clue - Personagens");
		Recursos.carregar();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());

		add(criarGradeCartas(), BorderLayout.CENTER);
		add(criarPainelLateral(), BorderLayout.EAST);

		botaoJogar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				iniciarPartida();
			}
		});

		atualizarEstado();
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel criarGradeCartas() {
		JPanel grade = new JPanel(new GridLayout(2, 3, 6, 6));
		grade.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		grade.setBackground(new Color(40, 30, 20));
		for (int i = 0; i < Recursos.SUSPEITOS.length; i++) {
			boolean obrigatorio = (i == 0);
			cartas[i] = new PainelCarta(Recursos.SUSPEITOS[i], obrigatorio);
			grade.add(cartas[i]);
		}
		return grade;
	}

	private JPanel criarPainelLateral() {
		JPanel painel = new JPanel();
		painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
		painel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		JLabel titulo = new JLabel("Selecione os personagens");
		titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
		titulo.setAlignmentX(0f);

		contador.setAlignmentX(0f);

		botaoJogar.setAlignmentX(0f);
		botaoJogar.setPreferredSize(new Dimension(140, 36));
		botaoJogar.setMaximumSize(new Dimension(140, 36));

		painel.add(titulo);
		painel.add(Box.createVerticalStrut(6));
		painel.add(contador);
		painel.add(Box.createVerticalStrut(20));
		painel.add(botaoJogar);
		painel.add(Box.createVerticalGlue());

		return painel;
	}

	// Chamado por cada PainelCarta quando o usuário clica.
	void cartaAlternada() {
		atualizarEstado();
	}

	private void atualizarEstado() {
		int marcados = 0;
		for (PainelCarta c : cartas) {
			if (c.estaSelecionada()) marcados++;
		}
		contador.setText("Selecionados: " + marcados + "/6 (mínimo 3)");
		botaoJogar.setEnabled(marcados >= 3);
	}

	private void iniciarPartida() {
		List<String> nomes = new ArrayList<>();
		for (PainelCarta c : cartas) {
			if (c.estaSelecionada()) {
				nomes.add(c.getSuspeito());
			}
		}
		// O Controller (Singleton) inicia a partida na Fachada; a janela do
		// tabuleiro apenas observa o estado resultante.
		Controller.getInstance().iniciarPartida(nomes);
		JanelaTabuleiro proxima = new JanelaTabuleiro();
		proxima.setVisible(true);
		dispose();
	}

	// Painel custom que desenha a imagem do suspeito via Graphics2D.drawImage
	// e gerencia o estado selecionada/não-selecionada por meio de cliques.
	private class PainelCarta extends JPanel {

		private static final long serialVersionUID = 1L;
		private final String suspeito;
		private final BufferedImage imagem;
		private boolean selecionada = true;

		PainelCarta(String suspeito, boolean obrigatorio) {
			this.suspeito = suspeito;
			this.imagem = Recursos.cartaSuspeito(suspeito);
			setPreferredSize(new Dimension(200, 280));
			setBackground(new Color(40, 30, 20));
			if (!obrigatorio) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (obrigatorio) return;
					selecionada = !selecionada;
					repaint();
					cartaAlternada();
				}
			});
		}

		boolean estaSelecionada() {
			return selecionada;
		}

		String getSuspeito() {
			return suspeito;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				int w = getWidth();
				int h = getHeight();

				if (imagem != null) {
					g2.drawImage(imagem, 0, 0, w, h, null);
				}

				if (!selecionada) {
					// Sobreposição cinza translúcida para indicar "fora da partida".
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
					g2.setColor(new Color(40, 40, 40));
					g2.fillRect(0, 0, w, h);
				} else {
					// Borda colorida do suspeito quando selecionada.
					g2.setComposite(AlphaComposite.SrcOver);
					Color cor = Recursos.cor(suspeito);
					if (cor != null) {
						g2.setColor(cor);
						g2.setStroke(new java.awt.BasicStroke(5f));
						g2.drawRect(2, 2, w - 5, h - 5);
					}
				}
			} finally {
				g2.dispose();
			}
		}
	}
}
