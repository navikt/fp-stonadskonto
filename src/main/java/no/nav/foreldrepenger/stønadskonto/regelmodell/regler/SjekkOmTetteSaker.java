package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.TetteSakerUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTetteSaker.ID)
public class SjekkOmTetteSaker extends LeafSpecification<KontoerMellomregning> {
    static final String ID = "FP_VK 17.1.11";
    private static final String DESC = "Hvis tette saker";

    public SjekkOmTetteSaker() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var toTette = TetteSakerUtil.toTette(grunnlag.getKonfigurasjonsvalgdato(), grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        return toTette ? ja() : nei();
    }
}
