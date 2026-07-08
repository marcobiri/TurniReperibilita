package it.reperibilita.compenso;

import it.reperibilita.domain.Tariffa;

/**
 * Compone la catena di decoratori (base + maggiorazione notturna + maggiorazione
 * festiva) a partire dalla tariffa corrente. E' l'unico punto in cui l'ordine di
 * composizione dei decoratori e' deciso.
 */
public class CalcoloCompensoFactory {

    private CalcoloCompensoFactory() {
    }

    public static CalcoloCompenso creaPerTariffa(Tariffa tariffa) {
        CalcoloCompenso calcolo = new CompensoBase(tariffa);
        calcolo = new MaggiorazioneNotturnaDecorator(calcolo, tariffa);
        calcolo = new MaggiorazioneFestivaDecorator(calcolo, tariffa);
        return calcolo;
    }
}
