package it.reperibilita.compenso;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.domain.Turno;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Aggiunge la percentuale di maggiorazione notturna se il turno cade nella fascia notte. */
public class MaggiorazioneNotturnaDecorator extends CompensoDecorator {

    private final Tariffa tariffa;

    public MaggiorazioneNotturnaDecorator(CalcoloCompenso delegato, Tariffa tariffa) {
        super(delegato);
        this.tariffa = tariffa;
    }

    @Override
    public BigDecimal calcola(Turno turno) {
        BigDecimal compenso = delegato.calcola(turno);
        if (!turno.getTipoTurno().isNotturno()) {
            return compenso;
        }
        BigDecimal maggiorazione = compenso
                .multiply(tariffa.getPercentualeMaggiorazioneNotturna())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return compenso.add(maggiorazione);
    }
}
