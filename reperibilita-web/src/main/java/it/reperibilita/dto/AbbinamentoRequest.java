package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;

public record AbbinamentoRequest(@NotBlank String primoOperatoreCodice, @NotBlank String secondoOperatoreCodice) {
}
