package no.nav.foreldrepenger.stønadskonto.grensesnitt;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.PrematurukerUtil;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.TetteSakerUtil;

public class StønadsdagerImpl implements Stønadsdager {

    private final LocalDate regelverksdato;

    public StønadsdagerImpl(LocalDate regelverksdato) {
        this.regelverksdato = regelverksdato;
    }

    @Override
    public int ekstradagerPrematur(LocalDate fødselsdato, LocalDate termindato) {
        if (PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
            return PrematurukerUtil.beregnPrematurdager(fødselsdato, termindato);
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

    @Override
    public int andredagerFarRundtFødsel(LocalDate hendelsedato, boolean gjelderFødsel) {
        return gjelderFødsel ? Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, null, getRegelverksdato(hendelsedato)) : 0;
    }

    @Override
    public int minsterettTetteFødsler(Brukerrolle brukerrolle, boolean gjelderFødsel, LocalDate familiehendelse, LocalDate familiehendelseNesteSak) {
        if (!TetteSakerUtil.toTette(getRegelverksdato(familiehendelse), familiehendelse, familiehendelseNesteSak)) {
            return 0;
        }
        if (Brukerrolle.MOR.equals(brukerrolle)) {
            return gjelderFødsel ?
                Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, null, getRegelverksdato(familiehendelse)) :
                Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, null, getRegelverksdato(familiehendelse));
        } else {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_TETTE_SAKER_DAGER, null, getRegelverksdato(familiehendelse));
        }
    }

    @Override
    public int minsterettBareFarRett(LocalDate familiehendelse, int antallBarn, boolean bareFarRett, boolean morAvklartUfør, Dekningsgrad dekningsgrad) {
        var regeldato = getRegelverksdato(familiehendelse);
        var antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, regeldato);
        if (antallDager == 0 || !bareFarRett) {
            return 0;
        }
        var flerbarnDager = 0;
        if (morAvklartUfør) {
            antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, dekningsgrad, regeldato);
        }
        if (antallBarn == 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN, dekningsgrad, regeldato);
        }
        if (antallBarn > 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, dekningsgrad, regeldato);
        }
        if (flerbarnDager > 0) {
            var dagerFørTilleggAvFlerbarn = morAvklartUfør ? antallDager : 0;
            antallDager = dagerFørTilleggAvFlerbarn + flerbarnDager;
        }
        return antallDager;
    }

    @Override
    public int aktivitetskravUføredager(LocalDate familiehendelse, boolean bareFarRett, boolean morAvklartUfør, Dekningsgrad dekningsgrad) {
        return !bareFarRett || !morAvklartUfør ? 0 :
            Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, dekningsgrad, getRegelverksdato(familiehendelse));
    }

    private LocalDate getRegelverksdato(LocalDate familieHendelseDato) {
        return Optional.ofNullable(regelverksdato).orElse(familieHendelseDato);
    }

    private LocalDate getRegelverksdato(LocalDate familieHendelseDato1, LocalDate familieHendelseDato2) {
        return Optional.ofNullable(regelverksdato).or(() -> Optional.ofNullable(familieHendelseDato1)).orElse(familieHendelseDato2);
    }

}
