package it.reperibilita.scheduling;

/** Sollevata da un {@link TurnoValidator} quando una richiesta di assegnazione turno non e' valida. */
public class TurnoValidationException extends RuntimeException {

    public TurnoValidationException(String message) {
        super(message);
    }
}
