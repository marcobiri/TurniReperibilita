package it.reperibilita.mapper;

import it.reperibilita.domain.TipoTurno;
import it.reperibilita.dto.TipoTurnoDTO;

public final class TipoTurnoMapper {

    private TipoTurnoMapper() {
    }

    public static TipoTurnoDTO toDTO(TipoTurno tipoTurno) {
        return new TipoTurnoDTO(tipoTurno.getId(), tipoTurno.getNome(), tipoTurno.getOraInizio(),
                tipoTurno.getOraFine(), tipoTurno.getOreDurata(), tipoTurno.isFestivo(), tipoTurno.isNotturno());
    }
}
