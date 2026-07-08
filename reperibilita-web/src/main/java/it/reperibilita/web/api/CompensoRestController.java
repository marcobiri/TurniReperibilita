package it.reperibilita.web.api;

import it.reperibilita.compenso.CompensoService;
import it.reperibilita.dto.RigaCompensoDTO;
import it.reperibilita.mapper.RigaCompensoMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compensi")
public class CompensoRestController {

    private final CompensoService compensoService;

    public CompensoRestController(CompensoService compensoService) {
        this.compensoService = compensoService;
    }

    @GetMapping
    public List<RigaCompensoDTO> righe(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al,
                                        @RequestParam(required = false) String operatore) {
        return compensoService.calcolaRighe(dal, al, operatore).stream().map(RigaCompensoMapper::toDTO).toList();
    }

    @GetMapping("/totali")
    public Map<String, BigDecimal> totali(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al) {
        return compensoService.totalePerOperatore(dal, al);
    }
}
