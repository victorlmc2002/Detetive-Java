package model;

// Toda classe da View que precise refletir mudanças no estado de uma partida
// implementa esta interface e se registra em um IObservado (a Fachada).
//
// A notificação recebe:
//   - origem: a referência do observado.
//     (o observador pode consultar dados via origem.algumGetter()).
//   - evento: identificador do que mudou (ver EventoJogo), permitindo
//     reação seletiva no observador.
public interface IObservador {
	void notificar(IObservado origem, Object evento);
}