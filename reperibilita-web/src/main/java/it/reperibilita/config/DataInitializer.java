package it.reperibilita.config;

import it.reperibilita.domain.TipoTurno;
import it.reperibilita.holiday.HolidayService;
import it.reperibilita.repository.TipoTurnoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Seed dei dati minimi necessari al primo avvio: i tre tipi di turno storici
 * (uguali a TAB_COD_TURNI del vecchio Turni.mdb) e il calendario festivo
 * dell'anno corrente, cosi' l'applicazione e' subito utilizzabile anche senza
 * importare il vecchio database.
 */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final TipoTurnoRepository tipoTurnoRepository;
    private final HolidayService holidayService;

    public DataInitializer(TipoTurnoRepository tipoTurnoRepository, HolidayService holidayService) {
        this.tipoTurnoRepository = tipoTurnoRepository;
        this.holidayService = holidayService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedTipiTurno();
        holidayService.assicuraCalendarioAnno(LocalDate.now().getYear());
    }

    private void seedTipiTurno() {
        if (tipoTurnoRepository.count() > 0) {
            return;
        }
        tipoTurnoRepository.save(new TipoTurno("Notturno feriale", LocalTime.of(20, 0), LocalTime.of(8, 0), 12, false));
        tipoTurnoRepository.save(new TipoTurno("Diurno festivo", LocalTime.of(8, 0), LocalTime.of(20, 0), 12, true));
        tipoTurnoRepository.save(new TipoTurno("Notturno festivo", LocalTime.of(20, 0), LocalTime.of(8, 0), 12, true));
    }
}
