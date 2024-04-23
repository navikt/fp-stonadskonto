package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.TetteSakerUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilMinsterettDersomTetteSaker.ID)
public class LeggTilMinsterettDersomTetteSaker extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.2.5";
    private static final String DESC = "Legg til evt minsterett ved tette saker";

    public LeggTilMinsterettDersomTetteSaker() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = TetteSakerUtil.toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette && grunnlag.isMorRett()) {
            if (grunnlag.isGjelderFødsel()) {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL));
            } else {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON));
            }
        }
        if (toTette && grunnlag.isFarRett()) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_FAR, Parametertype.FAR_TETTE_SAKER_DAGER));
        }
        return ja();
    }

}
