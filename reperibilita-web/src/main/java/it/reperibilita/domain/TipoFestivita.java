package it.reperibilita.domain;

/**
 * Origine di una festivita':
 * - FISSA: stesso giorno/mese ogni anno (es. Capodanno, Natale).
 * - MOBILE: calcolata a partire dalla Pasqua, cambia data ogni anno (es. Pasquetta).
 * - PERSONALIZZATA: inserita manualmente dall'utente (es. patrono locale, chiusura aziendale).
 */
public enum TipoFestivita {
    FISSA,
    MOBILE,
    PERSONALIZZATA
}
