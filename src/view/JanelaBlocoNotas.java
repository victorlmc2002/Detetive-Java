package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import model.EventoJogo;
import model.Fachada;
import model.IObservador;

/**
 * Bloco de notas de um jogador (Figura 6 do enunciado da 2ª iteração).
 *
 * Apresenta TODAS as cartas do jogo agrupadas em três quadros - Suspeitos,
 * Cômodos e Armas - cada carta com um JCheckBox. A janela:
 *
 *   - é aberta/fechada por meio de componentes Java Swing (botão "Bloco de
 *     Notas" no tabuleiro + botão de fechar da própria janela);
 *   - marca AUTOMATICAMENTE as cartas que estão de posse do jogador da vez
 *     (essas marcações ficam travadas, pois são fatos conhecidos);
 *   - permite que o jogador MARQUE/DESMARQUE manualmente as demais cartas à
 *     medida que adversários lhe mostram cartas durante as refutações.
 *
 * As marcações manuais são guardadas POR JOGADOR (chaveadas pelo suspeito da
 * vez, que é único na partida), de modo que cada jogador tenha o seu próprio
 * bloco de notas, exatamente como na Figura 6 ("SCARLETT's Notes").
 *
 * Tratamento de eventos feito com classes anônimas (sem expressões lambda,
 * conforme exigência do enunciado).
 */
public class JanelaBlocoNotas extends JFrame implements IObservador {

	private static final long serialVersionUID = 1L;

	private static final Color FUNDO        = new Color(25, 20, 20);
	private static final Color FUNDO_QUADRO = new Color(35, 30, 28);
	private static final Color TEXTO        = Color.WHITE;
	private static final Color DESTAQUE     = new Color(200, 170, 100);

	private final Fachada fachada = Fachada.getInstance();

	// Um JCheckBox por carta, indexado pelo nome EXATO da carta no Model.
	private final Map<String, JCheckBox> checkboxes = new HashMap<>();

	// Marcações MANUAIS por jogador: suspeito da vez -> conjunto de cartas marcadas.
	// As cartas de posse do próprio jogador NÃO entram aqui (são travadas à parte).
	private final Map<String, Set<String>> marcacoesManuais = new HashMap<>();

	// Suspeito (jogador) cujo bloco está sendo exibido no momento. As marcações
	// manuais feitas na interface são atribuídas a ele.
	private String suspeitoExibido = null;

	public JanelaBlocoNotas() {
		super("Bloco de Notas");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setResizable(false);
		getContentPane().setBackground(FUNDO);

		JPanel painel = new JPanel(new GridLayout(1, 3, 10, 0));
		painel.setBackground(FUNDO);
		painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		painel.add(criarQuadro("Suspeitos", fachada.getSuspeitos()));
		painel.add(criarQuadro("Cômodos",   fachada.getComodos()));
		painel.add(criarQuadro("Armas",     fachada.getArmas()));

		add(painel);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(null);

		// Observer: passa a refletir mudanças de estado da partida.
		fachada.adicionarObservador(this);
	}

	// =========================================================================
	// Montagem de um quadro (Suspeitos / Cômodos / Armas)
	// =========================================================================
	private JPanel criarQuadro(String titulo, List<String> cartas) {
		JPanel quadro = new JPanel();
		quadro.setLayout(new BoxLayout(quadro, BoxLayout.Y_AXIS));
		quadro.setBackground(FUNDO_QUADRO);

		TitledBorder borda = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(70, 60, 50)), titulo);
		borda.setTitleColor(DESTAQUE);
		borda.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
		quadro.setBorder(BorderFactory.createCompoundBorder(
				borda, BorderFactory.createEmptyBorder(4, 8, 8, 8)));

		for (String carta : cartas) {
			quadro.add(criarCheckBox(carta));
		}
		return quadro;
	}

	/**
	 * Cria o JCheckBox de uma carta e instala o tratador (classe anônima) que
	 * registra/remove a marcação manual no bloco do jogador exibido no momento.
	 */
	private JCheckBox criarCheckBox(final String nomeCarta) {
		final JCheckBox cb = new JCheckBox(nomeCarta);
		cb.setBackground(FUNDO_QUADRO);
		cb.setForeground(TEXTO);
		cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
		cb.setFocusPainted(false);
		cb.setAlignmentX(Component.LEFT_ALIGNMENT);

		// O tratador só dispara em ação do usuário (setSelected programático NÃO
		// dispara ActionListener), portanto a reconstrução em atualizar() é segura.
		cb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (suspeitoExibido == null) return;
				Set<String> marcadas = marcacoesManuais.get(suspeitoExibido);
				if (marcadas == null) {
					marcadas = new HashSet<>();
					marcacoesManuais.put(suspeitoExibido, marcadas);
				}
				if (cb.isSelected()) {
					marcadas.add(nomeCarta);
				} else {
					marcadas.remove(nomeCarta);
				}
			}
		});

		checkboxes.put(nomeCarta, cb);
		return cb;
	}

	// =========================================================================
	// Observer
	// =========================================================================
	@Override
	public void notificar(model.IObservado origem, Object evento) {
		if (evento == EventoJogo.EXIBIR_BLOCO_NOTAS) {
			atualizar();
			setVisible(true);
			toFront();
		} else if (evento == EventoJogo.PARTIDA_INICIADA) {
			marcacoesManuais.clear();
			suspeitoExibido = null;
		}
	}

	// =========================================================================
	// Atualização do conteúdo para o jogador da vez
	// =========================================================================
	/**
	 * Sincroniza os checkboxes com o bloco do jogador da vez:
	 *   - cartas de posse dele: marcadas e travadas (fato conhecido);
	 *   - cartas marcadas manualmente por ele: marcadas e editáveis;
	 *   - demais cartas: desmarcadas e editáveis.
	 */
	private void atualizar() {
		if (!fachada.partidaIniciada()) return;

		suspeitoExibido = fachada.suspeitoDaVez();
		String jogador  = fachada.jogadorDaVez();
		setTitle("Bloco de Notas - " + jogador + " (" + suspeitoExibido + ")");

		Set<String> proprias = new HashSet<>(fachada.cartasDoJogadorDaVez());
		Set<String> manuais  = marcacoesManuais.get(suspeitoExibido);
		if (manuais == null) manuais = new HashSet<>();

		for (Map.Entry<String, JCheckBox> entrada : checkboxes.entrySet()) {
			String nome  = entrada.getKey();
			JCheckBox cb = entrada.getValue();
			boolean propria = proprias.contains(nome);
			// setSelected não dispara o ActionListener - não há risco de poluir
			// as marcações manuais durante a reconstrução.
			cb.setSelected(propria || manuais.contains(nome));
			cb.setEnabled(!propria); // cartas próprias ficam travadas
		}
		repaint();
	}
}
