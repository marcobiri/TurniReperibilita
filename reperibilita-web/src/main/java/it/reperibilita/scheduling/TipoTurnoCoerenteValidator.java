package it.reperibilita.scheduling;

import it.reperibilita.holiday.HolidayService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Verifica che il tipo di turno scelto sia coerente con il calendario: un turno
 * marcato come "festivo" puo' essere assegnato solo in una data che e' davvero
 * festiva (festivita' nazionale/personalizzata oppure sabato/domenica), e viceversa
 * un turno feriale non puo' cadere in un giorno festivo.
 */
@Component
@Order(2)
public class TipoTurnoCoerenteValidator extends AbstractTurnoValidator {

    private final HolidayService holidayService;

    public TipoTurnoCoerenteValidator(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @Override
    protected void controlla(RichiestaTurno richiesta) {
        boolean giornoFestivo = isGiornoFestivoOWeekend(richiesta.data());
        boolean turnoFestivo = richiesta.tipoTurno().isFestivo();

        if (turnoFestivo && !giornoFestivo) {
            throw new TurnoValidationException(
                    "Il turno \"" + richiesta.tipoTurno().getNome() + "\" e' un turno festivo ma "
                            + richiesta.data() + " non e' un giorno festivo o un weekend");
        }
        if (!turnoFestivo && giornoFestivo) {
            throw new TurnoValidationException(
                    "Il turno \"" + richiesta.tipoTurno().getNome() + "\" e' un turno feriale ma "
                            + richiesta.data() + " e' un giorno festivo o un weekend");
        }
    }

    private boolean isGiornoFestivoOWeekend(LocalDate data) {
        DayOfWeek giorno = data.getDayOfWeek();
        return giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY || holidayService.isFestivo(data);
    }
}
