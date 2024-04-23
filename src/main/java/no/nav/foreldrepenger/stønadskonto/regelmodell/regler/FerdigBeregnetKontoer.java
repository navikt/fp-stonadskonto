package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FerdigBeregnetKontoer.ID)
class FerdigBeregnetKontoer extends LeafSpecification<KontoerMellomregning> {

    private static final String KONTOER = "KONTOER";

    public static final String ID = "FP_VK 17.5";

    FerdigBeregnetKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        Map<StønadskontoKontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoKontotype.class);
        mellomregning.getBeregnet().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .forEach(e -> kontoerMap.put(e.getKey(), e.getValue()));
        mellomregning.getBeregnet().clear();
        mellomregning.getBeregnet().putAll(kontoerMap);

        var eval = ja();
        eval.setEvaluationProperty(KONTOER, kontoerMap);
        return eval;

    }

}
