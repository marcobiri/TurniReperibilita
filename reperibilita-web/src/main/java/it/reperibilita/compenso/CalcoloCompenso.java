package it.reperibilita.compenso;

import it.reperibilita.domain.Turno;

import java.math.BigDecimal;

/**
 * Calcola il compenso dovuto per un turno. Implementata dalla componente base
 * {@link CompensoBase} e arricchibile con maggiorazioni tramite il pattern Decorator
 * (vedi {@link CompensoDecorator}), cosi' da poter comporre in futuro nuove
 * maggiorazioni (es. reperibilita' prolungata) senza toccare il codice esistente.
 */
public interface CalcoloCompenso {

    BigDecimal calcola(Turno turno);
}
