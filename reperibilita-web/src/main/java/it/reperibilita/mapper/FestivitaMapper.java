package it.reperibilita.mapper;

import it.reperibilita.domain.Festivita;
import it.reperibilita.dto.FestivitaDTO;

public final class FestivitaMapper {

    private FestivitaMapper() {
    }

    public static FestivitaDTO toDTO(Festivita festivita) {
        return new FestivitaDTO(festivita.getData(), festivita.getDescrizione(), festivita.getTipo().name());
    }
}
