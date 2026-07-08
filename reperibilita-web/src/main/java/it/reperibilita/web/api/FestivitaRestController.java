package it.reperibilita.web.api;

import it.reperibilita.dto.FestivitaDTO;
import it.reperibilita.dto.FestivitaRequest;
import it.reperibilita.holiday.HolidayService;
import it.reperibilita.mapper.FestivitaMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/festivita")
public class FestivitaRestController {

    private final HolidayService holidayService;

    public FestivitaRestController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    public List<FestivitaDTO> elenco(@RequestParam(required = false) Integer anno,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al) {
        List<it.reperibilita.domain.Festivita> festivita = (dal != null && al != null)
                ? holidayService.getFestivitaPeriodo(dal, al)
                : holidayService.getFestivitaAnno(anno != null ? anno : LocalDate.now().getYear());
        return festivita.stream().map(FestivitaMapper::toDTO).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FestivitaDTO aggiungi(@Valid @RequestBody FestivitaRequest request) {
        return FestivitaMapper.toDTO(holidayService.aggiungiPersonalizzata(request.data(), request.descrizione()));
    }

    @DeleteMapping("/{data}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        holidayService.rimuovi(data);
    }
}
