package it.reperibilita.holiday;

import it.reperibilita.domain.Festivita;
import it.reperibilita.domain.TipoFestivita;

import java.time.LocalDate;

/**
 * Festivita' mobile calcolata come offset in giorni rispetto alla domenica di
 * Pasqua (es. Pasquetta = Pasqua + 1). La data cambia ogni anno.
 */
public class EasterRelativeHolidayRule implements HolidayRule {

    private final int offsetGiorni;
    private final String descrizione;

    public EasterRelativeHolidayRule(int offsetGiorni, String descrizione) {
        this.offsetGiorni = offsetGiorni;
        this.descrizione = descrizione;
    }

    @Override
    public Festivita generaPer(int anno) {
        LocalDate pasqua = EasterCalculator.calcolaPasqua(anno);
        return new Festivita(pasqua.plusDays(offsetGiorni), descrizione, TipoFestivita.MOBILE);
    }
}
