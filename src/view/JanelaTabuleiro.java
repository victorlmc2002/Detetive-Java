package view;

import javax.swing.JFrame;
import javax.swing.JLabel;

import model.Jogo;

// STUB - Tem que serimplementado:
//   - PainelTabuleiro central (Graphics2D.drawImage do tabuleiro + peões + clique).
//   - PainelControles leste (botões Próximo / Jogar Dados + PainelDados).
//   - Máquina de estados do turno (AGUARDANDO_DADO -> AGUARDANDO_MOVIMENTO -> MOVIMENTO_FEITO).
// Contrato fixo: construtor recebe um Jogo já iniciado.
public class JanelaTabuleiro extends JFrame {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final Jogo jogo;

	public JanelaTabuleiro(Jogo jogo) {
		super("Clue - Jogo");
		this.jogo = jogo;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1400, 1050);
		setLocationRelativeTo(null);
		add(new JLabel("Janela do tabuleiro - implementação pendente", JLabel.CENTER));
	}
}
