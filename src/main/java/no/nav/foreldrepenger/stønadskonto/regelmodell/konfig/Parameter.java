package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public record Parameter(Dekningsgrad dekningsgrad, LocalDate fom, LocalDate tom, Integer verdi) {

    public Parameter(Dekningsgrad dekningsgrad, LocalDate fom, LocalDate tom, Integer verdi) {
        if (fom != null && tom != null && tom.isBefore(fom)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato: " + fom + ">" + tom);
        }
        this.dekningsgrad = dekningsgrad;
        this.fom = fom == null ? LocalDate.MIN : fom;
        this.tom = tom == null ? LocalDate.MAX : tom;
        this.verdi = verdi;
    }

    boolean overlapper(Parameter annen) {
        return Objects.equals(dekningsgrad(), annen.dekningsgrad()) && (overlapper(annen.fom()) || overlapper(annen.tom()) || erOmsluttetAv(annen));
    }

    boolean overlapper(Dekningsgrad annenDekningsgrad, LocalDate dato) {
        return Objects.equals(dekningsgrad(), annenDekningsgrad) && overlapper(dato);
    }

    boolean overlapper(LocalDate dato) {
        return !(dato.isBefore(fom()) || dato.isAfter(tom()));
    }

    //Også true hvis perioden er lik
    private boolean erOmsluttetAv(Parameter periode) {
        return !periode.fom().isAfter(fom()) && !periode.tom().isBefore(tom());
    }


}
