package it.reperibilita.migration;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter: nasconde dietro un'interfaccia di dominio l'API di Jackcess (righe
 * generiche indicizzate per nome colonna) usata per leggere il vecchio file
 * Access Turni.mdb. Solo le tabelle "vive" vengono lette; le tabelle di backup
 * storiche (Copia di..., *_OLD, foglio_excel*, TAB_TURNI_2003, ~TMPCLP*) sono
 * ignorate perche' non fanno parte dello schema corrente.
 */
public class LegacyMdbReader implements AutoCloseable {

    private final Database database;

    public LegacyMdbReader(File file) throws IOException {
        this.database = DatabaseBuilder.open(file);
    }

    public List<OperatoreLegacy> leggiOperatori() throws IOException {
        List<OperatoreLegacy> risultato = new ArrayList<>();
        Table tabella = database.getTable("TAB_OPERATORI");
        for (Row row : tabella) {
            risultato.add(new OperatoreLegacy(
                    row.getString("COD_OPERATORE"),
                    row.getString("cognome"),
                    row.getString("nome"),
                    row.getString("tel_az"),
                    row.getString("tel_casa"),
                    Boolean.TRUE.equals(row.getBoolean("attivo"))));
        }
        return risultato;
    }

    public List<TipoTurnoLegacy> leggiTipiTurno() throws IOException {
        List<TipoTurnoLegacy> risultato = new ArrayList<>();
        Table tabella = database.getTable("TAB_COD_TURNI");
        for (Row row : tabella) {
            risultato.add(new TipoTurnoLegacy(
                    toInt(row.get("ID_TURNO")),
                    row.getString("turno"),
                    row.getString("orario"),
                    toInt(row.get("ore")),
                    Boolean.TRUE.equals(row.getBoolean("festivo"))));
        }
        return risultato;
    }

    public List<TurnoLegacy> leggiTurni(String nomeTabella) throws IOException {
        List<TurnoLegacy> risultato = new ArrayList<>();
        Table tabella = database.getTable(nomeTabella);
        for (Row row : tabella) {
            LocalDate data = toLocalDate(row.get("data"));
            String codOperatore = row.getString("cod_operatore");
            if (data == null || codOperatore == null || codOperatore.isBlank()) {
                continue;
            }
            risultato.add(new TurnoLegacy(data, toInt(row.get("cod_turno")), codOperatore));
        }
        return risultato;
    }

    public List<AbbinamentoLegacy> leggiAbbinamenti() throws IOException {
        List<AbbinamentoLegacy> risultato = new ArrayList<>();
        Table tabella = database.getTable("TAB_abbinaOP");
        for (Row row : tabella) {
            risultato.add(new AbbinamentoLegacy(row.getString("COD_primoOP"), row.getString("COD_secondoOP")));
        }
        return risultato;
    }

    public Optional<BigDecimal> leggiTariffaOraria() throws IOException {
        Table tabella = database.getTable("tariffa");
        for (Row row : tabella) {
            Object importo = row.get("importo");
            if (importo instanceof Number numero) {
                return Optional.of(BigDecimal.valueOf(numero.doubleValue()));
            }
        }
        return Optional.empty();
    }

    /**
     * I campi Access di tipo "Numero intero" (2 byte) sono letti da Jackcess come
     * {@link Short}, mentre "Intero lungo" (4 byte) come {@link Integer}: entrambi
     * i tipi compaiono nel vecchio Turni.mdb, quindi si normalizza passando per
     * {@link Number} invece di assumere un tipo concreto specifico.
     */
    private static int toInt(Object valore) {
        return ((Number) valore).intValue();
    }

    private static LocalDate toLocalDate(Object valore) {
        if (valore instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (valore instanceof LocalDate localDate) {
            return localDate;
        }
        if (valore instanceof java.util.Date date) {
            return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        database.close();
    }
}
