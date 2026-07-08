package it.reperibilita.export;

import it.reperibilita.domain.Turno;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Esporta i turni in formato iCalendar (.ics), importabile in Google Calendar,
 * Outlook o qualunque altra app di calendario. Gestisce correttamente i turni
 * notturni che iniziano la sera e finiscono il mattino del giorno dopo.
 */
public class IcsTurnoExporter extends TurnoExporter {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    @Override
    protected void scriviIntestazione(StringBuilder sb) {
        sb.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//ReperibilitaWeb//IT\r\n");
    }

    @Override
    protected void scriviRiga(StringBuilder sb, Turno turno) {
        LocalDateTime inizio = turno.getData().atTime(turno.getTipoTurno().getOraInizio());
        LocalDateTime fine = turno.getData().atTime(turno.getTipoTurno().getOraFine());
        if (!fine.isAfter(inizio)) {
            fine = fine.plusDays(1);
        }

        sb.append("BEGIN:VEVENT\r\n")
                .append("UID:").append(turno.getId()).append("@reperibilita-web\r\n")
                .append("DTSTART:").append(TIMESTAMP.format(inizio)).append("\r\n")
                .append("DTEND:").append(TIMESTAMP.format(fine)).append("\r\n")
                .append("SUMMARY:").append(turno.getTipoTurno().getNome()).append(" - ")
                .append(turno.getOperatore().getNomeCompleto()).append("\r\n")
                .append("DESCRIPTION:Servizio ").append(turno.getServizio().getDescrizione()).append("\r\n")
                .append("END:VEVENT\r\n");
    }

    @Override
    protected void scriviPiePagina(StringBuilder sb) {
        sb.append("END:VCALENDAR\r\n");
    }

    @Override
    public String contentType() {
        return "text/calendar";
    }

    @Override
    public String nomeFile() {
        return "turni.ics";
    }
}
