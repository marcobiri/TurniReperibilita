package it.reperibilita.scheduling;

import it.reperibilita.domain.Servizio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Criteri di ricerca dei turni. Costruita tramite Builder perche' i filtri
 * opzionali (servizio, operatore) renderebbero scomodo un costruttore con
 * molti parametri nullable.
 */
public final class TurnoSearchCriteria {

    private final LocalDate dal;
    private final LocalDate al;
    private final Servizio servizio;
    private final String codiceOperatore;

    private TurnoSearchCriteria(Builder builder) {
        this.dal = builder.dal;
        this.al = builder.al;
        this.servizio = builder.servizio;
        this.codiceOperatore = builder.codiceOperatore;
    }

    public LocalDate getDal() {
        return dal;
    }

    public LocalDate getAl() {
        return al;
    }

    public Servizio getServizio() {
        return servizio;
    }

    public String getCodiceOperatore() {
        return codiceOperatore;
    }

    public static Builder builder(LocalDate dal, LocalDate al) {
        return new Builder(dal, al);
    }

    public static final class Builder {
        private final LocalDate dal;
        private final LocalDate al;
        private Servizio servizio;
        private String codiceOperatore;

        private Builder(LocalDate dal, LocalDate al) {
            this.dal = Objects.requireNonNull(dal, "dal");
            this.al = Objects.requireNonNull(al, "al");
        }

        public Builder servizio(Servizio servizio) {
            this.servizio = servizio;
            return this;
        }

        public Builder codiceOperatore(String codiceOperatore) {
            this.codiceOperatore = codiceOperatore;
            return this;
        }

        public TurnoSearchCriteria build() {
            return new TurnoSearchCriteria(this);
        }
    }
}
