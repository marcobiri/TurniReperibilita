package it.reperibilita.compenso;

/** Scheletro dei decoratori: avvolgono un {@link CalcoloCompenso} e ne arricchiscono il risultato. */
public abstract class CompensoDecorator implements CalcoloCompenso {

    protected final CalcoloCompenso delegato;

    protected CompensoDecorator(CalcoloCompenso delegato) {
        this.delegato = delegato;
    }
}
