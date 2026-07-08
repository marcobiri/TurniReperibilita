package it.reperibilita.dto;

import java.math.BigDecimal;

public record TariffaDTO(Long id, BigDecimal importoOrario, BigDecimal percentualeMaggiorazioneFestiva,
                          BigDecimal percentualeMaggiorazioneNotturna) {
}
