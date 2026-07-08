package it.reperibilita.compenso;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.domain.Turno;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aggiunge la percentuale di maggiorazione festiva se il tipo di turno e' marcato
 * come festivo. Si appoggia al flag {@code TipoTurno.festivo} anziche' ricalcolare
 * la festivita' dal calendario: la coerenza fra i due e' gia' garantita in fase di
 * assegnazione da {@code TipoTurnoCoerenteValidator}.
 */
public class MaggiorazioneFestivaDecorator extends CompensoDecorator {

    private final Tariffa tariffa;

    public MaggiorazioneFestivaDecorator(CalcoloCompenso delegato, Tariffa tariffa) {
        super(delegato);
        this.tariffa = tariffa;
    }

    @Override
    public BigDecimal calcola(Turno turno) {
        BigDecimal compenso = delegato.calcola(turno);
        if (!turno.getTipoTurno().isFestivo()) {
            return compenso;
        }
        BigDecimal maggiorazione = compenso
                .multiply(tariffa.getPercentualeMaggiorazioneFestiva())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return compenso.add(maggiorazione);
    }
}
