package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerDersomPrematur.ID)
public class LeggTilDagerDersomPrematur extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.4";

    public LeggTilDagerDersomPrematur() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        if (grunnlag.erFødsel()) {
            var fødselsdato = grunnlag.getFødselsdato().orElse(null);
            var termindato = grunnlag.getTermindato().orElse(null);
            if (PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR, null));
            }
        }
        return ja();
    }
}
