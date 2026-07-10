package it.reperibilita.web.api;

import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.Turno;
import it.reperibilita.dto.CalendarEventoDTO;
import it.reperibilita.dto.RisultatoGenerazioneDTO;
import it.reperibilita.dto.TurnoDTO;
import it.reperibilita.dto.TurnoPropostoDTO;
import it.reperibilita.dto.TurnoRequest;
import it.reperibilita.export.CsvTurnoExporter;
import it.reperibilita.export.IcsTurnoExporter;
import it.reperibilita.export.TurnoExporter;
import it.reperibilita.mapper.TurnoMapper;
import it.reperibilita.scheduling.GeneratoreTurniService;
import it.reperibilita.scheduling.TurnoSearchCriteria;
import it.reperibilita.scheduling.TurnoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turni")
public class TurnoRestController {

    private final TurnoService turnoService;
    private final GeneratoreTurniService generatoreTurniService;

    public TurnoRestController(TurnoService turnoService, GeneratoreTurniService generatoreTurniService) {
        this.turnoService = turnoService;
        this.generatoreTurniService = generatoreTurniService;
    }

    @GetMapping
    public List<TurnoDTO> elenco(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al,
                                  @RequestParam(required = false) Servizio servizio,
                                  @RequestParam(required = false) String operatore) {
        return cercaTurni(dal, al, servizio, operatore).stream().map(TurnoMapper::toDTO).toList();
    }

    @GetMapping("/eventi")
    public List<CalendarEventoDTO> eventi(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al,
                                           @RequestParam(required = false) Servizio servizio,
                                           @RequestParam(required = false) String operatore) {
        return cercaTurni(dal, al, servizio, operatore).stream().map(TurnoMapper::toEvento).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoDTO assegna(@Valid @RequestBody TurnoRequest request) {
        Servizio servizio = Servizio.valueOf(request.servizio());
        Turno turno = turnoService.assegna(request.data(), request.tipoTurnoId(), request.codiceOperatore(), servizio);
        return TurnoMapper.toDTO(turno);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@PathVariable Long id) {
        turnoService.rimuovi(id);
    }

    @GetMapping("/genera-anno/anteprima")
    public List<TurnoPropostoDTO> anteprimaGenerazioneAnno(@RequestParam int anno,
                                                            @RequestParam(required = false) Servizio servizio) {
        return generatoreTurniService.calcolaProposte(anno, servizio);
    }

    @PostMapping("/genera-anno")
    public RisultatoGenerazioneDTO generaAnno(@RequestParam int anno,
                                               @RequestParam(required = false) Servizio servizio) {
        return generatoreTurniService.generaEPersisti(anno, servizio);
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> esportaCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al,
                                              @RequestParam(required = false) Servizio servizio) {
        return esporta(new CsvTurnoExporter(), dal, al, servizio);
    }

    @GetMapping("/export.ics")
    public ResponseEntity<byte[]> esportaIcs(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al,
                                              @RequestParam(required = false) Servizio servizio) {
        return esporta(new IcsTurnoExporter(), dal, al, servizio);
    }

    private ResponseEntity<byte[]> esporta(TurnoExporter exporter, LocalDate dal, LocalDate al, Servizio servizio) {
        List<Turno> turni = cercaTurni(dal, al, servizio, null);
        byte[] contenuto = exporter.esporta(turni).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exporter.contentType() + ";charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exporter.nomeFile() + "\"")
                .body(contenuto);
    }

    private List<Turno> cercaTurni(LocalDate dal, LocalDate al, Servizio servizio, String operatore) {
        TurnoSearchCriteria.Builder builder = TurnoSearchCriteria.builder(dal, al);
        if (servizio != null) {
            builder.servizio(servizio);
        }
        if (operatore != null && !operatore.isBlank()) {
            builder.codiceOperatore(operatore);
        }
        return turnoService.cerca(builder.build());
    }
}
