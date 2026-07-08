package it.reperibilita.web.api;

import it.reperibilita.domain.AbbinamentoOperatori;
import it.reperibilita.dto.AbbinamentoDTO;
import it.reperibilita.dto.AbbinamentoRequest;
import it.reperibilita.mapper.AbbinamentoMapper;
import it.reperibilita.repository.AbbinamentoOperatoriRepository;
import it.reperibilita.repository.OperatoreRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/abbinamenti")
public class AbbinamentoRestController {

    private final AbbinamentoOperatoriRepository abbinamentoRepository;
    private final OperatoreRepository operatoreRepository;

    public AbbinamentoRestController(AbbinamentoOperatoriRepository abbinamentoRepository,
                                      OperatoreRepository operatoreRepository) {
        this.abbinamentoRepository = abbinamentoRepository;
        this.operatoreRepository = operatoreRepository;
    }

    @GetMapping
    public List<AbbinamentoDTO> elenco() {
        return abbinamentoRepository.findAll().stream().map(AbbinamentoMapper::toDTO).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbbinamentoDTO crea(@Valid @RequestBody AbbinamentoRequest request) {
        var primo = operatoreRepository.findById(request.primoOperatoreCodice())
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + request.primoOperatoreCodice()));
        var secondo = operatoreRepository.findById(request.secondoOperatoreCodice())
                .orElseThrow(() -> new EntityNotFoundException("Operatore non trovato: " + request.secondoOperatoreCodice()));
        return AbbinamentoMapper.toDTO(abbinamentoRepository.save(new AbbinamentoOperatori(primo, secondo)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        abbinamentoRepository.deleteById(id);
    }
}
