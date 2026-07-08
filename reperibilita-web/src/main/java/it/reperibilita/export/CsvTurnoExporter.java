package it.reperibilita.export;

import it.reperibilita.domain.Turno;

import java.time.format.DateTimeFormatter;

/** Esporta i turni in CSV (separatore ';', compatibile con Excel in locale italiana). */
public class CsvTurnoExporter extends TurnoExporter {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void scriviIntestazione(StringBuilder sb) {
        sb.append("Data;Tipo turno;Orario;Operatore;Servizio\n");
    }

    @Override
    protected void scriviRiga(StringBuilder sb, Turno turno) {
        sb.append(DATA.format(turno.getData())).append(';')
                .append(turno.getTipoTurno().getNome()).append(';')
                .append(turno.getTipoTurno().getOraInizio()).append('-').append(turno.getTipoTurno().getOraFine()).append(';')
                .append(turno.getOperatore().getNomeCompleto()).append(';')
                .append(turno.getServizio().getDescrizione())
                .append('\n');
    }

    @Override
    protected void scriviPiePagina(StringBuilder sb) {
        // nessuna chiusura necessaria per il CSV
    }

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String nomeFile() {
        return "turni.csv";
    }
}
