package it.reperibilita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OperatoreRequest(
        @NotBlank @Size(max = 2) String codice,
        @NotBlank @Size(max = 60) String cognome,
        @NotBlank @Size(max = 40) String nome,
        @Size(max = 20) String telefonoAziendale,
        @Size(max = 20) String telefonoCasa,
        boolean attivo) {
}
