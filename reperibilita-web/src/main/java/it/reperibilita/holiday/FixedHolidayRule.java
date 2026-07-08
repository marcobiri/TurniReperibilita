package it.reperibilita.holiday;

import it.reperibilita.domain.Festivita;
import it.reperibilita.domain.TipoFestivita;

import java.time.LocalDate;
import java.time.Month;

/**
 * Festivita' che cade sempre nello stesso giorno/mese, indipendentemente dall'anno
 * (es. Capodanno, Natale).
 */
public class FixedHolidayRule implements HolidayRule {

    private final Month mese;
    private final int giorno;
    private final String descrizione;

    public FixedHolidayRule(Month mese, int giorno, String descrizione) {
        this.mese = mese;
        this.giorno = giorno;
        this.descrizione = descrizione;
    }

    @Override
    public Festivita generaPer(int anno) {
        return new Festivita(LocalDate.of(anno, mese, giorno), descrizione, TipoFestivita.FISSA);
    }
}
