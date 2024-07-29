package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class StønadskontoResultat {

    // Fletting som velger max av input
    private final Map<StønadskontoKontotype, Integer> stønadskontoer = new EnumMap<>(StønadskontoKontotype.class);
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final String  regelVersjon;

    public StønadskontoResultat(Map<StønadskontoKontotype, Integer> stønadskontoer, String evalueringResultat,
                                String innsendtGrunnlag) {
        Objects.requireNonNull(stønadskontoer);
        Objects.requireNonNull(evalueringResultat);
        Objects.requireNonNull(innsendtGrunnlag);
        this.stønadskontoer.putAll(stønadskontoer);
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
        this.regelVersjon = StønadskontoVersion.STØNADSKONTO_VERSION.nameAndVersion();
    }

    public Map<StønadskontoKontotype, Integer> getStønadskontoer() {
        return Collections.unmodifiableMap(stønadskontoer);
    }

    public String getEvalueringResultat() {
        return evalueringResultat;
    }

    public String getInnsendtGrunnlag() {
        return innsendtGrunnlag;
    }

    public String getRegelVersjon() {
        return regelVersjon;
    }

}
