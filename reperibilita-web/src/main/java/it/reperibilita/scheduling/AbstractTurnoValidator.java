package it.reperibilita.scheduling;

/** Scheletro comune a tutti i validatori: esegue il proprio controllo e poi inoltra la richiesta. */
public abstract class AbstractTurnoValidator implements TurnoValidator {

    private TurnoValidator successivo;

    @Override
    public void setSuccessivo(TurnoValidator successivo) {
        this.successivo = successivo;
    }

    @Override
    public final void valida(RichiestaTurno richiesta) {
        controlla(richiesta);
        if (successivo != null) {
            successivo.valida(richiesta);
        }
    }

    protected abstract void controlla(RichiestaTurno richiesta);
}
