package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Richiesta di assegnazione dello stesso turno/operatore su tutti i giorni di un intervallo (selezione multipla sul calendario). */
public record TurnoBatchRequest(
        @NotNull LocalDate dal,
        @NotNull LocalDate al,
        @NotNull Long tipoTurnoId,
        @NotBlank String codiceOperatore,
        @NotBlank String servizio,
        boolean sovrascrivi) {
}
