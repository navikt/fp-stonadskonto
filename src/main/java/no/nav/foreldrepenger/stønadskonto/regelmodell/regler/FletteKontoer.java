package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FletteKontoer.ID)
class FletteKontoer extends LeafSpecification<KontoerMellomregning> {

    private static final String KONTOER = "KONTOER";
    private static final String ANTALL_FLERBARN_DAGER = "ANTALL_FLERBARN_DAGER";
    private static final String ANTALL_PREMATUR_DAGER = "ANTALL_PREMATUR_DAGER";

    public static final String ID = "FP_VK 17.4";

    FletteKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        Map<StønadskontoBeregningStønadskontotype, Integer> beregnet = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        beregnet.putAll(mellomregning.getBeregnet());
        Map<StønadskontoBeregningStønadskontotype, Integer> opprinnelig = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        opprinnelig.putAll(mellomregning.getGrunnlag().getTidligereUtregning());
        Map<StønadskontoBeregningStønadskontotype, Integer> endelig = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);

        Arrays.stream(StønadskontoBeregningStønadskontotype.values()).forEach(konto -> {
            Optional<Integer> verdi = switch (konto.getKontoKategori()) {
                case STØNADSDAGER -> Optional.ofNullable(opprinnelig.get(konto)).or(() -> Optional.ofNullable(beregnet.get(konto)));
                case UTVIDELSE -> Optional.ofNullable(opprinnelig.get(konto)).or(() -> Optional.ofNullable(beregnet.get(konto)));
                case AKTIVITETSKRAV -> Optional.ofNullable(beregnet.get(konto));
                case MINSTERETT -> Optional.ofNullable(beregnet.get(konto));
                case ANNET -> Optional.ofNullable(beregnet.get(konto));
            };
            verdi.ifPresent(v -> endelig.put(konto, v));
        });


        mellomregning.getBeregnet().clear();
        mellomregning.getBeregnet().putAll(endelig);
        return ja();
    }


}
