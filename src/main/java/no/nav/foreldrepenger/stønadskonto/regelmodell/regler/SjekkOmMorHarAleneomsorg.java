package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorHarAleneomsorg.ID)
public class SjekkOmMorHarAleneomsorg extends LeafSpecification<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17.1.5";

    public SjekkOmMorHarAleneomsorg() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        return grunnlag.isMorAleneomsorg() ? ja() : nei();
    }
}
