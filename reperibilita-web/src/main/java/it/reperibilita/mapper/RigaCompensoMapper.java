package it.reperibilita.mapper;

import it.reperibilita.compenso.RigaCompenso;
import it.reperibilita.dto.RigaCompensoDTO;

public final class RigaCompensoMapper {

    private RigaCompensoMapper() {
    }

    public static RigaCompensoDTO toDTO(RigaCompenso riga) {
        return new RigaCompensoDTO(riga.data(), riga.tipoTurno().getNome(), riga.operatore().getCodice(),
                riga.operatore().getNomeCompleto(), riga.servizio().name(), riga.importo());
    }
}
