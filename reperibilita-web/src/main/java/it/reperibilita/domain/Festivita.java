package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Un giorno festivo. Corrisponde a TAB_FESTIVI, ma qui la data e' generata
 * automaticamente ogni anno dal modulo "holiday" (vedi HolidayService) invece
 * di dover essere inserita manualmente come nel vecchio database.
 */
@Entity
@Table(name = "festivita")
public class Festivita {

    @Id
    @NotNull
    private LocalDate data;

    @NotBlank
    private String descrizione;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoFestivita tipo;

    protected Festivita() {
    }

    public Festivita(LocalDate data, String descrizione, TipoFestivita tipo) {
        this.data = data;
        this.descrizione = descrizione;
        this.tipo = tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public TipoFestivita getTipo() {
        return tipo;
    }

    public void setTipo(TipoFestivita tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Festivita other)) return false;
        return Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return data + " - " + descrizione;
    }
}
