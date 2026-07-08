package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Assegnazione di un operatore a un turno in una data specifica.
 * Unifica le vecchie tabelle TAB_TURNI e TAB_TURNI_FONIA tramite il campo {@link Servizio}.
 */
@Entity
@Table(name = "turno", uniqueConstraints = {
        @UniqueConstraint(name = "uk_turno_slot", columnNames = {"data", "tipo_turno_id", "servizio"})
})
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate data;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_turno_id", nullable = false)
    private TipoTurno tipoTurno;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operatore_codice", nullable = false)
    private Operatore operatore;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Servizio servizio;

    protected Turno() {
    }

    public Turno(LocalDate data, TipoTurno tipoTurno, Operatore operatore, Servizio servizio) {
        this.data = data;
        this.tipoTurno = tipoTurno;
        this.operatore = operatore;
        this.servizio = servizio;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public TipoTurno getTipoTurno() {
        return tipoTurno;
    }

    public void setTipoTurno(TipoTurno tipoTurno) {
        this.tipoTurno = tipoTurno;
    }

    public Operatore getOperatore() {
        return operatore;
    }

    public void setOperatore(Operatore operatore) {
        this.operatore = operatore;
    }

    public Servizio getServizio() {
        return servizio;
    }

    public void setServizio(Servizio servizio) {
        this.servizio = servizio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turno other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Turno{" + data + " " + tipoTurno + " -> " + operatore + " [" + servizio + "]}";
    }
}
