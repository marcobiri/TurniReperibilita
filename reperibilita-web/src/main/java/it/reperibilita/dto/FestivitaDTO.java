package it.reperibilita.dto;

import java.time.LocalDate;

public record FestivitaDTO(LocalDate data, String descrizione, String tipo) {
}
