package it.reperibilita.scheduling;

import it.reperibilita.domain.Operatore;
import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.TipoTurno;
import it.reperibilita.domain.Turno;
import it.reperibilita.repository.OperatoreRepository;
import it.reperibilita.repository.TipoTurnoRepository;
import it.reperibilita.repository.TurnoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        TipoTurno tipoTurno = tipoTurnoRepository.findById(tipoTurnoId)
                .orElseThrow(() -> new EntityNotFoundException("Tipo turno non trovato: " + tipoTurnoId));
        Operatore operatore = operatoreRepository.findById(codiceOperatore)
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + codiceOperatore));

        Optional<Turno> esistente = turnoRepository.findByDataAndTipoTurnoIdAndServizio(data, tipoTurnoId, servizio);

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

    @Transactional
    public void rimuovi(Long turnoId) {
        turnoRepository.deleteById(turnoId);
    }

    @Transactional(readOnly = true)
    public List<Turno> turniOperatore(String codiceOperatore, java.time.LocalDate dal, java.time.LocalDate al) {
        return turnoRepository.findByOperatoreAndPeriodo(codiceOperatore, dal, al);
    }
}
