package model;

// Tipos de evento enviados aos observadores no argumento de notificar(Object).
public enum EventoJogo {
	PARTIDA_INICIADA,
	DADOS_LANCADOS,
	PIAO_MOVIDO,
	TURNO_AVANCADO,
	EXIBIR_CARTAS,
	EXIBIR_BLOCO_NOTAS,
	PALPITE_FEITO,
	FIM_DE_JOGO
}