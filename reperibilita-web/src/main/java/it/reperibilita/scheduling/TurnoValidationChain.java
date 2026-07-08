package it.reperibilita.scheduling;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Collega in sequenza tutti i {@link TurnoValidator} disponibili nel contesto
 * Spring (l'ordine e' determinato dall'annotazione @Order su ciascuno) formando
 * la catena di responsabilita' vera e propria. Aggiungere un nuovo controllo in
 * futuro significa solo creare un nuovo @Component che estende
 * {@link AbstractTurnoValidator}: questa classe non deve essere modificata.
 */
@Component
public class TurnoValidationChain {

    private final TurnoValidator testa;

    public TurnoValidationChain(List<TurnoValidator> validatori) {
        for (int i = 0; i < validatori.size() - 1; i++) {
            validatori.get(i).setSuccessivo(validatori.get(i + 1));
        }
        this.testa = validatori.isEmpty() ? null : validatori.get(0);
    }

    public void valida(RichiestaTurno richiesta) {
        if (testa != null) {
            testa.valida(richiesta);
        }
    }
}
