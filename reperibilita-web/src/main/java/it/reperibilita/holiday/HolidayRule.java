package it.reperibilita.holiday;

import it.reperibilita.domain.Festivita;

/**
 * Strategy: una regola capace di generare la festivita' che rappresenta per un
 * dato anno. Implementazioni: {@link FixedHolidayRule} per le festivita' a
 * data fissa, {@link EasterRelativeHolidayRule} per quelle mobili legate alla Pasqua.
 * Nuove regole (es. un patrono locale con logica particolare) si aggiungono
 * implementando questa interfaccia senza toccare il codice esistente (Open/Closed Principle).
 */
public interface HolidayRule {

    Festivita generaPer(int anno);
}
