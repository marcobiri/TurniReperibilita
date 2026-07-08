package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TurnoRequest(
        @NotNull LocalDate data,
        @NotNull Long tipoTurnoId,
        @NotBlank String codiceOperatore,
        @NotBlank String servizio) {
}
