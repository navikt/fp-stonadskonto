package no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet;

import static java.lang.Math.toIntExact;
import static java.time.Month.JULY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class PrematurukerUtil {

    private static final LocalDate FØRSTE_DATO_PREMATUR = LocalDate.of(2019, JULY, 1);
    private static final Period PREMATUR_FØDSEL_DAGER_FØR_TERMIN = Period.ofDays(52);

    private static final int DAGER_PR_UKE = 7;
    private static final int VIRKEDAGER_PR_UKE = 5;
    private static final int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;


    private PrematurukerUtil() {
    }

    public static boolean oppfyllerKravTilPrematuruker(LocalDate fødselsdato, LocalDate termindato) {
        if (fødselsdato == null || termindato == null || fødselsdato.isBefore(FØRSTE_DATO_PREMATUR) || !fødselsdato.isBefore(termindato)) {
            return false;
        }
        return fødselsdato.plus(PREMATUR_FØDSEL_DAGER_FØR_TERMIN).isBefore(termindato);
    }

    public static int beregnPrematurdager(LocalDate fødselsdato, LocalDate termindato) {
        if (!oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
            return 0;
        }
        return PrematurukerUtil.beregnAntallVirkedager(fødselsdato, termindato.minusDays(1));
    }

    static int beregnAntallVirkedager(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        if (fom.isAfter(tom)) {
            throw new IllegalArgumentException("Utviklerfeil: fom " + fom + " kan ikke være før tom " + tom);
        }

        try {
            // Utvid til nærmeste mandag tilbake i tid fra og med begynnelse (fom) (0-6 dager)
            var padBefore = ChronoUnit.DAYS.between(fom.with(DayOfWeek.MONDAY), fom);
            // Utvid til nærmeste søndag fram i tid fra og med slutt (tom) (0-6 dager)
            var padAfter = ChronoUnit.DAYS.between(tom, tom.with(DayOfWeek.SUNDAY));
            // Antall virkedager i perioden utvidet til hele uker
            var virkedagerPadded = ChronoUnit.WEEKS.between(fom.with(DayOfWeek.MONDAY), tom.with(DayOfWeek.SUNDAY).plusDays(1)) * VIRKEDAGER_PR_UKE;
            // Antall virkedager i utvidelse
            var virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);
            // Virkedager i perioden uten virkedagene fra utvidelse
            return toIntExact(virkedagerPadded - virkedagerPadding);
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", e);
        }
    }
}
