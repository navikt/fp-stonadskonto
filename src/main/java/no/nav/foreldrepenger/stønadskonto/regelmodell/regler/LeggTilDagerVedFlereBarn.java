package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerVedFlereBarn.ID)
public class LeggTilDagerVedFlereBarn extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.3";

    public LeggTilDagerVedFlereBarn() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var parametertypeFlerbarn = mellomregning.getGrunnlag().getAntallBarn() == 2 ? Parametertype.EKSTRA_DAGER_TO_BARN : Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN;
        mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TILLEGG_FLERBARN, parametertypeFlerbarn));
        if (aktivitetsKravIkkeMinsterett(grunnlag)) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.FLERBARNSDAGER, parametertypeFlerbarn));
        }

        return ja();
    }

    private static boolean aktivitetsKravIkkeMinsterett(BeregnKontoerGrunnlag grunnlag) {
        var beggeRett = grunnlag.isBeggeRett();
        var bareFarRett = grunnlag.isBareFarHarRett();
        var bareFarMinsterett = Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, grunnlag.getKonfigurasjonsvalgdato()) > 0;
        return beggeRett || (bareFarRett && !bareFarMinsterett);
    }
}
