package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import static java.lang.Math.toIntExact;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

public final class PrematurukerUtil {

    private static final int DAGER_PR_UKE = 7;
    private static final int VIRKEDAGER_PR_UKE = 5;
    private static final int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;


    private PrematurukerUtil() {
    }

    public static boolean oppfyllerKravTilPrematuruker(LocalDate fødselsdato, LocalDate termindato) {
        if (fødselsdato == null || termindato == null) {
            return false;
        }
        if (erEtterRegelendringStartdato(fødselsdato)) {
            var antallDagerFørTermin = Konfigurasjon.STANDARD.getParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, null, fødselsdato);
            return fødselsdato.plusDays(antallDagerFørTermin).isBefore(termindato);
        }
        return false;
    }

    public static int beregnAntallVirkedager(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        if (fom.isAfter(tom)) {
            throw new IllegalArgumentException("Utviklerfeil: fom " + fom + " kan ikke være før tom " + tom);
        }

        try {
            // Utvid til nærmeste mandag tilbake i tid fra og med begynnelse (fom) (0-6 dager)
            var padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            // Utvid til nærmeste søndag fram i tid fra og med slutt (tom) (0-6 dager)
            var padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            // Antall virkedager i perioden utvidet til hele uker
            var virkedagerPadded = toIntExact(
                ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1)) * VIRKEDAGER_PR_UKE);
            // Antall virkedager i utvidelse
            var virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);
            // Virkedager i perioden uten virkedagene fra utvidelse
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", e);
        }
    }

    private static boolean erEtterRegelendringStartdato(LocalDate fødselsdato) {
        return !fødselsdato.isBefore(Konfigurasjon.PREMATURUKER_REGELENDRING_START_DATO);
    }
}
