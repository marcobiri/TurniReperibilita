package it.reperibilita.web.api;

import it.reperibilita.domain.Operatore;
import it.reperibilita.dto.OperatoreDTO;
import it.reperibilita.dto.OperatoreRequest;
import it.reperibilita.mapper.OperatoreMapper;
import it.reperibilita.repository.OperatoreRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operatori")
public class OperatoreRestController {

    private final OperatoreRepository operatoreRepository;

    public OperatoreRestController(OperatoreRepository operatoreRepository) {
        this.operatoreRepository = operatoreRepository;
    }

    @GetMapping
    public List<OperatoreDTO> elenco(@RequestParam(required = false) Boolean soloAttivi) {
        List<Operatore> operatori = Boolean.TRUE.equals(soloAttivi)
                ? operatoreRepository.findByAttivoTrueOrderByCognomeAscNomeAsc()
                : operatoreRepository.findAllByOrderByCognomeAscNomeAsc();
        return operatori.stream().map(OperatoreMapper::toDTO).toList();
    }

    @GetMapping("/{codice}")
    public OperatoreDTO dettaglio(@PathVariable String codice) {
        return operatoreRepository.findById(codice).map(OperatoreMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + codice));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperatoreDTO crea(@Valid @RequestBody OperatoreRequest request) {
        if (operatoreRepository.existsById(request.codice())) {
            throw new IllegalArgumentException("Esiste gia' un operatore con codice " + request.codice());
        }
        Operatore operatore = new Operatore(request.codice(), request.cognome(), request.nome(),
                request.telefonoAziendale(), request.telefonoCasa(), request.attivo());
        return OperatoreMapper.toDTO(operatoreRepository.save(operatore));
    }

    @PutMapping("/{codice}")
    public OperatoreDTO aggiorna(@PathVariable String codice, @Valid @RequestBody OperatoreRequest request) {
        Operatore operatore = operatoreRepository.findById(codice)
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + codice));
        operatore.setCognome(request.cognome());
        operatore.setNome(request.nome());
        operatore.setTelefonoAziendale(request.telefonoAziendale());
        operatore.setTelefonoCasa(request.telefonoCasa());
        operatore.setAttivo(request.attivo());
        return OperatoreMapper.toDTO(operatoreRepository.save(operatore));
    }

    @DeleteMapping("/{codice}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable String codice) {
        operatoreRepository.deleteById(codice);
    }
}
