package it.reperibilita.web.api;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.dto.TariffaDTO;
import it.reperibilita.dto.TariffaRequest;
import it.reperibilita.mapper.TariffaMapper;
import it.reperibilita.repository.TariffaRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/tariffa")
public class TariffaRestController {

    private final TariffaRepository tariffaRepository;

    public TariffaRestController(TariffaRepository tariffaRepository) {
        this.tariffaRepository = tariffaRepository;
    }

    @GetMapping
    public TariffaDTO leggi() {
        return TariffaMapper.toDTO(tariffaCorrente());
    }

    @PutMapping
    public TariffaDTO aggiorna(@Valid @RequestBody TariffaRequest request) {
        Tariffa tariffa = tariffaCorrente();
        tariffa.setImportoOrario(request.importoOrario());
        tariffa.setPercentualeMaggiorazioneFestiva(request.percentualeMaggiorazioneFestiva());
        tariffa.setPercentualeMaggiorazioneNotturna(request.percentualeMaggiorazioneNotturna());
        return TariffaMapper.toDTO(tariffaRepository.save(tariffa));
    }

    private Tariffa tariffaCorrente() {
        return tariffaRepository.findAll().stream().findFirst()
                .orElseGet(() -> tariffaRepository.save(
                        new Tariffa(BigDecimal.valueOf(1.8), BigDecimal.valueOf(20), BigDecimal.valueOf(10))));
    }
}
