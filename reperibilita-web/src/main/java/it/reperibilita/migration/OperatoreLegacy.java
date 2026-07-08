package it.reperibilita.migration;

/** Riga grezza di TAB_OPERATORI. */
public record OperatoreLegacy(String codice, String cognome, String nome,
                               String telefonoAziendale, String telefonoCasa, boolean attivo) {
}
