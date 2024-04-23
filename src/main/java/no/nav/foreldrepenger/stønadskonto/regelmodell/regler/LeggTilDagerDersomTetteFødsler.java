package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.TetteFødslerUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerDersomTetteFødsler.ID)
public class LeggTilDagerDersomTetteFødsler extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.6";

    public LeggTilDagerDersomTetteFødsler() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = TetteFødslerUtil.toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette && grunnlag.isMorRett()) {
            if (grunnlag.isGjelderFødsel()) {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL));
            } else {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON));
            }
        }
        if (toTette && grunnlag.isFarRett()) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_FAR, Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT));
        }
        return ja();
    }

}
