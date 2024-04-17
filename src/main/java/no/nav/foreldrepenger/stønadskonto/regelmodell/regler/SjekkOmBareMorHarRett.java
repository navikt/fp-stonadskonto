package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareMorHarRett.ID)
public class SjekkOmBareMorHarRett extends LeafSpecification<KontoerMellomregning> {
    public static final String ID = "FP_VK 17.1.4";

    public SjekkOmBareMorHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        return mellomregning.getGrunnlag().isBareMorHarRett() ? ja() :  nei();
    }
}
