package it.reperibilita.scheduling;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.TipoTurno;
import it.reperibilita.domain.Turno;
import it.reperibilita.dto.RisultatoGenerazioneDTO;
import it.reperibilita.dto.TurnoPropostoDTO;
import it.reperibilita.holiday.HolidayService;
import it.reperibilita.repository.OperatoreRepository;
import it.reperibilita.repository.TipoTurnoRepository;
import it.reperibilita.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Genera la turnazione di un intero anno continuando il giro round-robin osservato nello
 * storico, invece di richiedere un ordine scelto a mano: per ogni servizio, gli operatori
 * attivi con almeno un turno recente vengono ordinati dal meno recente al piu' recente
 * (chi ha fatto turno per ultimo va per ultimo nel nuovo giro) e si assegna un operatore
 * per ogni blocco settimanale venerdi'-giovedi', lo stesso schema usato storicamente.
 * Non sovrascrive mai uno slot (data, tipoTurno, servizio) gia' occupato - lo segnala come
 * "gia' esistente" e lo salta, cosi' da non toccare inserimenti manuali fatti per esigenze
 * di servizio.
 */
@Service
public class GeneratoreTurniService {

    private static final int GIORNI_BLOCCO = 7;
    private static final int MESI_STORICO_ELEGGIBILITA = 12;

    private final TurnoRepository turnoRepository;
    private final OperatoreRepository operatoreRepository;
    private final TipoTurnoRepository tipoTurnoRepository;
    private final HolidayService holidayService;
    private final TurnoService turnoService;

    public GeneratoreTurniService(TurnoRepository turnoRepository, OperatoreRepository operatoreRepository,
                                   TipoTurnoRepository tipoTurnoRepository, HolidayService holidayService,
                                   TurnoService turnoService) {
        this.turnoRepository = turnoRepository;
        this.operatoreRepository = operatoreRepository;
        this.tipoTurnoRepository = tipoTurnoRepository;
        this.holidayService = holidayService;
        this.turnoService = turnoService;
    }

    public List<TurnoPropostoDTO> calcolaProposte(int anno, Servizio servizioFiltro) {
        List<TurnoPropostoDTO> proposte = new ArrayList<>();
        for (Servizio servizio : serviziDaGenerare(servizioFiltro)) {
            proposte.addAll(calcolaProposteServizio(anno, servizio));
        }
        return proposte;
    }

    @Transactional
    public RisultatoGenerazioneDTO generaEPersisti(int anno, Servizio servizioFiltro) {
        int creati = 0;
        int saltati = 0;
        for (TurnoPropostoDTO proposta : calcolaProposte(anno, servizioFiltro)) {
            if (proposta.giaEsistente()) {
                saltati++;
                continue;
            }
            turnoService.assegna(proposta.data(), proposta.tipoTurnoId(), proposta.codiceOperatore(),
                    Servizio.valueOf(proposta.servizio()));
            creati++;
        }
        return new RisultatoGenerazioneDTO(anno, creati, saltati);
    }

    private List<Servizio> serviziDaGenerare(Servizio filtro) {
        return filtro != null ? List.of(filtro) : List.of(Servizio.values());
    }

    private List<TurnoPropostoDTO> calcolaProposteServizio(int anno, Servizio servizio) {
        List<Operatore> ordineRotazione = ordineRotazione(anno, servizio);
        if (ordineRotazione.isEmpty()) {
            return List.of();
        }

        LocalDate inizioAnno = LocalDate.of(anno, 1, 1);
        LocalDate fineAnno = LocalDate.of(anno, 12, 31);
        LocalDate primoBlocco = inizioAnno.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));

        TipoTurno feriale = tipoTurnoFeriale();
        List<TipoTurno> festivi = tipiTurnoFestivi();

        List<TurnoPropostoDTO> proposte = new ArrayList<>();
        LocalDate inizioBlocco = primoBlocco;
        int indiceBlocco = 0;
        while (!inizioBlocco.isAfter(fineAnno)) {
            Operatore operatore = ordineRotazione.get(indiceBlocco % ordineRotazione.size());
            for (int i = 0; i < GIORNI_BLOCCO; i++) {
                LocalDate giorno = inizioBlocco.plusDays(i);
                if (giorno.isBefore(inizioAnno) || giorno.isAfter(fineAnno)) {
                    continue;
                }
                List<TipoTurno> tipiGiorno = isFestivoOWeekend(giorno) ? festivi : List.of(feriale);
                for (TipoTurno tipo : tipiGiorno) {
                    proposte.add(creaProposta(giorno, tipo, operatore, servizio));
                }
            }
            inizioBlocco = inizioBlocco.plusDays(GIORNI_BLOCCO);
            indiceBlocco++;
        }
        return proposte;
    }

    private TurnoPropostoDTO creaProposta(LocalDate giorno, TipoTurno tipo, Operatore operatore, Servizio servizio) {
        boolean giaEsistente = turnoRepository
                .findByDataAndTipoTurnoIdAndServizio(giorno, tipo.getId(), servizio)
                .isPresent();
        return new TurnoPropostoDTO(giorno, servizio.name(), tipo.getId(), tipo.getNome(),
                operatore.getCodice(), operatore.getNomeCompleto(), giaEsistente);
    }

    private boolean isFestivoOWeekend(LocalDate data) {
        DayOfWeek giorno = data.getDayOfWeek();
        return giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY || holidayService.isFestivo(data);
    }

    /**
     * Operatori attivi con almeno un turno per il servizio negli ultimi 12 mesi prima del
     * 1 gennaio dell'anno da generare, ordinati dal meno recente (prossimo di turno) al piu'
     * recente. Derivare l'elenco dai dati, invece di elencare a mano chi e' in rotazione,
     * fa si' che un operatore che ha smesso di fare reperibilita' ne esca da solo.
     */
    private List<Operatore> ordineRotazione(int anno, Servizio servizio) {
        LocalDate sogliaEleggibilita = LocalDate.of(anno, 1, 1).minusMonths(MESI_STORICO_ELEGGIBILITA);
        List<Turno> turniRecenti = turnoRepository.findByServizioAndDataAfterOrderByDataDesc(servizio, sogliaEleggibilita);

        Set<String> codiciAttivi = operatoreRepository.findByAttivoTrueOrderByCognomeAscNomeAsc().stream()
                .map(Operatore::getCodice)
                .collect(Collectors.toSet());

        Map<String, Operatore> operatorePerCodice = new LinkedHashMap<>();
        Map<String, LocalDate> ultimaDataPerCodice = new LinkedHashMap<>();
        for (Turno turno : turniRecenti) {
            String codice = turno.getOperatore().getCodice();
            if (!codiciAttivi.contains(codice) || ultimaDataPerCodice.containsKey(codice)) {
                continue; // ordinato per data desc: la prima occorrenza per codice e' gia' la piu' recente
            }
            ultimaDataPerCodice.put(codice, turno.getData());
            operatorePerCodice.put(codice, turno.getOperatore());
        }

        return operatorePerCodice.values().stream()
                .sorted(Comparator.comparing(o -> ultimaDataPerCodice.get(o.getCodice())))
                .toList();
    }

    private TipoTurno tipoTurnoFeriale() {
        return tipoTurnoRepository.findAll().stream()
                .filter(t -> !t.isFestivo())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nessun tipo turno feriale configurato"));
    }

    private List<TipoTurno> tipiTurnoFestivi() {
        return tipoTurnoRepository.findAll().stream()
                .filter(TipoTurno::isFestivo)
                .toList();
    }
}
