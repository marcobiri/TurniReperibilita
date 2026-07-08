package it.reperibilita.mapper;

import it.reperibilita.domain.AbbinamentoOperatori;
import it.reperibilita.dto.AbbinamentoDTO;

public final class AbbinamentoMapper {

    private AbbinamentoMapper() {
    }

    public static AbbinamentoDTO toDTO(AbbinamentoOperatori abbinamento) {
        return new AbbinamentoDTO(abbinamento.getId(),
                abbinamento.getPrimoOperatore().getCodice(), abbinamento.getPrimoOperatore().getNomeCompleto(),
                abbinamento.getSecondoOperatore().getCodice(), abbinamento.getSecondoOperatore().getNomeCompleto());
    }
}
