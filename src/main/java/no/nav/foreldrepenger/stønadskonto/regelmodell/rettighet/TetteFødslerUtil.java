package no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

public final class TetteFødslerUtil {

    private TetteFødslerUtil() {
    }

    public static boolean toTette(LocalDate regeldato, LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null) {
            return false;
        }
        return Konfigurasjon.STANDARD.getIntervallgrenseParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, regeldato)
            .map(grenseToTette -> familieHendelseDato.plus(Period.ofWeeks(grenseToTette)).plusDays(1))
            .filter(grenseToTette -> grenseToTette.isAfter(familieHendelseDatoNesteSak))
            .isPresent();
    }

}
