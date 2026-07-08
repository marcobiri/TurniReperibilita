package it.reperibilita.holiday;

import java.time.LocalDate;
import java.time.Month;

/**
 * Calcola la data della domenica di Pasqua per un dato anno secondo il
 * calendario gregoriano, usando l'algoritmo di Meeus/Jones/Butcher.
 * E' il motivo per cui le festivita' italiane non possono essere una semplice
 * lista statica di giorni fissi: Pasqua (e di conseguenza Pasquetta) cade in
 * un giorno diverso ogni anno.
 */
public final class EasterCalculator {

    private EasterCalculator() {
    }

    public static LocalDate calcolaPasqua(int anno) {
        int a = anno % 19;
        int b = anno / 100;
        int c = anno % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mese = (h + l - 7 * m + 114) / 31;
        int giorno = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(anno, Month.of(mese), giorno);
    }
}
