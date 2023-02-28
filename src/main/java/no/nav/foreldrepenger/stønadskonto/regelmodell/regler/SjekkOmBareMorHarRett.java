package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBareMorHarRett.ID)
public class SjekkOmBareMorHarRett extends LeafSpecification<BeregnKontoerGrunnlag> {
    public static final String ID = "FP_VK 17.1.4";

    public SjekkOmBareMorHarRett() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.isMorRett() && !grunnlag.isFarRett()) {
            return ja();
        }
        return nei();
    }
}
