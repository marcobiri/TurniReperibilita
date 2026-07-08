package it.reperibilita.holiday;

import it.reperibilita.domain.Festivita;
import it.reperibilita.domain.TipoFestivita;
import it.reperibilita.repository.FestivitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade che nasconde ai controller la generazione/persistenza delle festivita'.
 * Genera pigramente (lazy) il calendario nazionale di un anno la prima volta che
 * serve, e lascia intatte eventuali festivita' gia' presenti (comprese quelle
 * personalizzate aggiunte manualmente), cosi' da non sovrascrivere modifiche dell'utente.
 */
@Service
public class HolidayService {

    private final FestivitaRepository festivitaRepository;
    private final ItalianHolidayCalendarFactory calendarFactory;

    public HolidayService(FestivitaRepository festivitaRepository, ItalianHolidayCalendarFactory calendarFactory) {
        this.festivitaRepository = festivitaRepository;
        this.calendarFactory = calendarFactory;
    }

    @Transactional
    public void assicuraCalendarioAnno(int anno) {
        List<Festivita> calendarioNazionale = calendarFactory.generaCalendario(anno);
        for (Festivita festivita : calendarioNazionale) {
            if (!festivitaRepository.existsByData(festivita.getData())) {
                festivitaRepository.save(festivita);
            }
        }
    }

    @Transactional
    public List<Festivita> getFestivitaAnno(int anno) {
        assicuraCalendarioAnno(anno);
        return festivitaRepository.findByDataBetweenOrderByData(
                LocalDate.of(anno, 1, 1), LocalDate.of(anno, 12, 31));
    }

    @Transactional
    public List<Festivita> getFestivitaPeriodo(LocalDate dal, LocalDate al) {
        for (int anno = dal.getYear(); anno <= al.getYear(); anno++) {
            assicuraCalendarioAnno(anno);
        }
        return festivitaRepository.findByDataBetweenOrderByData(dal, al);
    }

    public boolean isFestivo(LocalDate data) {
        assicuraCalendarioAnno(data.getYear());
        return festivitaRepository.existsByData(data);
    }

    @Transactional
    public Festivita aggiungiPersonalizzata(LocalDate data, String descrizione) {
        Festivita festivita = new Festivita(data, descrizione, TipoFestivita.PERSONALIZZATA);
        return festivitaRepository.save(festivita);
    }

    @Transactional
    public void rimuovi(LocalDate data) {
        festivitaRepository.deleteById(data);
    }
}
