package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerVedBareFarRett.ID)
public class LeggTilDagerVedBareFarRett extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.2.4";
    private static final String DESC = "Legg til for bare far rett";

    public LeggTilDagerVedBareFarRett() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var flerbarn = LeggTilDagerVedFlereBarn.flerbarnsParametertype(grunnlag);
        var minsterett = minsterett(grunnlag);
        if (minsterett) {
            // Etter WLB 1: Flerbarn og mor ufør summeres. Ellers teller flerbarnsdagene som minsterett. Situasjonen kan utvikles over tid.
            if (grunnlag.isMorHarUføretrygd()) {
                mellomregning.getKontokonfigurasjon()
                    .add(new Kontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT));
                flerbarn.ifPresent(fbd -> mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, fbd)));
            } else if (flerbarn.isPresent()) {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, flerbarn.get()));
            } else {
                mellomregning.getKontokonfigurasjon()
                    .add(new Kontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT));
            }
        } else {
            if (grunnlag.isMorHarUføretrygd()) {
                mellomregning.getKontokonfigurasjon()
                    .add(new Kontokonfigurasjon(StønadskontoKontotype.UFØREDAGER, Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV));
            }
        }
        return ja();
    }

    public static boolean minsterett(BeregnKontoerGrunnlag grunnlag) {
        return Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, grunnlag.getKonfigurasjonsvalgdato()) > 0;
    }

}
