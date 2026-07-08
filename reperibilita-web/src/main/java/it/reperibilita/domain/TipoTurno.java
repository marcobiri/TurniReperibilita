package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Tipologia di turno (es. notturno feriale, diurno festivo, notturno festivo).
 * Corrisponde a TAB_COD_TURNI. A differenza del vecchio database, l'orario e'
 * modellato con LocalTime invece di testo libero, cosi' da poter essere usato
 * per calcoli (es. maggiorazione notturna).
 */
@Entity
@Table(name = "tipo_turno")
public class TipoTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    @Positive
    private int oreDurata;

    /** Indica se questo turno copre la fascia festiva (usato come default, la festivita' effettiva del giorno viene comunque verificata su TAB_FESTIVI). */
    private boolean festivo;

    protected TipoTurno() {
    }

    public TipoTurno(String nome, LocalTime oraInizio, LocalTime oraFine, int oreDurata, boolean festivo) {
        this.nome = nome;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.oreDurata = oreDurata;
        this.festivo = festivo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalTime getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(LocalTime oraInizio) {
        this.oraInizio = oraInizio;
    }

    public LocalTime getOraFine() {
        return oraFine;
    }

    public void setOraFine(LocalTime oraFine) {
        this.oraFine = oraFine;
    }

    public int getOreDurata() {
        return oreDurata;
    }

    public void setOreDurata(int oreDurata) {
        this.oreDurata = oreDurata;
    }

    public boolean isFestivo() {
        return festivo;
    }

    public void setFestivo(boolean festivo) {
        this.festivo = festivo;
    }

    /** Un turno che inizia la sera e finisce il mattino dopo (es. 20:00 - 08:00). */
    public boolean isNotturno() {
        return oraFine.isBefore(oraInizio) || oraInizio.getHour() >= 20 || oraInizio.getHour() < 6;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoTurno other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + oraInizio + "-" + oraFine + ")";
    }
}
