package no.nav.foreldrepenger.stønadskonto.regelmodell;

public class KontoRegelFeil extends RuntimeException {

    public KontoRegelFeil(String message, Throwable cause) {
        super(message, cause);
    }
}
