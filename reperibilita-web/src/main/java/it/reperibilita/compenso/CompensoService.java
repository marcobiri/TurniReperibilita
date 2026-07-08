package it.reperibilita.compenso;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.domain.Turno;
import it.reperibilita.repository.TariffaRepository;
import it.reperibilita.scheduling.TurnoSearchCriteria;
import it.reperibilita.scheduling.TurnoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Facade per il calcolo dei compensi dovuti agli operatori in un periodo. */
@Service
public class CompensoService {

    private final TurnoService turnoService;
    private final TariffaRepository tariffaRepository;

    public CompensoService(TurnoService turnoService, TariffaRepository tariffaRepository) {
        this.turnoService = turnoService;
        this.tariffaRepository = tariffaRepository;
    }

    @Transactional(readOnly = true)
    public List<RigaCompenso> calcolaRighe(LocalDate dal, LocalDate al, String codiceOperatore) {
        Tariffa tariffa = tariffaCorrente();
        CalcoloCompenso calcolo = CalcoloCompensoFactory.creaPerTariffa(tariffa);

        TurnoSearchCriteria.Builder criteriBuilder = TurnoSearchCriteria.builder(dal, al);
        if (codiceOperatore != null && !codiceOperatore.isBlank()) {
            criteriBuilder.codiceOperatore(codiceOperatore);
        }

        List<Turno> turni = turnoService.cerca(criteriBuilder.build());
        return turni.stream()
                .map(t -> new RigaCompenso(t.getData(), t.getTipoTurno(), t.getOperatore(), t.getServizio(), calcolo.calcola(t)))
                .sorted(Comparator.comparing(RigaCompenso::data))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> totalePerOperatore(LocalDate dal, LocalDate al) {
        return calcolaRighe(dal, al, null).stream()
                .collect(Collectors.groupingBy(
                        riga -> riga.operatore().getCodice(),
                        Collectors.reducing(BigDecimal.ZERO, RigaCompenso::importo, BigDecimal::add)));
    }

    private Tariffa tariffaCorrente() {
        return tariffaRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> tariffaRepository.save(
                        new Tariffa(BigDecimal.valueOf(1.8), BigDecimal.valueOf(20), BigDecimal.valueOf(10))));
    }
}
