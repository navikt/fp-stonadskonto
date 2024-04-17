package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class KontoerMellomregning {

    private final BeregnKontoerGrunnlag grunnlag;

    private final Map<StønadskontoBeregningStønadskontotype, Integer> beregnet = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);

    private final List<Kontokonfigurasjon> kontokonfigurasjon = new ArrayList<>();

    public KontoerMellomregning(BeregnKontoerGrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public BeregnKontoerGrunnlag getGrunnlag() {
        return grunnlag;
    }

    public Map<StønadskontoBeregningStønadskontotype, Integer> getBeregnet() {
        return beregnet;
    }

    List<Kontokonfigurasjon> getKontokonfigurasjon() {
        return kontokonfigurasjon;
    }
}
