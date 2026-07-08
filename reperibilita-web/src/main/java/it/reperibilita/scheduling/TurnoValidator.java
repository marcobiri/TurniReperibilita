package it.reperibilita.scheduling;

/**
 * Un anello della catena di validazione (pattern Chain of Responsibility) applicata
 * prima di salvare un'assegnazione di turno. Ogni implementazione controlla un
 * singolo aspetto ed e' libera di interrompere la catena lanciando
 * {@link TurnoValidationException}; se il controllo passa, la richiesta prosegue
 * verso l'anello successivo.
 */
public interface TurnoValidator {

    void setSuccessivo(TurnoValidator successivo);

    void valida(RichiestaTurno richiesta);
}
