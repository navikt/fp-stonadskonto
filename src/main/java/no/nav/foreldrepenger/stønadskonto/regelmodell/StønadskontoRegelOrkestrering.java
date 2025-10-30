package no.nav.foreldrepenger.stønadskonto.regelmodell;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.regler.BeregnKontoer;
import no.nav.foreldrepenger.stønadskonto.regelmodell.regler.KontoerMellomregning;

public class StønadskontoRegelOrkestrering {

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag) {
        if (grunnlag.getDekningsgrad() == null) {
            throw new IllegalArgumentException("Mangler dekningsgrad");
        }
        var mellomregning = new KontoerMellomregning(grunnlag);
        var beregnKontoer = new BeregnKontoer();
        beregnKontoer.evaluer(mellomregning);

        return new StønadskontoResultat(mellomregning.getBeregnet());
    }
}
