package it.reperibilita.scheduling;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Un turno puo' essere assegnato solo a un operatore attualmente attivo. */
@Component
@Order(1)
public class OperatoreAttivoValidator extends AbstractTurnoValidator {

    @Override
    protected void controlla(RichiestaTurno richiesta) {
        if (!richiesta.operatore().isAttivo()) {
            throw new TurnoValidationException(
                    "L'operatore " + richiesta.operatore().getCodice() + " non e' attivo e non puo' ricevere nuovi turni");
        }
    }
}
