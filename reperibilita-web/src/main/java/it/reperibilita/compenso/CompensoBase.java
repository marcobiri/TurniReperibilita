package it.reperibilita.compenso;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.domain.Turno;

import java.math.BigDecimal;

/** Componente base della decorazione: ore del turno per la tariffa oraria, senza maggiorazioni. */
public class CompensoBase implements CalcoloCompenso {

    private final Tariffa tariffa;

    public CompensoBase(Tariffa tariffa) {
        this.tariffa = tariffa;
    }

    @Override
    public BigDecimal calcola(Turno turno) {
        return tariffa.getImportoOrario().multiply(BigDecimal.valueOf(turno.getTipoTurno().getOreDurata()));
    }
}
