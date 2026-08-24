package it.reperibilita.dto;

import java.time.LocalDate;

/** Esito dell'assegnazione di un singolo giorno all'interno di una {@link TurnoBatchRequest}. */
public record TurnoBatchEsitoDTO(LocalDate data, String stato, String messaggio) {

    public static TurnoBatchEsitoDTO creato(LocalDate data) {
        return new TurnoBatchEsitoDTO(data, "CREATO", null);
    }

    public static TurnoBatchEsitoDTO aggiornato(LocalDate data) {
        return new TurnoBatchEsitoDTO(data, "AGGIORNATO", null);
    }

    public static TurnoBatchEsitoDTO saltato(LocalDate data) {
        return new TurnoBatchEsitoDTO(data, "SALTATO", "Giorno gia' assegnato");
    }

    public static TurnoBatchEsitoDTO errore(LocalDate data, String messaggio) {
        return new TurnoBatchEsitoDTO(data, "ERRORE", messaggio);
    }
}
