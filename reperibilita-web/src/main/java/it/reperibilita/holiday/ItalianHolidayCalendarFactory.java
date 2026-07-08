package it.reperibilita.holiday;

import it.reperibilita.domain.Festivita;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.List;

/**
 * Factory: assembla l'insieme delle regole che definiscono le festivita'
 * nazionali italiane e produce il calendario festivo completo per un dato anno.
 * Per aggiungere o togliere una festivita' nazionale basta modificare la lista
 * di regole qui, senza toccare {@link HolidayService} ne' le regole stesse.
 */
@Component
public class ItalianHolidayCalendarFactory {

    private final List<HolidayRule> regole = List.of(
            new FixedHolidayRule(Month.JANUARY, 1, "Capodanno"),
            new FixedHolidayRule(Month.JANUARY, 6, "Epifania"),
            new EasterRelativeHolidayRule(0, "Pasqua"),
            new EasterRelativeHolidayRule(1, "Lunedi dell'Angelo (Pasquetta)"),
            new FixedHolidayRule(Month.APRIL, 25, "Anniversario della Liberazione"),
            new FixedHolidayRule(Month.MAY, 1, "Festa dei Lavoratori"),
            new FixedHolidayRule(Month.JUNE, 2, "Festa della Repubblica"),
            new FixedHolidayRule(Month.AUGUST, 15, "Ferragosto"),
            new FixedHolidayRule(Month.NOVEMBER, 1, "Ognissanti"),
            new FixedHolidayRule(Month.DECEMBER, 8, "Immacolata Concezione"),
            new FixedHolidayRule(Month.DECEMBER, 25, "Natale"),
            new FixedHolidayRule(Month.DECEMBER, 26, "Santo Stefano")
    );

    public List<Festivita> generaCalendario(int anno) {
        return regole.stream()
                .map(regola -> regola.generaPer(anno))
                .toList();
    }
}
