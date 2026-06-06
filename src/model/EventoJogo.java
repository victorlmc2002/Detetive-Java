package model;

// Tipos de evento enviados aos observadores no argumento de notificar(Object).
//
// Metade A (Padrões + Movimento) usa:
//   PARTIDA_INICIADA, DADOS_LANCADOS, PIAO_MOVIDO, TURNO_AVANCADO
//
// Os demais ficam reservados para a Metade B (palpite, acusação e as janelas
// de cartas e de anotações), mas já fazem parte do contrato comum entre a dupla.
public enum EventoJogo {
	PARTIDA_INICIADA,
	DADOS_LANCADOS,
	PIAO_MOVIDO,
	TURNO_AVANCADO,
	EXIBIR_CARTAS,
	EXIBIR_ANOTACOES,
	PALPITE_FEITO,
	FIM_DE_JOGO
}