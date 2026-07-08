package it.reperibilita.export;

import it.reperibilita.domain.Turno;

import java.util.List;

/**
 * Template Method: definisce lo scheletro comune per esportare un elenco di
 * turni in un formato di testo (intestazione, una riga per turno, chiusura),
 * lasciando alle sottoclassi solo la sintassi specifica del formato
 * (vedi {@link CsvTurnoExporter}, {@link IcsTurnoExporter}).
 */
public abstract class TurnoExporter {

    public final String esporta(List<Turno> turni) {
        StringBuilder sb = new StringBuilder();
        scriviIntestazione(sb);
        for (Turno turno : turni) {
            scriviRiga(sb, turno);
        }
        scriviPiePagina(sb);
        return sb.toString();
    }

    protected abstract void scriviIntestazione(StringBuilder sb);

    protected abstract void scriviRiga(StringBuilder sb, Turno turno);

    protected abstract void scriviPiePagina(StringBuilder sb);

    public abstract String contentType();

    public abstract String nomeFile();
}
