package model;

// Padrão Observer (Cap. 17): contrato do OBSERVADOR.
// Toda classe da View que precise refletir mudanças no estado de uma partida
// implementa esta interface e se registra em um IObservado (a Fachada).
//
// A notificação recebe:
//   - origem: a referência do observado, para callback no estilo do Cap. 17
//     (o observador pode consultar dados via origem.algumGetter()).
//   - evento: identificador do que mudou (ver EventoJogo), permitindo
//     reação seletiva no observador.
public interface IObservador {
	void notificar(IObservado origem, Object evento);
}