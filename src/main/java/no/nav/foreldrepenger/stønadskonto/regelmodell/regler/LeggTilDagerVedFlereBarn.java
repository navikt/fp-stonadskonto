package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerVedFlereBarn.ID)
public class LeggTilDagerVedFlereBarn extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.2.3";
    private static final String DESC = "Legg til evt flerbarnsutvidelse";

    public LeggTilDagerVedFlereBarn() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var parametertypeFlerbarn = flerbarnsParametertype(grunnlag).orElseThrow();
        mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TILLEGG_FLERBARN, parametertypeFlerbarn));
        if (aktivitetsKravIkkeMinsterett(grunnlag)) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.FLERBARNSDAGER, parametertypeFlerbarn));
        }

        return ja();
    }

    private static boolean aktivitetsKravIkkeMinsterett(BeregnKontoerGrunnlag grunnlag) {
        var beggeRett = grunnlag.isBeggeRett();
        var bareFarRett = grunnlag.isBareFarHarRett();
        var bareFarMinsterett = LeggTilDagerVedBareFarRett.minsterett(grunnlag);
        return beggeRett || (bareFarRett && !bareFarMinsterett);
    }

    public static Optional<Parametertype> flerbarnsParametertype(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.getAntallBarn() == 2) {
            return Optional.of(Parametertype.EKSTRA_DAGER_TO_BARN);
        } else if (grunnlag.getAntallBarn() > 2) {
            return Optional.of(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN);
        } else {
            return Optional.empty();
        }
    }
}
