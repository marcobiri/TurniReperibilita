package it.reperibilita.migration;

import java.time.LocalDate;

/** Riga grezza di TAB_TURNI / TAB_TURNI_FONIA. */
public record TurnoLegacy(LocalDate data, int codTurno, String codOperatore) {
}
