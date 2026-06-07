package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.EventoJogo;
import model.Fachada;
import model.IObservador;

/**
 * Janela de anotações dos palpites feitos durante a partida.
 *
 * OBSERVER: implementa IObservador e se registra na Fachada.
 * Abre/atualiza quando recebe o evento EXIBIR_ANOTACOES ou PALPITE_FEITO.
 *
 * Estrutura da tabela:
 *   Colunas: Turno | Jogador | Suspeito | Arma | Cômodo | Refutado por | Carta
 */
public class JanelaAnotacoes extends JFrame implements IObservador {

	private static final long serialVersionUID = 1L;

	private static final String[] COLUNAS = {
		"Turno", "Jogador", "Suspeito", "Arma", "Cômodo", "Refutado por", "Carta mostrada"
	};

	private final Fachada fachada = Fachada.getInstance();

	// Histórico de linhas — acumula durante a partida.
	private final List<Object[]> historico = new ArrayList<>();
	private int turno = 0;

	// Campos do último palpite, preenchidos por registrarPalpite().
	private String ultimoJogador = "";
	private String ultimoSuspeito = "";
	private String ultimaArma = "";
	private String ultimoComodo = "";
	private String ultimoRefutador = "-";
	private String ultimaCarta = "-";

	private DefaultTableModel modeloTabela;
	private JTable tabela;

	public JanelaAnotacoes() {
		super("Anotações dos Palpites");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLayout(new BorderLayout(0, 0));
		getContentPane().setBackground(new Color(25, 20, 20));

		JLabel titulo = new JLabel("Histórico de Palpites", JLabel.CENTER);
		titulo.setForeground(new Color(200, 170, 100));
		titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
		titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
		add(titulo, BorderLayout.NORTH);

		modeloTabela = new DefaultTableModel(COLUNAS, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) { return false; }
		};

		tabela = new JTable(modeloTabela);
		tabela.setBackground(new Color(35, 30, 28));
		tabela.setForeground(Color.WHITE);
		tabela.setGridColor(new Color(70, 60, 50));
		tabela.setFont(new Font("SansSerif", Font.PLAIN, 12));
		tabela.getTableHeader().setBackground(new Color(60, 50, 40));
		tabela.getTableHeader().setForeground(Color.WHITE);
		tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		tabela.setRowHeight(22);
		tabela.setShowHorizontalLines(true);

		// Centralizador para todas as colunas.
		DefaultTableCellRenderer centralizador = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellRendererComponent(
					JTable t, Object v, boolean sel, boolean foc, int r, int c) {
				super.getTableCellRendererComponent(t, v, sel, foc, r, c);
				setHorizontalAlignment(JLabel.CENTER);
				setBackground(r % 2 == 0 ? new Color(35, 30, 28) : new Color(45, 38, 35));
				setForeground(Color.WHITE);
				return this;
			}
		};
		for (int i = 0; i < COLUNAS.length; i++) {
			tabela.getColumnModel().getColumn(i).setCellRenderer(centralizador);
		}
		// Larguras das colunas
		int[] larguras = {45, 90, 130, 120, 130, 110, 130};
		for (int i = 0; i < larguras.length; i++) {
			tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
		}

		JScrollPane scroll = new JScrollPane(tabela);
		scroll.setPreferredSize(new Dimension(760, 300));
		scroll.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
		scroll.getViewport().setBackground(new Color(35, 30, 28));
		add(scroll, BorderLayout.CENTER);

		pack();
		setMinimumSize(new Dimension(780, 200));

		// Observer: registra-se na Fachada.
		fachada.adicionarObservador(this);
	}

	// =========================================================================
	// Observer
	// =========================================================================
	@Override
	public void notificar(Object evento) {
		if (evento == EventoJogo.EXIBIR_ANOTACOES) {
			setVisible(true);
			toFront();
		} else if (evento == EventoJogo.PALPITE_FEITO) {
			// Linha já foi registrada antes da notificação via registrarPalpite().
			atualizarTabela();
		} else if (evento == EventoJogo.PARTIDA_INICIADA) {
			historico.clear();
			turno = 0;
			modeloTabela.setRowCount(0);
		}
	}

	/**
	 * Deve ser chamado pelo Controller/View ANTES de chamar fazerPalpite(),
	 * para que os dados fiquem disponíveis quando o evento PALPITE_FEITO chegar.
	 */
	public void registrarPalpite(String jogador, String suspeito, String arma,
			String comodo, String refutador, String carta) {
		turno++;
		ultimoJogador   = jogador;
		ultimoSuspeito  = suspeito;
		ultimaArma      = arma;
		ultimoComodo    = comodo;
		ultimoRefutador = (refutador == null || refutador.isEmpty()) ? "-" : refutador;
		ultimaCarta     = (carta == null || carta.isEmpty()) ? "-" : carta;

		historico.add(new Object[]{
			turno, ultimoJogador, ultimoSuspeito, ultimaArma,
			ultimoComodo, ultimoRefutador, ultimaCarta
		});
		// Atualiza a tabela imediatamente, pois o resultado da refutação
		// só é conhecido APÓS o evento PALPITE_FEITO ter sido disparado.
		atualizarTabela();
	}

	// =========================================================================
	// Atualização da tabela
	// =========================================================================
	private void atualizarTabela() {
		modeloTabela.setRowCount(0);
		for (Object[] linha : historico) {
			modeloTabela.addRow(linha);
		}
		// Rola para a última linha.
		if (modeloTabela.getRowCount() > 0) {
			tabela.scrollRectToVisible(
				tabela.getCellRect(modeloTabela.getRowCount() - 1, 0, true));
		}
		repaint();
	}
}
