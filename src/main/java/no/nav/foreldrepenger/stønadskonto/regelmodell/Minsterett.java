package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

public enum Minsterett {

    GENERELL_MINSTERETT,
    FAR_UTTAK_RUNDT_FØDSEL,
    TETTE_FØDSLER,
    UTEN_AKTIVITETSKRAV;


    public static Map<Minsterett, Integer> finnMinsterett(BeregnMinsterettGrunnlag grunnlag) {

        var fhDato = grunnlag.getFamilieHendelseDato();
        var retter = new HashMap<Minsterett, Integer>();
        var minsterett = grunnlag.isMinsterett();
        var morHarUføretrygd = grunnlag.isMorHarUføretrygd();
        var bareFarHarRett = grunnlag.isBareFarHarRett();
        var aleneomsorg = grunnlag.isAleneomsorg();

        sjekkToTette(grunnlag).
            ifPresent(tt -> retter.put(TETTE_FØDSLER, tt))
        ;
        if (minsterett) {
            // Settes for begge parter. Brukes ifm berørt for begge og fakta uttak for far.
            retter.put(Minsterett.FAR_UTTAK_RUNDT_FØDSEL, Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, null, fhDato));
        }

        if (minsterett && bareFarHarRett && !aleneomsorg) {
            sjekkBareFarRettMinsterett(grunnlag)
                .ifPresent(mr -> retter.put(Minsterett.GENERELL_MINSTERETT, mr));
        } else if (morHarUføretrygd && bareFarHarRett && !aleneomsorg) {
            var antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, grunnlag.getDekningsgrad(), fhDato);
            retter.put(Minsterett.UTEN_AKTIVITETSKRAV, antallDager);
        }
        return retter;
    }

    private static Optional<Integer> sjekkToTette(BeregnMinsterettGrunnlag grunnlag) {
        var fhDato = grunnlag.getFamilieHendelseDato();
        var toTette = toTette(grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (grunnlag.isMinsterett() && toTette) {
            var antallDager = 0;
            if (grunnlag.isMor() && grunnlag.isGjelderFødsel()) {
                antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, null, fhDato);
            } else if (grunnlag.isMor()) {
                antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, null, fhDato);
            } else {
                antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, null, fhDato);
            }
            return Optional.of(antallDager);
        } else if (grunnlag.getFamilieHendelseDatoNesteSak() != null){
            return Optional.of(0);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> sjekkBareFarRettMinsterett(BeregnMinsterettGrunnlag grunnlag) {
        var fhDato = grunnlag.getFamilieHendelseDato();
        var morHarUføretrygd = grunnlag.isMorHarUføretrygd();
        var antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, fhDato);
        var flerbarnDager = 0;
        if (morHarUføretrygd) {
            antallDager = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, grunnlag.getDekningsgrad(), fhDato);
        }
        if (grunnlag.getAntallBarn() == 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN, grunnlag.getDekningsgrad(), fhDato);
        }
        if (grunnlag.getAntallBarn() > 2) {
            flerbarnDager = Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, grunnlag.getDekningsgrad(), fhDato);
        }
        if (flerbarnDager > 0) {
            var dagerFørTilleggAvFlerbarn = morHarUføretrygd ? antallDager : 0;
            antallDager = dagerFørTilleggAvFlerbarn + flerbarnDager;
        }
        if (antallDager > 0) {
            return Optional.of(antallDager);
        } else {
            return Optional.empty();
        }
    }


    private static boolean toTette(LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null) {
            return false;
        }
        var toTetteGrense = Konfigurasjon.STANDARD.getParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, null, familieHendelseDato);
        var grenseToTette = familieHendelseDato.plus(Period.ofWeeks(toTetteGrense)).plusDays(1);
        return grenseToTette.isAfter(familieHendelseDatoNesteSak);
    }
}
