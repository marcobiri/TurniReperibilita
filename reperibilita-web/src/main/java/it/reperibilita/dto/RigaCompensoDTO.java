package it.reperibilita.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RigaCompensoDTO(LocalDate data, String tipoTurno, String operatoreCodice, String operatoreNome,
                               String servizio, BigDecimal importo) {
}
