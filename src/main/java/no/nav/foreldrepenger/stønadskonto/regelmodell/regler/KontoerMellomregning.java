package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class KontoerMellomregning {

    private final BeregnKontoerGrunnlag grunnlag;

    private final Map<StønadskontoKontotype, Integer> beregnet = new EnumMap<>(StønadskontoKontotype.class);

    private final List<Kontokonfigurasjon> kontokonfigurasjon = new ArrayList<>();

    public KontoerMellomregning(BeregnKontoerGrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public BeregnKontoerGrunnlag getGrunnlag() {
        return grunnlag;
    }

    public Map<StønadskontoKontotype, Integer> getBeregnet() {
        return beregnet;
    }

    List<Kontokonfigurasjon> getKontokonfigurasjon() {
        return kontokonfigurasjon;
    }
}
