package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FletteKontoer.ID)
class FletteKontoer extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.4";

    FletteKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        if (mellomregning.getGrunnlag().getTidligereUtregning().isEmpty()) {
            return ja();
        }
        Map<StønadskontoBeregningStønadskontotype, Integer> beregnet = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        beregnet.putAll(mellomregning.getBeregnet());
        Map<StønadskontoBeregningStønadskontotype, Integer> opprinnelig = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        opprinnelig.putAll(mellomregning.getGrunnlag().getTidligereUtregning());
        Map<StønadskontoBeregningStønadskontotype, Integer> endelig = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);

        /*
         * Opp til konsument å sjekke eventuell differanse og lagre unna.
         * - Stønadsdager, utvidelser og flerbarnsdager: Beholder opprinnelig dersom ulik konfig, ellers maxverdi
         * - Dette vil dekke ulikhet i antall barn, prematurdager, etc. Men ved endret dekningsgrad bør det sendes inn tom tidligere beregning
         * - Øvrige retter: Bruk alltid siste utregning.
         */
        var sammeStønadskonfig = sammeOppsettStønadsdager(opprinnelig.keySet(), beregnet.keySet());
        BiFunction<Integer, Integer, Optional<Integer>> kontovelger = sammeStønadskonfig ? FletteKontoer::max : FletteKontoer::lhs;

        Arrays.stream(StønadskontoBeregningStønadskontotype.values()).forEach(konto -> {
            Optional<Integer> verdi = switch (konto.getKontoKategori()) {
                case STØNADSDAGER, UTVIDELSE -> kontovelger.apply(opprinnelig.get(konto), beregnet.get(konto));
                case AKTIVITETSKRAV -> switch (konto) {
                    case FLERBARNSDAGER -> kontovelger.apply(opprinnelig.get(konto), beregnet.get(konto));
                    case UFØREDAGER -> Optional.ofNullable(beregnet.get(konto));
                    default -> Optional.empty();
                };
                case MINSTERETT, ANNET -> Optional.ofNullable(beregnet.get(konto));
            };
            verdi.ifPresent(v -> endelig.put(konto, v));
        });

        mellomregning.getBeregnet().clear();
        mellomregning.getBeregnet().putAll(endelig);
        return ja();
    }

    private static boolean sammeOppsettStønadsdager(Set<StønadskontoBeregningStønadskontotype> s1,
                                                    Set<StønadskontoBeregningStønadskontotype> s2) {
        var fellesperiodeBegge = s1.contains(StønadskontoBeregningStønadskontotype.FELLESPERIODE) &&
            s2.contains(StønadskontoBeregningStønadskontotype.FELLESPERIODE);
        var foreldrepengerBegge = s1.contains(StønadskontoBeregningStønadskontotype.FORELDREPENGER) &&
            s2.contains(StønadskontoBeregningStønadskontotype.FORELDREPENGER);
        return fellesperiodeBegge || foreldrepengerBegge;
    }

    private static Optional<Integer> lhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(lhs);
    }

    private static Optional<Integer> rhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(rhs);
    }

    private static Optional<Integer> max(Integer lhs, Integer rhs) {
        var lhs0 = Optional.ofNullable(lhs).orElse(0);
        var rhs0 = Optional.ofNullable(rhs).orElse(0);
        return Optional.of(Math.max(lhs0, rhs0)).filter(v -> v > 0);
    }


}
