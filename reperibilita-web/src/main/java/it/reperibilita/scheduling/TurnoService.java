package it.reperibilita.scheduling;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.TipoTurno;
import it.reperibilita.domain.Turno;
import it.reperibilita.dto.TurnoBatchEsitoDTO;
import it.reperibilita.repository.OperatoreRepository;
import it.reperibilita.repository.TipoTurnoRepository;
import it.reperibilita.repository.TurnoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Facade: e' l'unico punto di ingresso usato dai controller per leggere/scrivere
 * turni. Nasconde ai chiamanti la catena di validazione, il caricamento delle
 * entita' collegate (operatore, tipo turno) e la logica di upsert sullo slot
 * (data, tipoTurno, servizio).
 */
@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final OperatoreRepository operatoreRepository;
    private final TipoTurnoRepository tipoTurnoRepository;
    private final TurnoValidationChain validationChain;

    public TurnoService(TurnoRepository turnoRepository, OperatoreRepository operatoreRepository,
                         TipoTurnoRepository tipoTurnoRepository, TurnoValidationChain validationChain) {
        this.turnoRepository = turnoRepository;
        this.operatoreRepository = operatoreRepository;
        this.tipoTurnoRepository = tipoTurnoRepository;
        this.validationChain = validationChain;
    }

    @Transactional(readOnly = true)
    public List<Turno> cerca(TurnoSearchCriteria criteri) {
        List<Turno> turni = criteri.getServizio() != null
                ? turnoRepository.findByDataBetweenAndServizioOrderByDataAscTipoTurnoIdAsc(
                        criteri.getDal(), criteri.getAl(), criteri.getServizio())
                : turnoRepository.findByDataBetweenOrderByDataAscTipoTurnoIdAsc(criteri.getDal(), criteri.getAl());

        if (criteri.getCodiceOperatore() == null) {
            return turni;
        }
        return turni.stream()
                .filter(t -> t.getOperatore().getCodice().equals(criteri.getCodiceOperatore()))
                .toList();
    }

    @Transactional
    public Turno assegna(LocalDate data, Long tipoTurnoId, String codiceOperatore, Servizio servizio) {
        TipoTurno tipoTurno = caricaTipoTurno(tipoTurnoId);
        Operatore operatore = caricaOperatore(codiceOperatore);
        return upsert(data, tipoTurno, operatore, servizio);
    }

    @Transactional
    public void rimuovi(Long turnoId) {
        turnoRepository.deleteById(turnoId);
    }

    /**
     * Assegna lo stesso operatore ai turni diurno e notturno festivo dello stesso giorno (es.
     * un sabato: "Diurno festivo" 08-20 e "Notturno festivo" 20-08) come un'unica operazione
     * atomica: restano due {@link Turno} distinti in database (vedi
     * {@link it.reperibilita.mapper.TurnoMapper#toEventi}), ma qui vengono scritti insieme,
     * cosi' sul calendario quel giorno mostra una sola riga per servizio invece di due.
     */
    @Transactional
    public List<Turno> assegnaGiornata(LocalDate data, Long tipoTurnoDiurnoId, Long tipoTurnoNotturnoId,
                                        String codiceOperatore, Servizio servizio) {
        TipoTurno tipoDiurno = caricaTipoTurno(tipoTurnoDiurnoId);
        TipoTurno tipoNotturno = caricaTipoTurno(tipoTurnoNotturnoId);
        Operatore operatore = caricaOperatore(codiceOperatore);
        Turno turnoDiurno = upsert(data, tipoDiurno, operatore, servizio);
        Turno turnoNotturno = upsert(data, tipoNotturno, operatore, servizio);
        return List.of(turnoDiurno, turnoNotturno);
    }

    @Transactional
    public void rimuoviGiornata(LocalDate data, Long tipoTurnoDiurnoId, Long tipoTurnoNotturnoId, Servizio servizio) {
        turnoRepository.findByDataAndTipoTurnoIdAndServizio(data, tipoTurnoDiurnoId, servizio)
                .ifPresent(t -> turnoRepository.deleteById(t.getId()));
        turnoRepository.findByDataAndTipoTurnoIdAndServizio(data, tipoTurnoNotturnoId, servizio)
                .ifPresent(t -> turnoRepository.deleteById(t.getId()));
    }

    /**
     * Assegna lo stesso operatore/tipo turno a tutti i giorni di un intervallo (selezione multipla
     * sul calendario). A differenza di {@link #assegna}, non interrompe l'intero intervallo se un
     * singolo giorno non e' valido (es. turno feriale su un giorno festivo): l'errore di validazione
     * di quel giorno viene catturato e riportato singolarmente nell'esito, con lo stesso spirito
     * tollerante di {@link GeneratoreTurniService}.
     */
    @Transactional
    public List<TurnoBatchEsitoDTO> assegnaIntervallo(LocalDate dal, LocalDate al, Long tipoTurnoId,
                                                        String codiceOperatore, Servizio servizio, boolean sovrascrivi) {
        if (al.isBefore(dal)) {
            throw new IllegalArgumentException("La data finale non puo' precedere quella iniziale");
        }
        TipoTurno tipoTurno = caricaTipoTurno(tipoTurnoId);
        Operatore operatore = caricaOperatore(codiceOperatore);

        List<TurnoBatchEsitoDTO> esiti = new ArrayList<>();
        for (LocalDate data = dal; !data.isAfter(al); data = data.plusDays(1)) {
            Optional<Turno> esistente = turnoRepository.findByDataAndTipoTurnoIdAndServizio(data, tipoTurnoId, servizio);
            if (esistente.isPresent() && !sovrascrivi) {
                esiti.add(TurnoBatchEsitoDTO.saltato(data));
                continue;
            }
            try {
                boolean eraPresente = esistente.isPresent();
                upsert(data, tipoTurno, operatore, servizio);
                esiti.add(eraPresente ? TurnoBatchEsitoDTO.aggiornato(data) : TurnoBatchEsitoDTO.creato(data));
            } catch (TurnoValidationException e) {
                esiti.add(TurnoBatchEsitoDTO.errore(data, e.getMessage()));
            }
        }
        return esiti;
    }

    @Transactional(readOnly = true)
    public List<Turno> turniOperatore(String codiceOperatore, java.time.LocalDate dal, java.time.LocalDate al) {
        return turnoRepository.findByOperatoreAndPeriodo(codiceOperatore, dal, al);
    }

    private TipoTurno caricaTipoTurno(Long tipoTurnoId) {
        return tipoTurnoRepository.findById(tipoTurnoId)
                .orElseThrow(() -> new EntityNotFoundException("Tipo turno non trovato: " + tipoTurnoId));
    }

    private Operatore caricaOperatore(String codiceOperatore) {
        return operatoreRepository.findById(codiceOperatore)
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + codiceOperatore));
    }

    private Turno upsert(LocalDate data, TipoTurno tipoTurno, Operatore operatore, Servizio servizio) {
        Optional<Turno> esistente = turnoRepository.findByDataAndTipoTurnoIdAndServizio(data, tipoTurno.getId(), servizio);

        RichiestaTurno richiesta = new RichiestaTurno(data, tipoTurno, operatore, servizio,
                esistente.map(Turno::getId).orElse(null));
        validationChain.valida(richiesta);

        Turno turno = esistente.orElseGet(() -> new Turno(data, tipoTurno, operatore, servizio));
        turno.setData(data);
        turno.setTipoTurno(tipoTurno);
        turno.setOperatore(operatore);
        turno.setServizio(servizio);
        return turnoRepository.save(turno);
    }
}
