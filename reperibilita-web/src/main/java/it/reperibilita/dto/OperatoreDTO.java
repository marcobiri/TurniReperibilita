package it.reperibilita.dto;

public record OperatoreDTO(String codice, String cognome, String nome, String nomeCompleto,
                            String telefonoAziendale, String telefonoCasa, boolean attivo) {
}
