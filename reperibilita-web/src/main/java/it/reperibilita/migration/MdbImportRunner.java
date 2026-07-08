package it.reperibilita.migration;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.Tariffa;
import it.reperibilita.domain.TipoTurno;
import it.reperibilita.domain.Turno;
import it.reperibilita.repository.AbbinamentoOperatoriRepository;
import it.reperibilita.repository.OperatoreRepository;
import it.reperibilita.repository.TariffaRepository;
import it.reperibilita.repository.TipoTurnoRepository;
import it.reperibilita.repository.TurnoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Importazione una tantum del vecchio Turni.mdb nel nuovo database. Si attiva
 * solo se la proprieta' "import.mdb.path" e' valorizzata (vedi application.yml),
 * ed e' idempotente: puo' essere rilanciata piu' volte senza duplicare i dati
 * gia' importati.
 *
 * Esempio di avvio:
 * mvn spring-boot:run -Dspring-boot.run.arguments=--import.mdb.path=/percorso/turni.mdb
 */
@Component
@Order(2)
public class MdbImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MdbImportRunner.class);

    private final String mdbPath;
    private final OperatoreRepository operatoreRepository;
    private final TipoTurnoRepository tipoTurnoRepository;
    private final TurnoRepository turnoRepository;
    private final AbbinamentoOperatoriRepository abbinamentoRepository;
    private final TariffaRepository tariffaRepository;

    public MdbImportRunner(@Value("${import.mdb.path:}") String mdbPath,
                            OperatoreRepository operatoreRepository,
                            TipoTurnoRepository tipoTurnoRepository,
                            TurnoRepository turnoRepository,
                            AbbinamentoOperatoriRepository abbinamentoRepository,
                            TariffaRepository tariffaRepository) {
        this.mdbPath = mdbPath;
        this.operatoreRepository = operatoreRepository;
        this.tipoTurnoRepository = tipoTurnoRepository;
        this.turnoRepository = turnoRepository;
        this.abbinamentoRepository = abbinamentoRepository;
        this.tariffaRepository = tariffaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (mdbPath == null || mdbPath.isBlank()) {
            return;
        }
        File file = new File(mdbPath);
        if (!file.exists()) {
            log.warn("File Access da importare non trovato: {}", mdbPath);
            return;
        }

        log.info("Avvio importazione da {}", mdbPath);
        try (LegacyMdbReader reader = new LegacyMdbReader(file)) {
            int operatoriImportati = importaOperatori(reader);
            Map<Integer, TipoTurno> mappaTipiTurno = importaTipiTurno(reader);
            int turniReperibilita = importaTurni(reader, "TAB_TURNI", Servizio.REPERIBILITA, mappaTipiTurno);
            int turniFonia = importaTurni(reader, "TAB_TURNI_FONIA", Servizio.FONIA, mappaTipiTurno);
            int abbinamenti = importaAbbinamenti(reader);
            boolean tariffaImportata = importaTariffa(reader);

            log.info("Importazione completata: {} operatori, {} tipi turno, {} turni reperibilita, "
                            + "{} turni fonia, {} abbinamenti, tariffa importata={}",
                    operatoriImportati, mappaTipiTurno.size(), turniReperibilita, turniFonia, abbinamenti, tariffaImportata);
        }
    }

    private int importaOperatori(LegacyMdbReader reader) throws Exception {
        int importati = 0;
        for (OperatoreLegacy legacy : reader.leggiOperatori()) {
            if (operatoreRepository.existsById(legacy.codice())) {
                continue;
            }
            operatoreRepository.save(new Operatore(legacy.codice(), legacy.cognome(), legacy.nome(),
                    legacy.telefonoAziendale(), legacy.telefonoCasa(), legacy.attivo()));
            importati++;
        }
        return importati;
    }

    private Map<Integer, TipoTurno> importaTipiTurno(LegacyMdbReader reader) throws Exception {
        Map<Integer, TipoTurno> mappa = new HashMap<>();
        for (TipoTurnoLegacy legacy : reader.leggiTipiTurno()) {
            LocalTime[] orario = parseOrario(legacy.orario());
            TipoTurno tipoTurno = tipoTurnoRepository.findByNomeIgnoreCase(legacy.nome())
                    .orElseGet(() -> tipoTurnoRepository.save(
                            new TipoTurno(legacy.nome(), orario[0], orario[1], legacy.ore(), legacy.festivo())));
            mappa.put(legacy.idTurno(), tipoTurno);
        }
        return mappa;
    }

    private int importaTurni(LegacyMdbReader reader, String nomeTabella, Servizio servizio,
                              Map<Integer, TipoTurno> mappaTipiTurno) throws Exception {
        // Le righe da importare sono migliaia: si precarica l'insieme delle chiavi (data, tipoTurno)
        // gia' presenti con un'unica query, invece di interrogare il database riga per riga dentro
        // il ciclo. Interrogare durante un'unica transazione che accumula via via migliaia di
        // entita' appena salvate costringerebbe Hibernate a ricontrollare l'intero contesto di
        // persistenza (dirty checking) prima di ogni query, con un costo che cresce quadraticamente
        // con il numero di righe.
        Set<String> chiaviEsistenti = turnoRepository.findByServizio(servizio).stream()
                .map(t -> chiaveSlot(t.getData(), t.getTipoTurno().getId()))
                .collect(Collectors.toCollection(HashSet::new));

        int importati = 0;
        for (TurnoLegacy legacy : reader.leggiTurni(nomeTabella)) {
            TipoTurno tipoTurno = mappaTipiTurno.get(legacy.codTurno());
            if (tipoTurno == null) {
                log.warn("Tipo turno {} sconosciuto, riga {} {} ignorata", legacy.codTurno(), nomeTabella, legacy.data());
                continue;
            }
            var operatore = operatoreRepository.findById(legacy.codOperatore());
            if (operatore.isEmpty()) {
                log.warn("Operatore {} sconosciuto, riga {} {} ignorata", legacy.codOperatore(), nomeTabella, legacy.data());
                continue;
            }
            String chiave = chiaveSlot(legacy.data(), tipoTurno.getId());
            if (!chiaviEsistenti.add(chiave)) {
                continue;
            }
            turnoRepository.save(new Turno(legacy.data(), tipoTurno, operatore.get(), servizio));
            importati++;
        }
        return importati;
    }

    private int importaAbbinamenti(LegacyMdbReader reader) throws Exception {
        int importati = 0;
        for (AbbinamentoLegacy legacy : reader.leggiAbbinamenti()) {
            var primo = operatoreRepository.findById(legacy.primoOperatore());
            var secondo = operatoreRepository.findById(legacy.secondoOperatore());
            if (primo.isEmpty() || secondo.isEmpty()) {
                continue;
            }
            boolean esiste = abbinamentoRepository.findByPrimoOperatoreCodice(legacy.primoOperatore()).stream()
                    .anyMatch(a -> a.getSecondoOperatore().getCodice().equals(legacy.secondoOperatore()));
            if (esiste) {
                continue;
            }
            abbinamentoRepository.save(new it.reperibilita.domain.AbbinamentoOperatori(primo.get(), secondo.get()));
            importati++;
        }
        return importati;
    }

    private boolean importaTariffa(LegacyMdbReader reader) throws Exception {
        if (!tariffaRepository.findAll().isEmpty()) {
            return false;
        }
        return reader.leggiTariffaOraria()
                .map(importo -> {
                    tariffaRepository.save(new Tariffa(importo, BigDecimal.valueOf(20), BigDecimal.valueOf(10)));
                    return true;
                })
                .orElse(false);
    }

    private static String chiaveSlot(LocalDate data, Long tipoTurnoId) {
        return data + "|" + tipoTurnoId;
    }

    private static LocalTime[] parseOrario(String orario) {
        if (orario == null) {
            return new LocalTime[]{LocalTime.MIDNIGHT, LocalTime.MIDNIGHT};
        }
        String[] parti = orario.split("-");
        LocalTime inizio = parseOra(parti[0]);
        LocalTime fine = parti.length > 1 ? parseOra(parti[1]) : inizio;
        return new LocalTime[]{inizio, fine};
    }

    private static LocalTime parseOra(String testo) {
        String pulito = testo.trim().replace('.', ':');
        return LocalTime.parse(pulito);
    }
}
