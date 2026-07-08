package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FestivitaRequest(@NotNull LocalDate data, @NotBlank String descrizione) {
}
