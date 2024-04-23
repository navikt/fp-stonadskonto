package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareFarHarRett.ID)
public class SjekkOmBareFarHarRett extends LeafSpecification<KontoerMellomregning> {
    static final String ID = "FP_VK 17.1.6";
    private static final String DESC = "Hvis bare far har rett";

    public SjekkOmBareFarHarRett() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        return mellomregning.getGrunnlag().isBareFarHarRett() ? ja() : nei();
    }
}
