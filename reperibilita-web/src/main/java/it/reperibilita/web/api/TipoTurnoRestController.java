package it.reperibilita.web.api;

import it.reperibilita.domain.TipoTurno;
import it.reperibilita.dto.TipoTurnoDTO;
import it.reperibilita.dto.TipoTurnoRequest;
import it.reperibilita.mapper.TipoTurnoMapper;
import it.reperibilita.repository.TipoTurnoRepository;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipi-turno")
public class TipoTurnoRestController {

    private final TipoTurnoRepository tipoTurnoRepository;

    public TipoTurnoRestController(TipoTurnoRepository tipoTurnoRepository) {
        this.tipoTurnoRepository = tipoTurnoRepository;
    }

    @GetMapping
    public List<TipoTurnoDTO> elenco() {
        return tipoTurnoRepository.findAll().stream().map(TipoTurnoMapper::toDTO).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoTurnoDTO crea(@Valid @RequestBody TipoTurnoRequest request) {
        TipoTurno tipoTurno = new TipoTurno(request.nome(), request.oraInizio(), request.oraFine(),
                request.oreDurata(), request.festivo());
        return TipoTurnoMapper.toDTO(tipoTurnoRepository.save(tipoTurno));
    }

    @PutMapping("/{id}")
    public TipoTurnoDTO aggiorna(@PathVariable Long id, @Valid @RequestBody TipoTurnoRequest request) {
        TipoTurno tipoTurno = tipoTurnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo turno non trovato: " + id));
        tipoTurno.setNome(request.nome());
        tipoTurno.setOraInizio(request.oraInizio());
        tipoTurno.setOraFine(request.oraFine());
        tipoTurno.setOreDurata(request.oreDurata());
        tipoTurno.setFestivo(request.festivo());
        return TipoTurnoMapper.toDTO(tipoTurnoRepository.save(tipoTurno));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        tipoTurnoRepository.deleteById(id);
    }
}
