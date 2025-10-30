package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FerdigBeregnetKontoer.ID)
class FerdigBeregnetKontoer extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.5";
    private static final String DESC = "Fastsett beregning";

    FerdigBeregnetKontoer() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        fjernInnslag0(mellomregning.getBeregnet());
        return ja();

    }

    private static void fjernInnslag0(Map<StønadskontoKontotype, Integer> input) {
        if (input.values().stream().anyMatch(v -> v <= 0)) {
            var midlertidig = input.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            input.clear();
            input.putAll(midlertidig);
        }
    }

}
