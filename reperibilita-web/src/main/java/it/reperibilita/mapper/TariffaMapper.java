package it.reperibilita.mapper;

import it.reperibilita.domain.Tariffa;
import it.reperibilita.dto.TariffaDTO;

public final class TariffaMapper {

    private TariffaMapper() {
    }

    public static TariffaDTO toDTO(Tariffa tariffa) {
        return new TariffaDTO(tariffa.getId(), tariffa.getImportoOrario(),
                tariffa.getPercentualeMaggiorazioneFestiva(), tariffa.getPercentualeMaggiorazioneNotturna());
    }
}
