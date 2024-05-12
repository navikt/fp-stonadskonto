package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTidligereBeregnet.ID)
public class SjekkOmTidligereBeregnet extends LeafSpecification<KontoerMellomregning> {
    static final String ID = "FP_VK 17.1.13";
    private static final String DESC = "Hvis beregnet tidligere";

    public SjekkOmTidligereBeregnet() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        return grunnlag.getTidligereUtregning().isEmpty() ? nei() : ja();
    }
}
