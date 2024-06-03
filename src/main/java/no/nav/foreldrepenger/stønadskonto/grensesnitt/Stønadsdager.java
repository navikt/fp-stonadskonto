package no.nav.foreldrepenger.stønadskonto.grensesnitt;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

/*
 * Beholdes til evt senere behov
 */
public class Stønadsdager {

    private final LocalDate regelverksdato;

    private Stønadsdager(LocalDate regelverksdato) {
        this.regelverksdato = regelverksdato;
    }

    public static Stønadsdager instance(LocalDate regelverksdato) {
        return new Stønadsdager(regelverksdato);
    }

    public boolean endretFellesperiodeDekningsgrad80(Integer dager, LocalDate familieHendelseDato) {
        if (dager == null || dager == 0) {
            return false;
        }return Konfigurasjon.STANDARD.getParameter(Parametertype.FELLESPERIODE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, getRegelverksdato(familieHendelseDato)) != dager;
    }

    public boolean endretForeldrepengerDekningsgrad80(Integer dager, LocalDate familieHendelseDato) {
        if (dager == null || dager == 0) {
            return false;
        }
        return Konfigurasjon.STANDARD.getParameter(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER, Dekningsgrad.DEKNINGSGRAD_80, getRegelverksdato(familieHendelseDato)) != dager &&
            Konfigurasjon.STANDARD.getParameter(Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER, Dekningsgrad.DEKNINGSGRAD_80, getRegelverksdato(familieHendelseDato)) != dager;
    }

    private LocalDate getRegelverksdato(LocalDate familieHendelseDato) {
        return Optional.ofNullable(regelverksdato).orElse(familieHendelseDato);
    }

}
