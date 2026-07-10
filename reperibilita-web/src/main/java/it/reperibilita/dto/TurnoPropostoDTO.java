package it.reperibilita.dto;

import java.time.LocalDate;

public record TurnoPropostoDTO(LocalDate data, String servizio, Long tipoTurnoId, String nomeTipoTurno,
                                String codiceOperatore, String nomeOperatore, boolean giaEsistente) {
}
