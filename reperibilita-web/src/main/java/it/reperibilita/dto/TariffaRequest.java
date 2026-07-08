package it.reperibilita.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TariffaRequest(
        @NotNull @PositiveOrZero BigDecimal importoOrario,
        @NotNull @PositiveOrZero BigDecimal percentualeMaggiorazioneFestiva,
        @NotNull @PositiveOrZero BigDecimal percentualeMaggiorazioneNotturna) {
}
