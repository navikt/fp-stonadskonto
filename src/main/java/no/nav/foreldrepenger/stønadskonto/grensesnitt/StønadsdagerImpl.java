package no.nav.foreldrepenger.stønadskonto.grensesnitt;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.regler.PrematurukerUtil;

public class StønadsdagerImpl implements Stønadsdager {

    private final LocalDate regelverksdato;

    public StønadsdagerImpl(LocalDate regelverksdato) {
        this.regelverksdato = regelverksdato;
    }

    @Override
    public int ekstradagerPrematur(LocalDate fødselsdato, LocalDate termindato) {
        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
            return PrematurukerUtil.beregnAntallVirkedager(fødselsdato, termindato.minusDays(1));
        }
        return 0;
    }


    @Override
    public int ekstradagerFlerbarn(LocalDate familieHendelseDato, int antallBarn, Dekningsgrad dekningsgrad) {
        if (antallBarn < 2) {
            return 0;
        } else if (antallBarn == 2) {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN, dekningsgrad, getRegelverksdato(familieHendelseDato));
        } else {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, dekningsgrad, getRegelverksdato(familieHendelseDato));
        }
    }

    private LocalDate getRegelverksdato(LocalDate familieHendelseDato) {
        return Optional.ofNullable(regelverksdato).orElse(familieHendelseDato);
    }

    private LocalDate getRegelverksdato(LocalDate familieHendelseDato1, LocalDate familieHendelseDato2) {
        return Optional.ofNullable(regelverksdato).or(() -> Optional.ofNullable(familieHendelseDato1)).orElse(familieHendelseDato2);
    }

}
