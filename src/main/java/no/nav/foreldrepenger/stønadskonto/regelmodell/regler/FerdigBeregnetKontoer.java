package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FerdigBeregnetKontoer.ID)
class FerdigBeregnetKontoer extends LeafSpecification<KontoerMellomregning> {

    private static final String KONTOER = "KONTOER";
    private static final String ANTALL_FLERBARN_DAGER = "ANTALL_FLERBARN_DAGER";
    private static final String ANTALL_PREMATUR_DAGER = "ANTALL_PREMATUR_DAGER";

    public static final String ID = "FP_VK 17.5";

    FerdigBeregnetKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        mellomregning.getBeregnet().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .forEach(e -> kontoerMap.put(e.getKey(), e.getValue()));
        mellomregning.getBeregnet().clear();
        mellomregning.getBeregnet().putAll(kontoerMap);

        return beregnetMedResultat(kontoerMap,
            Optional.ofNullable(kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_FLERBARN)).orElse(0),
            Optional.ofNullable(kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR)).orElse(0));
    }

    private Evaluation beregnetMedResultat(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer,
                                           Integer antallExtraBarnDager,
                                           Integer antallPrematurDager) {
        var outcome = new KontoOutcome(kontoer)
            .medAntallExtraBarnDager(antallExtraBarnDager)
            .medAntallPrematurDager(antallPrematurDager);
        var eval = ja(outcome);
        eval.setEvaluationProperty(KONTOER, kontoer);
        eval.setEvaluationProperty(ANTALL_FLERBARN_DAGER, antallExtraBarnDager);
        eval.setEvaluationProperty(ANTALL_PREMATUR_DAGER, antallPrematurDager);

        return eval;
    }

}
