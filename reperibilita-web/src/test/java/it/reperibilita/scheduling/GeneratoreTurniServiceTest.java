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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lo storico usato in questi test riproduce la coda REPERIBILITA di fine 2026 (vedi analisi
 * nel piano): l'ultimo blocco e' stato di E, quindi il prossimo operatore "dovuto" (il meno
 * recente) e' H, seguito da I, poi G, poi di nuovo E.
 */
@ExtendWith(MockitoExtension.class)
class GeneratoreTurniServiceTest {

    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private OperatoreRepository operatoreRepository;
    @Mock
    private TipoTurnoRepository tipoTurnoRepository;
    @Mock
    private HolidayService holidayService;
    @Mock
    private TurnoService turnoService;

    private GeneratoreTurniService generatore;

    private TipoTurno feriale;
    private TipoTurno diurnoFestivo;
    private TipoTurno notturnoFestivo;

    private Operatore g;
    private Operatore i;
    private Operatore h;
    private Operatore e;
    private Operatore d;

    @BeforeEach
    void setUp() {
        generatore = new GeneratoreTurniService(turnoRepository, operatoreRepository, tipoTurnoRepository,
                holidayService, turnoService);

        feriale = new TipoTurno("Notturno feriale", LocalTime.of(20, 0), LocalTime.of(8, 0), 12, false);
        ReflectionTestUtils.setField(feriale, "id", 1L);
        diurnoFestivo = new TipoTurno("Diurno festivo", LocalTime.of(8, 0), LocalTime.of(20, 0), 12, true);
        ReflectionTestUtils.setField(diurnoFestivo, "id", 2L);
        notturnoFestivo = new TipoTurno("Notturno festivo", LocalTime.of(20, 0), LocalTime.of(8, 0), 12, true);
        ReflectionTestUtils.setField(notturnoFestivo, "id", 3L);
        lenient().when(tipoTurnoRepository.findAll()).thenReturn(List.of(feriale, diurnoFestivo, notturnoFestivo));

        g = new Operatore("G", "Perugini", "Giorgio", null, null, true);
        i = new Operatore("I", "Molari", "Luca", null, null, true);
        h = new Operatore("H", "Biribao", "Marco", null, null, true);
        e = new Operatore("E", "Marcotulli", "Riccardo", null, null, true);
        d = new Operatore("D", "Molari", "Roberto", null, null, true); // attivo in anagrafica ma senza turni recenti

        lenient().when(operatoreRepository.findByAttivoTrueOrderByCognomeAscNomeAsc())
                .thenReturn(List.of(d, h, i, g, e));

        lenient().when(holidayService.isFestivo(any())).thenReturn(false);
        lenient().when(holidayService.isFestivo(LocalDate.of(2027, 1, 1))).thenReturn(true); // Capodanno

        lenient().when(turnoRepository.findByDataAndTipoTurnoIdAndServizio(any(), any(), any()))
                .thenReturn(Optional.empty());
    }

    private Turno turno(LocalDate data, Operatore operatore) {
        return new Turno(data, feriale, operatore, Servizio.REPERIBILITA);
    }

    private void conStoricoCompleto() {
        // Ordine dal piu' vecchio al piu' recente: H, I, G, E -> prossimo dovuto: H
        when(turnoRepository.findByServizioAndDataAfterOrderByDataDesc(eq(Servizio.REPERIBILITA), any()))
                .thenReturn(List.of(
                        turno(LocalDate.of(2026, 12, 27), e),
                        turno(LocalDate.of(2026, 12, 20), g),
                        turno(LocalDate.of(2026, 12, 13), i),
                        turno(LocalDate.of(2026, 12, 6), h)));
    }

    @Test
    void continuaIlGiroDalMenoRecente() {
        conStoricoCompleto();

        List<TurnoPropostoDTO> proposte = generatore.calcolaProposte(2027, Servizio.REPERIBILITA);

        // Il primo blocco (venerdi' 1 gennaio 2027, Capodanno) deve andare a H: e' l'operatore
        // meno di recente in turno, quindi il prossimo dovuto nel giro.
        TurnoPropostoDTO capodanno = proposte.stream()
                .filter(p -> p.data().equals(LocalDate.of(2027, 1, 1)))
                .filter(p -> p.nomeTipoTurno().contains("festivo"))
                .findFirst().orElseThrow();
        assertThat(capodanno.codiceOperatore()).isEqualTo("H");
    }

    @Test
    void escludeOperatoreAttivoSenzaTurniRecenti() {
        conStoricoCompleto(); // D non compare mai nello storico recente

        List<TurnoPropostoDTO> proposte = generatore.calcolaProposte(2027, Servizio.REPERIBILITA);

        assertThat(proposte).extracting(TurnoPropostoDTO::codiceOperatore).doesNotContain("D");
    }

    @Test
    void assegnaTipoFestivoNelWeekendETipoFerialeInSettimana() {
        conStoricoCompleto();

        List<TurnoPropostoDTO> proposte = generatore.calcolaProposte(2027, Servizio.REPERIBILITA);

        // 2 gennaio 2027 e' un sabato: servono sia il diurno che il notturno festivo.
        List<TurnoPropostoDTO> sabato = proposte.stream()
                .filter(p -> p.data().equals(LocalDate.of(2027, 1, 2)))
                .toList();
        assertThat(sabato).extracting(TurnoPropostoDTO::nomeTipoTurno)
                .containsExactlyInAnyOrder("Diurno festivo", "Notturno festivo");

        // 4 gennaio 2027 e' un lunedi' feriale: solo il notturno feriale.
        List<TurnoPropostoDTO> lunedi = proposte.stream()
                .filter(p -> p.data().equals(LocalDate.of(2027, 1, 4)))
                .toList();
        assertThat(lunedi).extracting(TurnoPropostoDTO::nomeTipoTurno).containsExactly("Notturno feriale");
    }

    @Test
    void nonSovrascriveUnoSlotGiaEsistenteELoContaComeSaltato() {
        conStoricoCompleto();
        // Il 4 gennaio 2027 (feriale) e' gia' stato assegnato a mano a G.
        when(turnoRepository.findByDataAndTipoTurnoIdAndServizio(LocalDate.of(2027, 1, 4), feriale.getId(), Servizio.REPERIBILITA))
                .thenReturn(Optional.of(turno(LocalDate.of(2027, 1, 4), g)));

        RisultatoGenerazioneDTO risultato = generatore.generaEPersisti(2027, Servizio.REPERIBILITA);

        assertThat(risultato.saltati()).isEqualTo(1);
        verify(turnoService, never()).assegna(LocalDate.of(2027, 1, 4), feriale.getId(), "H", Servizio.REPERIBILITA);
    }
}
