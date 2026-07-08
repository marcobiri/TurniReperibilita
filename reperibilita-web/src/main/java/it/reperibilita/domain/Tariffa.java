package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Tariffa oraria usata per calcolare il compenso di reperibilita'.
 * Corrisponde a "tariffa" nel vecchio database (che conteneva un unico importo orario).
 * Qui viene arricchita con le percentuali di maggiorazione per notte e festivo,
 * usate dai decoratori del pacchetto "compenso".
 */
@Entity
@Table(name = "tariffa")
public class Tariffa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero
    private BigDecimal importoOrario;

    @NotNull
    @PositiveOrZero
    private BigDecimal percentualeMaggiorazioneFestiva = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    private BigDecimal percentualeMaggiorazioneNotturna = BigDecimal.ZERO;

    protected Tariffa() {
    }

    public Tariffa(BigDecimal importoOrario, BigDecimal percentualeMaggiorazioneFestiva,
                    BigDecimal percentualeMaggiorazioneNotturna) {
        this.importoOrario = importoOrario;
        this.percentualeMaggiorazioneFestiva = percentualeMaggiorazioneFestiva;
        this.percentualeMaggiorazioneNotturna = percentualeMaggiorazioneNotturna;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getImportoOrario() {
        return importoOrario;
    }

    public void setImportoOrario(BigDecimal importoOrario) {
        this.importoOrario = importoOrario;
    }

    public BigDecimal getPercentualeMaggiorazioneFestiva() {
        return percentualeMaggiorazioneFestiva;
    }

    public void setPercentualeMaggiorazioneFestiva(BigDecimal percentualeMaggiorazioneFestiva) {
        this.percentualeMaggiorazioneFestiva = percentualeMaggiorazioneFestiva;
    }

    public BigDecimal getPercentualeMaggiorazioneNotturna() {
        return percentualeMaggiorazioneNotturna;
    }

    public void setPercentualeMaggiorazioneNotturna(BigDecimal percentualeMaggiorazioneNotturna) {
        this.percentualeMaggiorazioneNotturna = percentualeMaggiorazioneNotturna;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tariffa other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
