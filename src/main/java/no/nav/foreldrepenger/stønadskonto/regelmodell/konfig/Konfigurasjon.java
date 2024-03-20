package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import static java.time.Month.AUGUST;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad.DEKNINGSGRAD_100;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad.DEKNINGSGRAD_80;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.EKSTRA_DAGER_TO_BARN;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FAR_DAGER_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FEDREKVOTE_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FELLESPERIODE_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FORELDREPENGER_FAR_ALENEOMSORG_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.FORELDREPENGER_MOR_ALENEOMSORG_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.MØDREKVOTE_DAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.TETTE_SAKER_MELLOMROM_UKER;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public class Konfigurasjon {

    private static final LocalDate DATO_TIDLIGST = LocalDate.of(2010, JANUARY, 1);
    private static final LocalDate DATO_VEDTAK = LocalDate.of(2019, JANUARY, 1);
    private static final LocalDate DATO_PREMATUR = LocalDate.of(2019, JULY, 1);
    private static final LocalDate DATO_MINSTERETT_1 = LocalDate.of(2022, AUGUST, 2);
    private static final LocalDate DAG_FØR_MINSTERETT_1 = DATO_MINSTERETT_1.minusDays(1);


    public static final Konfigurasjon STANDARD = KonfigurasjonBuilder.create()
        /*
         * Stønadskontoer - alle parametre skal ha en sammenhengende tidslinje fra DATO_TIDLIGST med 0/dummy før ikrafttredelse
         * - Endring av kvoter/80% fom 1/1-2019
         * - Prematuruker 1/7-2019
         * - FAB 2/8-2022
         */
        // Stønadskontoer
        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 75)
        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 75)
        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 80)

        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_80, DATO_VEDTAK, null, 95)
        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, DATO_VEDTAK.minusDays(1), 75)

        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_80, DATO_VEDTAK, null, 95)
        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, DATO_VEDTAK.minusDays(1), 75)

        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_80, DATO_VEDTAK, null, 90)
        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, DATO_VEDTAK.minusDays(1), 130)

        .leggTilParameter(FORELDREPENGER_MOR_ALENEOMSORG_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 230)
        .leggTilParameter(FORELDREPENGER_MOR_ALENEOMSORG_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 280)
        .leggTilParameter(FORELDREPENGER_FAR_ALENEOMSORG_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 230)
        .leggTilParameter(FORELDREPENGER_FAR_ALENEOMSORG_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 280)
        .leggTilParameter(FORELDREPENGER_BARE_FAR_RETT_DAGER, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 200)
        .leggTilParameter(FORELDREPENGER_BARE_FAR_RETT_DAGER, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 250)
        .leggTilParameter(FORELDREPENGER_FØR_FØDSEL, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 15)
        .leggTilParameter(FORELDREPENGER_FØR_FØDSEL, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 15)

        // Utvidelse og dager uten aktivitetskrav flerbarn
        .leggTilParameter(EKSTRA_DAGER_TO_BARN, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 85)
        .leggTilParameter(EKSTRA_DAGER_TO_BARN, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 105)
        .leggTilParameter(EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, DEKNINGSGRAD_100, DATO_TIDLIGST, null, 230)
        .leggTilParameter(EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, DEKNINGSGRAD_80, DATO_TIDLIGST, null, 280)

        // Rettigheter 14-14 bare far rett - minsterett eller dager uten aktivitetskrav
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_100, DATO_MINSTERETT_1, null, 0)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_80, DATO_MINSTERETT_1, null, 0)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_100, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 75)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 95)
        .leggTilParameter(BARE_FAR_RETT_DAGER_MINSTERETT, DATO_MINSTERETT_1, null, 40)
        .leggTilParameter(BARE_FAR_RETT_DAGER_MINSTERETT, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_100, DATO_MINSTERETT_1, null, 75)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_100, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_80, DATO_MINSTERETT_1, null, 95)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)

        // Rettigheter fars uttak rundt fødsel
        .leggTilParameter(FAR_DAGER_RUNDT_FØDSEL, DATO_MINSTERETT_1, null, 10)
        .leggTilParameter(FAR_DAGER_RUNDT_FØDSEL, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)

        // Minsterett tette fødsler
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_FØDSEL, DATO_MINSTERETT_1, null, 110)
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_FØDSEL, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_ADOPSJON, DATO_MINSTERETT_1, null, 40)
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_ADOPSJON, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
        .leggTilParameter(FAR_TETTE_SAKER_DAGER_MINSTERETT, DATO_MINSTERETT_1, null, 40)
        .leggTilParameter(FAR_TETTE_SAKER_DAGER_MINSTERETT, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)

        // Grenser
        .leggTilParameter(TETTE_SAKER_MELLOMROM_UKER, DATO_MINSTERETT_1, null, 48)
        .leggTilParameter(TETTE_SAKER_MELLOMROM_UKER, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, -1) // "Umulig periode" før innføring
        .leggTilParameter(PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, DATO_PREMATUR, null, 52)
        .leggTilParameter(PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, DATO_TIDLIGST, DATO_PREMATUR.minusDays(1), -1) // "Umulig periode" før innføring
        .build();

    private final Map<Parametertype, Collection<Parameter>> parameterMap = new EnumMap<>(Parametertype.class);

    Konfigurasjon(Map<Parametertype, Collection<Parameter>> parameterMap) {
        this.parameterMap.putAll(parameterMap);
    }

    public int getParameter(Parametertype parametertype, Dekningsgrad dekningsgrad, final LocalDate dato) {
        return getParameterVerdier(parametertype).stream()
            .filter(p -> p.overlapper(dekningsgrad, dato))
            .findFirst()
            .map(Parameter::verdi)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ingen parameter funnet for " + parametertype.name() + " med dekningsgrad " + (dekningsgrad != null ? dekningsgrad : "") + " på dato " + dato));
    }

    Collection<Parameter> getParameterVerdier(Parametertype parametertype) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
            .orElseThrow(() -> new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype));
    }

}
