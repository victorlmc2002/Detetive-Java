package model;

// Padrão Observer (Cap. 17): contrato do OBSERVADO (sujeito).
// A Fachada implementa esta interface: mantém a lista de observadores e os
// avisa sempre que o estado da partida muda, permitindo que a interface
// gráfica seja atualizada sem que o Model conheça a View.
public interface IObservado {
	void adicionarObservador(IObservador o);

	void removerObservador(IObservador o);

	void notificarObservadores(Object evento);
}