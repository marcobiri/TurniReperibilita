package it.reperibilita.dto;

import java.time.LocalDate;

public record TurnoDTO(Long id, LocalDate data, TipoTurnoDTO tipoTurno, OperatoreDTO operatore, String servizio) {
}
