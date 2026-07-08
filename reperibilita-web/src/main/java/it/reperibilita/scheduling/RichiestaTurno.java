package it.reperibilita.scheduling;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.TipoTurno;

import java.time.LocalDate;

/**
 * Richiesta di assegnazione di un turno, gia' risolta sugli oggetti di dominio
 * (non sui semplici id ricevuti dall'API). E' l'oggetto che passa attraverso
 * la catena di validatori in {@link TurnoValidationChain}.
 *
 * @param turnoIdEscluso id del turno da escludere dai controlli, usato in fase di
 *                        modifica per non confrontare un turno con se stesso; null in creazione.
 */
public record RichiestaTurno(LocalDate data, TipoTurno tipoTurno, Operatore operatore,
                              Servizio servizio, Long turnoIdEscluso) {
}
