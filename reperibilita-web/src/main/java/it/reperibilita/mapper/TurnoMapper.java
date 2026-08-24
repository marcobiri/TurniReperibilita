package it.reperibilita.mapper;

import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.Turno;
import it.reperibilita.dto.CalendarEventoDTO;
import it.reperibilita.dto.TurnoDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TurnoMapper {

    private TurnoMapper() {
    }

    public static TurnoDTO toDTO(Turno turno) {
        return new TurnoDTO(turno.getId(), turno.getData(), TipoTurnoMapper.toDTO(turno.getTipoTurno()),
                OperatoreMapper.toDTO(turno.getOperatore()), turno.getServizio().name());
    }

    public static CalendarEventoDTO toEvento(Turno turno) {
        LocalDateTime inizio = turno.getData().atTime(turno.getTipoTurno().getOraInizio());
        LocalDateTime fine = turno.getData().atTime(turno.getTipoTurno().getOraFine());
        if (!fine.isAfter(inizio)) {
            fine = fine.plusDays(1);
        }
        String colore = colorePerServizio(turno.getServizio());
        String coloreTesto = coloreTestoPerServizio(turno.getServizio());
        String titolo = turno.getTipoTurno().getNome() + " - " + turno.getOperatore().getNomeCompleto();

        return new CalendarEventoDTO(turno.getId(), titolo, inizio, fine, colore, coloreTesto,
                turno.getServizio().name(), turno.getOperatore().getCodice(),
                turno.getTipoTurno().getId(), turno.getTipoTurno().isFestivo(), null);
    }

    /**
     * Come {@link #toEvento}, ma fonde in un'unica riga i turni diurno festivo e notturno festivo
     * dello stesso giorno quando condividono operatore e servizio (es. un sabato: "Diurno festivo"
     * 08-20 e "Notturno festivo" 20-08 dello stesso sabato, stesso operatore): sul calendario quel
     * giorno mostra una sola riga per servizio, come un giorno feriale qualunque, invece delle due
     * che ci sarebbero mostrando i due turni separatamente. Restano comunque due {@link Turno}
     * distinti in database (nessuna modifica al modello dati) - l'evento fuso porta l'id del turno
     * notturno in {@code idSecondario}, cosi' che modificare o eliminare l'unica riga agisca su
     * entrambi (vedi {@code /api/turni/giornata} in TurnoRestController). Se invece giorno e notte
     * dello stesso giorno festivo hanno operatori diversi, non si fondono: restano righe separate,
     * per non perdere l'informazione di chi e' effettivamente in turno.
     */
    public static List<CalendarEventoDTO> toEventi(List<Turno> turni) {
        Map<String, List<Turno>> gruppi = turni.stream()
                .collect(Collectors.groupingBy(TurnoMapper::chiaveGruppoGiornata, LinkedHashMap::new, Collectors.toList()));

        List<CalendarEventoDTO> eventi = new ArrayList<>();
        for (List<Turno> gruppo : gruppi.values()) {
            Turno diurno = trovaTurnoFestivo(gruppo, false);
            Turno notturno = trovaTurnoFestivo(gruppo, true);
            if (diurno != null && notturno != null) {
                eventi.add(toEventoGiornata(diurno, notturno));
            } else {
                for (Turno turno : gruppo) {
                    eventi.add(toEvento(turno));
                }
            }
        }
        return eventi;
    }

    private static String chiaveGruppoGiornata(Turno turno) {
        return turno.getData() + "|" + turno.getOperatore().getCodice() + "|" + turno.getServizio();
    }

    private static Turno trovaTurnoFestivo(List<Turno> gruppo, boolean notturno) {
        return gruppo.stream()
                .filter(t -> t.getTipoTurno().isFestivo() && t.getTipoTurno().isNotturno() == notturno)
                .findFirst().orElse(null);
    }

    private static CalendarEventoDTO toEventoGiornata(Turno diurno, Turno notturno) {
        LocalDateTime inizio = diurno.getData().atTime(diurno.getTipoTurno().getOraInizio());
        LocalDateTime fine = notturno.getData().plusDays(1).atTime(notturno.getTipoTurno().getOraFine());
        String colore = colorePerServizio(diurno.getServizio());
        String coloreTesto = coloreTestoPerServizio(diurno.getServizio());
        String titolo = "Turno festivo - " + diurno.getOperatore().getNomeCompleto();

        return new CalendarEventoDTO(diurno.getId(), titolo, inizio, fine, colore, coloreTesto,
                diurno.getServizio().name(), diurno.getOperatore().getCodice(),
                diurno.getTipoTurno().getId(), true, notturno.getId());
    }

    private static String colorePerServizio(Servizio servizio) {
        return switch (servizio) {
            case REPERIBILITA -> "#2563eb";
            case FONIA -> "#f5e08a"; // giallo pastello
        };
    }

    // Sfondi chiari (es. il giallo pastello di FONIA) richiedono un testo scuro per restare leggibili;
    // il blu di REPERIBILITA resta invece leggibile con il testo bianco di default di FullCalendar.
    private static String coloreTestoPerServizio(Servizio servizio) {
        return switch (servizio) {
            case REPERIBILITA -> "#ffffff";
            case FONIA -> "#7a5c00";
        };
    }
}
