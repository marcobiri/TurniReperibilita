package it.reperibilita.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Coppia di operatori abbinati (es. chi copre il subentro in caso di indisponibilita').
 * Corrisponde a TAB_abbinaOP.
 */
@Entity
@Table(name = "abbinamento_operatori")
public class AbbinamentoOperatori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primo_operatore_codice", nullable = false)
    private Operatore primoOperatore;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secondo_operatore_codice", nullable = false)
    private Operatore secondoOperatore;

    protected AbbinamentoOperatori() {
    }

    public AbbinamentoOperatori(Operatore primoOperatore, Operatore secondoOperatore) {
        this.primoOperatore = primoOperatore;
        this.secondoOperatore = secondoOperatore;
    }

    public Long getId() {
        return id;
    }

    public Operatore getPrimoOperatore() {
        return primoOperatore;
    }

    public void setPrimoOperatore(Operatore primoOperatore) {
        this.primoOperatore = primoOperatore;
    }

    public Operatore getSecondoOperatore() {
        return secondoOperatore;
    }

    public void setSecondoOperatore(Operatore secondoOperatore) {
        this.secondoOperatore = secondoOperatore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbbinamentoOperatori other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
