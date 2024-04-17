package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareFarHarRett.ID)
public class SjekkOmBareFarHarRett extends LeafSpecification<KontoerMellomregning> {
    public static final String ID = "FP_VK 17.1.6";

    public SjekkOmBareFarHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        if (mellomregning.getGrunnlag().isFarRett() && !mellomregning.getGrunnlag().isMorRett()) {
            return ja();
        }
        return nei();
    }
}
