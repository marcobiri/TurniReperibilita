package it.reperibilita.domain;

/**
 * Le due rotazioni gestite dal vecchio Turni.mdb: TAB_TURNI (reperibilita' generale)
 * e TAB_TURNI_FONIA (reperibilita' del servizio fonia).
 */
public enum Servizio {
    REPERIBILITA("Reperibilita generale"),
    FONIA("Reperibilita fonia");

    private final String descrizione;

    Servizio(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
