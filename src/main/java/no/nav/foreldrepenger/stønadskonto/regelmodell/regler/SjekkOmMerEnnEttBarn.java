package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMerEnnEttBarn.ID)
public class SjekkOmMerEnnEttBarn extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.1.2";

    public SjekkOmMerEnnEttBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        return mellomregning.getGrunnlag().getAntallBarn() > 1 ? ja() : nei();
    }
}
