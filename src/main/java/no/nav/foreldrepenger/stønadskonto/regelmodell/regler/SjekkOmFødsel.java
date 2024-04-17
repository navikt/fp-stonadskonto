package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFødsel.ID)
public class SjekkOmFødsel extends LeafSpecification<KontoerMellomregning> {
    public static final String ID = "FP_VK 17.1.10";

    public SjekkOmFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        return mellomregning.getGrunnlag().erFødsel() ? ja() : nei();
    }
}
