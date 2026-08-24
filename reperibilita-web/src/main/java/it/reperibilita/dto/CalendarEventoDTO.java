package it.reperibilita.dto;

import java.time.LocalDateTime;

/**
 * Forma "appiattita" di un turno pensata per essere consumata direttamente da
 * FullCalendar.js: i campi non standard (servizio, codiceOperatore, tipoTurnoId,
 * festivo, idSecondario) finiscono automaticamente in event.extendedProps lato client,
 * mentre color/textColor sono proprieta' standard di FullCalendar (sfondo e testo dell'evento).
 *
 * @param idSecondario id del turno della domenica quando questo evento rappresenta un weekend
 *                      sabato+domenica unificato (vedi {@link it.reperibilita.mapper.TurnoMapper#toEventi}); null altrimenti.
 */
public record CalendarEventoDTO(Long id, String title, LocalDateTime start, LocalDateTime end,
                                 String color, String textColor, String servizio, String codiceOperatore,
                                 Long tipoTurnoId, boolean festivo, Long idSecondario) {
}
