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

    private final List<Kontokonfigurasjon> kontokonfigurasjon = new ArrayList<>();

    private final Map<StønadskontoKontotype, Integer> beregnet = new EnumMap<>(StønadskontoKontotype.class);

    private final Map<StønadskontoKontotype, Integer> flettet = new EnumMap<>(StønadskontoKontotype.class);

    public KontoerMellomregning(BeregnKontoerGrunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public BeregnKontoerGrunnlag getGrunnlag() {
        return grunnlag;
    }

    List<Kontokonfigurasjon> getKontokonfigurasjon() {
        return kontokonfigurasjon;
    }

    public Map<StønadskontoKontotype, Integer> getBeregnet() {
        return beregnet;
    }

    public Map<StønadskontoKontotype, Integer> getFlettet() {
        return flettet;
    }

}
