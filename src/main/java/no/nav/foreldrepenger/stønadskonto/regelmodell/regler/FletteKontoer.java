package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FletteKontoer.ID)
class FletteKontoer extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.4";
    private static final String DESC = "Flette resultat og tidligere beregning";

    FletteKontoer() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var beregnet = new EnumMap<>(mellomregning.getBeregnet());
        var tidligereBeregnet = new EnumMap<>(mellomregning.getGrunnlag().getTidligereUtregning());

        /*
         * For utprøving ved migrering i fpsak. Mulig denne blir default
         * Beholder input-verdi av stønadsdager, utvidelser og flerbarnsdager. Fersk utregning av øvrige
         */
        Map<StønadskontoKontotype, Integer> flettetBeholdStønad = new EnumMap<>(StønadskontoKontotype.class);
        Arrays.stream(StønadskontoKontotype.values())
            .forEach(konto -> flettVerdi(konto, FletteKontoer::lhs, tidligereBeregnet.get(konto), beregnet.get(konto)).ifPresent(v -> flettetBeholdStønad.put(konto, v)));
        mellomregning.getFlettetBeholdStønadsdager().putAll(flettetBeholdStønad);

        /*
         * Stønadsdager, utvidelser og flerbarnsdager: Beholder opprinnelig dersom ulik konfig, ellers beregning med maxverdi
         * Øvrige retter: Bruk alltid siste utregning.
         *
         * OBS endringer i barn, prematurdager og dekningsgrad som gir redusert antall dager. Mulig fletting flyttes til konsument.
         */
        Map<StønadskontoKontotype, Integer> flettetBrukMaxStønad = new EnumMap<>(StønadskontoKontotype.class);
        var kontovelgerMax = utledMaxKontoVelger(tidligereBeregnet, beregnet);
        Arrays.stream(StønadskontoKontotype.values())
            .forEach(konto -> flettMax(konto, kontovelgerMax, tidligereBeregnet.get(konto), beregnet.get(konto)).ifPresent(v -> flettetBrukMaxStønad.put(konto, v)));
        mellomregning.getFlettet().putAll(flettetBrukMaxStønad);
        return ja();
    }

    private static Optional<Integer> flettVerdi(StønadskontoKontotype konto, BiFunction<Integer, Integer, Optional<Integer>> kontovelger,
                                               Integer opprinnelig, Integer beregnet) {
        return switch (konto) {
            case FELLESPERIODE, MØDREKVOTE, FEDREKVOTE, FORELDREPENGER, FORELDREPENGER_FØR_FØDSEL,
                TILLEGG_FLERBARN, TILLEGG_PREMATUR, FLERBARNSDAGER -> kontovelger.apply(opprinnelig, beregnet).filter(v -> v > 0);
            case UFØREDAGER, TETTE_SAKER_MOR, TETTE_SAKER_FAR, BARE_FAR_RETT, FAR_RUNDT_FØDSEL -> Optional.ofNullable(beregnet).filter(v -> v > 0);
        };
    }

    private static Optional<Integer> flettMax(StønadskontoKontotype konto, BiFunction<Integer, Integer, Optional<Integer>> kontovelger,
                                              Integer opprinnelig, Integer beregnet) {
        return switch (konto) {
            case FELLESPERIODE, MØDREKVOTE, FEDREKVOTE, FORELDREPENGER, FORELDREPENGER_FØR_FØDSEL,
                TILLEGG_FLERBARN, TILLEGG_PREMATUR, FLERBARNSDAGER -> kontovelger.apply(opprinnelig, beregnet).filter(v -> v > 0);
            case UFØREDAGER, TETTE_SAKER_MOR, TETTE_SAKER_FAR, BARE_FAR_RETT, FAR_RUNDT_FØDSEL -> Optional.ofNullable(beregnet).filter(v -> v > 0);
        };
    }

    private static BiFunction<Integer, Integer, Optional<Integer>> utledMaxKontoVelger(Map<StønadskontoKontotype, Integer> m1,
                                                                                       Map<StønadskontoKontotype, Integer> m2) {
        var fellesperiodeBegge = m1.containsKey(StønadskontoKontotype.FELLESPERIODE) &&
            m2.containsKey(StønadskontoKontotype.FELLESPERIODE);
        var foreldrepengerBegge = m1.containsKey(StønadskontoKontotype.FORELDREPENGER) &&
            m2.containsKey(StønadskontoKontotype.FORELDREPENGER);
        if (fellesperiodeBegge) {
            return m1.get(StønadskontoKontotype.FELLESPERIODE) > m2.get(StønadskontoKontotype.FELLESPERIODE)
                ? FletteKontoer::lhs : FletteKontoer::rhs;
        } else if (foreldrepengerBegge) {
            return m1.get(StønadskontoKontotype.FORELDREPENGER) > m2.get(StønadskontoKontotype.FORELDREPENGER)
                ? FletteKontoer::lhs : FletteKontoer::rhs;
        } else {
            return FletteKontoer::lhs;
        }
    }

    private static Optional<Integer> lhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(lhs);
    }

    private static Optional<Integer> rhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(rhs);
    }

}
