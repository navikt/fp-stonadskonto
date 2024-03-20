package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.time.LocalDate;
import java.time.Period;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

public enum Minsterett {

    BARE_FAR_GENERELL_MINSTERETT,
    FAR_UTTAK_RUNDT_FØDSEL,
    TETTE_FØDSLER_MOR,
    TETTE_FØDSLER_FAR,
    BARE_FAR_UTEN_AKTIVITETSKRAV;


    public static Map<Minsterett, Integer> finnMinsterett(BeregnMinsterettGrunnlag grunnlag) {

        var retter = new EnumMap<Minsterett, Integer>(Minsterett.class);

        retter.put(Minsterett.TETTE_FØDSLER_MOR, finnToTetteMor(grunnlag));
        retter.put(Minsterett.TETTE_FØDSLER_FAR, finnToTetteFar(grunnlag));

        retter.put(Minsterett.FAR_UTTAK_RUNDT_FØDSEL, finnFarRundtFødsel(grunnlag));

        retter.put(Minsterett.BARE_FAR_GENERELL_MINSTERETT, finnBareFarRettMinsterett(grunnlag));
        retter.put(Minsterett.BARE_FAR_UTEN_AKTIVITETSKRAV, finnBareFarUtenAktivitetskrav(grunnlag));

        return retter;
    }

    private static int finnBareFarRettMinsterett(BeregnMinsterettGrunnlag grunnlag) {
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

    private static int finnBareFarUtenAktivitetskrav(BeregnMinsterettGrunnlag grunnlag) {
        if (grunnlag.isMorHarUføretrygd() && grunnlag.isBareFarHarRett() && !grunnlag.isAleneomsorg()) {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato());
        } else {
            return 0;
        }
    }

    private static int finnFarRundtFødsel(BeregnMinsterettGrunnlag grunnlag) {
        if (grunnlag.isGjelderFødsel()) {
            // Settes for begge parter. Brukes ifm berørt for begge og fakta uttak for far.
            return Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, null, grunnlag.getKonfigurasjonsvalgdato());
        } else {
            return 0;
        }
    }

    private static int finnToTetteMor(BeregnMinsterettGrunnlag grunnlag) {
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

    private static int finnToTetteFar(BeregnMinsterettGrunnlag grunnlag) {
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette) {
            return Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, null, regeldato);
        } else {
            return 0;
        }
    }

    private static boolean toTette(LocalDate regeldato, LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        var toTetteGrense = Konfigurasjon.STANDARD.getParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, null, regeldato);
        if (toTetteGrense <= 0 || familieHendelseDatoNesteSak == null) {
            return false;
        }
        var grenseToTette = familieHendelseDato.plus(Period.ofWeeks(toTetteGrense)).plusDays(1);
        return grenseToTette.isAfter(familieHendelseDatoNesteSak);
    }
}
