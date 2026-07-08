package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record TipoTurnoRequest(
        @NotBlank String nome,
        @NotNull LocalTime oraInizio,
        @NotNull LocalTime oraFine,
        @Positive int oreDurata,
        boolean festivo) {
}
