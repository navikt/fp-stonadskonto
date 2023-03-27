package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import static java.time.LocalDate.*;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad.*;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public class Konfigurasjon {

    public static final Konfigurasjon STANDARD = KonfigurasjonBuilder.create()
        /*
         * Stønadskontoer
         * - Endring av kvoter/80% fom 1/1-2019
         * - Prematuruker 1/7-2019
         * - FAB 2/8-2022
         */
        // Stønadskontoer
        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 75)
        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 75)
        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_80, of(2019, JANUARY, 1), null, 95)
        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_80, of(2019, JANUARY, 1), null, 95)
        .leggTilParameter(MØDREKVOTE_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), of(2018, DECEMBER, 31), 75)
        .leggTilParameter(FEDREKVOTE_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), of(2018, DECEMBER, 31), 75)

        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 80)
        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_80, of(2019, JANUARY, 1), null, 90)
        .leggTilParameter(FELLESPERIODE_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), of(2018, DECEMBER, 31), 130)
        .leggTilParameter(FORELDREPENGER_MOR_ALENEOMSORG_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 230)
        .leggTilParameter(FORELDREPENGER_MOR_ALENEOMSORG_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 280)
        .leggTilParameter(FORELDREPENGER_FAR_ALENEOMSORG_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 230)
        .leggTilParameter(FORELDREPENGER_FAR_ALENEOMSORG_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 280)
        .leggTilParameter(FORELDREPENGER_BARE_FAR_RETT_DAGER, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 200)
        .leggTilParameter(FORELDREPENGER_BARE_FAR_RETT_DAGER, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 250)
        .leggTilParameter(FORELDREPENGER_FØR_FØDSEL, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 15)
        .leggTilParameter(FORELDREPENGER_FØR_FØDSEL, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 15)

        // Ekstradager og Minsteretter
        .leggTilParameter(EKSTRA_DAGER_TO_BARN, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 85)
        .leggTilParameter(EKSTRA_DAGER_TO_BARN, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 105)
        .leggTilParameter(EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 230)
        .leggTilParameter(EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 280)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_100, of(2010, JANUARY, 1), null, 75)
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, DEKNINGSGRAD_80, of(2010, JANUARY, 1), null, 95)
        .leggTilParameter(BARE_FAR_RETT_DAGER_MINSTERETT, of(2017, JANUARY, 1), null, 40) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_100, of(2017, JANUARY, 1), null, 75) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, DEKNINGSGRAD_80, of(2017, JANUARY, 1), null, 95) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(FAR_DAGER_RUNDT_FØDSEL, of(2017, JANUARY, 1), null, 10) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_FØDSEL, of(2017, JANUARY, 1), null, 110) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(MOR_TETTE_SAKER_DAGER_ADOPSJON, of(2017, JANUARY, 1), null, 40) // TODO: endre til aug 2022 etter overgang
        .leggTilParameter(FAR_TETTE_SAKER_DAGER_MINSTERETT, of(2017, JANUARY, 1), null, 40) // TODO: endre til aug 2022 etter overgang

        // Grenser
        .leggTilParameter(TETTE_SAKER_MELLOMROM_UKER, of(2017, JANUARY, 1), null, 48)  // TODO: endre til aug 2022 el 48 uker tidligere etter overgang
        .leggTilParameter(PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, of(2019, JULY, 1), null, 52)
        .build();

    public static final LocalDate PREMATURUKER_REGELENDRING_START_DATO = of(2019, 7, 1);

    private final Map<Parametertype, Collection<Parameter>> parameterMap = new EnumMap<>(Parametertype.class);

    Konfigurasjon(Map<Parametertype, Collection<Parameter>> parameterMap) {
        this.parameterMap.putAll(parameterMap);
    }

    public Integer getParameter(Parametertype parametertype, Dekningsgrad dekningsgrad, final LocalDate dato) {
        return getParameterVerdier(parametertype).stream()
            .filter(p -> p.overlapper(dekningsgrad, dato))
            .findFirst()
            .map(Parameter::verdi)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ingen parameter funnet for " + parametertype.name() + " med dekningsgrad " + (dekningsgrad != null ? dekningsgrad : "") + " på dato " + dato));
    }

    private Collection<Parameter> getParameterVerdier(Parametertype parametertype) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
            .orElseThrow(() -> new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype));
    }

}
