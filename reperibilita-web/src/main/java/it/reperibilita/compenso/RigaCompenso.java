package it.reperibilita.compenso;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.TipoTurno;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una riga del report compensi: un turno con il suo importo gia' calcolato. */
public record RigaCompenso(LocalDate data, TipoTurno tipoTurno, Operatore operatore,
                            Servizio servizio, BigDecimal importo) {
}
