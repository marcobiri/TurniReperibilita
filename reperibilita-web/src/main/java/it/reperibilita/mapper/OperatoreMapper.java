package it.reperibilita.mapper;

import it.reperibilita.domain.Operatore;
import it.reperibilita.dto.OperatoreDTO;

public final class OperatoreMapper {

    private OperatoreMapper() {
    }

    public static OperatoreDTO toDTO(Operatore operatore) {
        return new OperatoreDTO(operatore.getCodice(), operatore.getCognome(), operatore.getNome(),
                operatore.getNomeCompleto(), operatore.getTelefonoAziendale(), operatore.getTelefonoCasa(),
                operatore.isAttivo());
    }
}
