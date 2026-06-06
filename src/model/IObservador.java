package model;

// Padrão Observer (Cap. 17): contrato do OBSERVADOR.
// Toda classe da View que precise refletir mudanças no estado de uma partida
// implementa esta interface e se registra em um IObservado (a Fachada).
// O argumento de notificar identifica o que mudou (ver EventoJogo).
public interface IObservador {
	void notificar(Object evento);
}