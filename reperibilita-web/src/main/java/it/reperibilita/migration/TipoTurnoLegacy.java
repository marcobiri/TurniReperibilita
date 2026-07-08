package it.reperibilita.migration;

/** Riga grezza di TAB_COD_TURNI. L'orario e' ancora testo libero (es. "20.00 - 08.00") come nel vecchio Access. */
public record TipoTurnoLegacy(int idTurno, String nome, String orario, int ore, boolean festivo) {
}
