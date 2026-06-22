package controller;

import javax.swing.SwingUtilities;

import view.JanelaInicio;
import view.Recursos;

// Ponto de entrada da aplicação.
// Carrega imagens uma única vez e abre a JanelaInicio na EDT
public class App {

	public static void main(String[] args) {
		Recursos.carregar();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JanelaInicio().setVisible(true);
			}
		});
	}
}
