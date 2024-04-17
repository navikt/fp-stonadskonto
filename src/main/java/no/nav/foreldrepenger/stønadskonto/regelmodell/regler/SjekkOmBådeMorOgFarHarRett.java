package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBådeMorOgFarHarRett.ID)
public class SjekkOmBådeMorOgFarHarRett extends LeafSpecification<KontoerMellomregning> {
    public static final String ID = "FP_VK 17.1.3";

    public SjekkOmBådeMorOgFarHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        if (mellomregning.getGrunnlag().isMorRett() && mellomregning.getGrunnlag().isFarRett()) {
            return ja();
        }
        return nei();
    }
}
