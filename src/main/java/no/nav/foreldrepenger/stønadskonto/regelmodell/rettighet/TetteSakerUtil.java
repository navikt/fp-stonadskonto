package no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet;

import static java.time.Month.AUGUST;

import java.time.LocalDate;
import java.time.Period;

public final class TetteSakerUtil {

    private static final LocalDate FØRSTE_DATO_TETTE_SAKER = LocalDate.of(2022, AUGUST, 2);
    private static final Period TETTE_SAKER_MELLOMROM_UKER = Period.ofWeeks(48);

    private TetteSakerUtil() {
    }

    public static boolean toTette(LocalDate regeldato, LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null || regeldato.isBefore(FØRSTE_DATO_TETTE_SAKER)) {
            return false;
        }
        return familieHendelseDato.plus(TETTE_SAKER_MELLOMROM_UKER).plusDays(1).isAfter(familieHendelseDatoNesteSak);
    }

}
