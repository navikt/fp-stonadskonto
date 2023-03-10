package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkOmToBarn extends LeafSpecification<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17.1.2.1";

    public SjekkOmToBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.getAntallBarn() == 2) {
            return ja();
        }
        return nei();
    }


}
