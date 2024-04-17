package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.time.LocalDate;
import java.time.Period;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

/*
 * TODO: Fjern etter vurdering av bruk ifm datamigrering
 */
public class Minsterett {

    public static Map<StønadskontoBeregningStønadskontotype, Integer> finnMinsterett(BeregnKontoerGrunnlag grunnlag) {

        var retter = new EnumMap<StønadskontoBeregningStønadskontotype, Integer>(StønadskontoBeregningStønadskontotype.class);

        retter.put(StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_MOR, finnToTetteMor(grunnlag));
        retter.put(StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_FAR, finnToTetteFar(grunnlag));

        retter.put(StønadskontoBeregningStønadskontotype.FAR_RUNDT_FØDSEL, finnFarRundtFødsel(grunnlag));

        retter.put(StønadskontoBeregningStønadskontotype.BARE_FAR_RETT, finnBareFarRettMinsterett(grunnlag));
        retter.put(StønadskontoBeregningStønadskontotype.UFØREDAGER, finnBareFarUtenAktivitetskrav(grunnlag));

        return retter;
    }

    private static int finnBareFarRettMinsterett(BeregnKontoerGrunnlag grunnlag) {
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var morHarUføretrygd = grunnlag.isMorHarUføretrygd();
        var antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, regeldato);
        if (antallDager == 0 || !grunnlag.isBareFarHarRett() || grunnlag.isAleneomsorg()) {
            return 0;
        }
        var flerbarnDager = 0;
        if (morHarUføretrygd) {
            antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, grunnlag.getDekningsgrad(), regeldato);
        }
        if (grunnlag.getAntallBarn() == 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN, grunnlag.getDekningsgrad(), regeldato);
        }
        if (grunnlag.getAntallBarn() > 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, grunnlag.getDekningsgrad(), regeldato);
        }
        if (flerbarnDager > 0) {
            var dagerFørTilleggAvFlerbarn = morHarUføretrygd ? antallDager : 0;
            antallDager = dagerFørTilleggAvFlerbarn + flerbarnDager;
        }
        return antallDager;
    }

    private static int finnBareFarUtenAktivitetskrav(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.isMorHarUføretrygd() && grunnlag.isBareFarHarRett() && !grunnlag.isAleneomsorg()) {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato());
        } else {
            return 0;
        }
    }

    private static int finnFarRundtFødsel(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.isGjelderFødsel()) {
            // Settes for begge parter. Brukes ifm berørt for begge og fakta uttak for far.
            return Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, null, grunnlag.getKonfigurasjonsvalgdato());
        } else {
            return 0;
        }
    }

    private static int finnToTetteMor(BeregnKontoerGrunnlag grunnlag) {
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette) {
            var antallDager = 0;
            if (grunnlag.isGjelderFødsel()) {
                antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, null, regeldato);
            } else {
                antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, null, regeldato);
            }
            return antallDager;
        } else {
            return 0;
        }
    }

    private static int finnToTetteFar(BeregnKontoerGrunnlag grunnlag) {
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette) {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, null, regeldato);
        } else {
            return 0;
        }
    }

    private static boolean toTette(LocalDate regeldato, LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null) {
            return false;
        }
        return Konfigurasjon.STANDARD.getIntervallgrenseParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, regeldato)
            .map(grenseToTette -> familieHendelseDato.plus(Period.ofWeeks(grenseToTette)).plusDays(1))
            .filter(grenseToTette -> grenseToTette.isAfter(familieHendelseDatoNesteSak))
            .isPresent();
    }
}
