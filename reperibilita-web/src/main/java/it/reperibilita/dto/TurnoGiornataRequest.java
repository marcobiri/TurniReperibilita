package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Richiesta di assegnazione dei turni diurno+notturno festivo dello stesso giorno come un'unica giornata. */
public record TurnoGiornataRequest(
        @NotNull LocalDate data,
        @NotNull Long tipoTurnoDiurnoId,
        @NotNull Long tipoTurnoNotturnoId,
        @NotBlank String codiceOperatore,
        @NotBlank String servizio) {
}
