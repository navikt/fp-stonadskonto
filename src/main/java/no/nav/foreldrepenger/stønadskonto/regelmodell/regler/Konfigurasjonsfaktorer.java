package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

public class Konfigurasjonsfaktorer {

    enum Berettiget {
        MOR,
        FAR,
        FAR_ALENE,
        BEGGE
    }

    static final Map<Berettiget, List<Kontokonfigurasjon>> KONFIGURASJONER_FELLES = Map.ofEntries(
        Map.entry(Berettiget.MOR,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_MOR_ALENEOMSORG_DAGER))),
        Map.entry(Berettiget.FAR,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER))),
        Map.entry(Berettiget.FAR_ALENE,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_FAR_ALENEOMSORG_DAGER))),
        Map.entry(Berettiget.BEGGE,
            List.of(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_DAGER),
                new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER),
                new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER))));
    

}
