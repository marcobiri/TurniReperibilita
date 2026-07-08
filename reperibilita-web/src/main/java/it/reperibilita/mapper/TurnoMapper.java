package it.reperibilita.mapper;

import it.reperibilita.domain.Servizio;
import it.reperibilita.domain.Turno;
import it.reperibilita.dto.CalendarEventoDTO;
import it.reperibilita.dto.TurnoDTO;

import java.time.LocalDateTime;

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
        String titolo = turno.getTipoTurno().getNome() + " - " + turno.getOperatore().getNomeCompleto();

        return new CalendarEventoDTO(turno.getId(), titolo, inizio, fine, colore,
                turno.getServizio().name(), turno.getOperatore().getCodice(),
                turno.getTipoTurno().getId(), turno.getTipoTurno().isFestivo());
    }

    private static String colorePerServizio(Servizio servizio) {
        return switch (servizio) {
            case REPERIBILITA -> "#2563eb";
            case FONIA -> "#7c3aed";
        };
    }
}
