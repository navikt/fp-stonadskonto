package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerVedFødsel.ID)
public class LeggTilDagerVedFødsel extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.2";

    public LeggTilDagerVedFødsel() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        if (grunnlag.isMorRett()) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL));
        }
        if (grunnlag.isFarRett()) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.FAR_RUNDT_FØDSEL, Parametertype.FAR_DAGER_RUNDT_FØDSEL));
        }

        return ja();
    }
}
