package it.reperibilita.dto;

import java.time.LocalDateTime;

/**
 * Forma "appiattita" di un turno pensata per essere consumata direttamente da
 * FullCalendar.js: i campi non standard (servizio, codiceOperatore, tipoTurnoId,
 * festivo) finiscono automaticamente in event.extendedProps lato client.
 */
public record CalendarEventoDTO(Long id, String title, LocalDateTime start, LocalDateTime end,
                                 String color, String servizio, String codiceOperatore,
                                 Long tipoTurnoId, boolean festivo) {
}
