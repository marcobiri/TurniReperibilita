package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Un operatore reperibile. Corrisponde a TAB_OPERATORI nel vecchio Turni.mdb.
 */
@Entity
@Table(name = "operatore")
public class Operatore {

    @Id
    @NotBlank
    @Size(max = 2)
    private String codice;

    @NotBlank
    @Size(max = 60)
    private String cognome;

    @NotBlank
    @Size(max = 40)
    private String nome;

    @Size(max = 20)
    private String telefonoAziendale;

    @Size(max = 20)
    private String telefonoCasa;

    private boolean attivo = true;

    protected Operatore() {
        // richiesto da JPA
    }

    public Operatore(String codice, String cognome, String nome, String telefonoAziendale,
                      String telefonoCasa, boolean attivo) {
        this.codice = codice;
        this.cognome = cognome;
        this.nome = nome;
        this.telefonoAziendale = telefonoAziendale;
        this.telefonoCasa = telefonoCasa;
        this.attivo = attivo;
    }

    public String getCodice() {
        return codice;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefonoAziendale() {
        return telefonoAziendale;
    }

    public void setTelefonoAziendale(String telefonoAziendale) {
        this.telefonoAziendale = telefonoAziendale;
    }

    public String getTelefonoCasa() {
        return telefonoCasa;
    }

    public void setTelefonoCasa(String telefonoCasa) {
        this.telefonoCasa = telefonoCasa;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operatore other)) return false;
        return Objects.equals(codice, other.codice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codice);
    }

    @Override
    public String toString() {
        return "Operatore{" + codice + " - " + getNomeCompleto() + "}";
    }
}
