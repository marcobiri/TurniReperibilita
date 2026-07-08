package it.reperibilita.dto;

import java.time.LocalTime;

public record TipoTurnoDTO(Long id, String nome, LocalTime oraInizio, LocalTime oraFine,
                            int oreDurata, boolean festivo, boolean notturno) {
}
