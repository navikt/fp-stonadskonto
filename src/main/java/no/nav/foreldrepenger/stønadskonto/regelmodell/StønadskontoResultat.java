package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class StønadskontoResultat {

    private final Map<StønadskontoKontotype, Integer> stønadskontoer;
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final String  regelVersjon;
    private final Integer antallFlerbarnsdager;
    private final Integer antallPrematurDager;

    public StønadskontoResultat(Map<StønadskontoKontotype, Integer> stønadskontoer,
                                Integer antallFlerbarnsdager,
                                String evalueringResultat,
                                String innsendtGrunnlag,
                                Integer antallPrematurDager) {
        this.antallPrematurDager = antallPrematurDager;
        Objects.requireNonNull(stønadskontoer);
        Objects.requireNonNull(evalueringResultat);
        Objects.requireNonNull(innsendtGrunnlag);
        this.stønadskontoer = stønadskontoer;
        this.antallFlerbarnsdager = antallFlerbarnsdager;
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
        this.regelVersjon = StønadskontoVersion.STØNADSKONTO_VERSION.version();
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

    public Integer getAntallFlerbarnsdager() {
        return antallFlerbarnsdager;
    }

    public Integer getAntallPrematurDager() {
        return antallPrematurDager;
    }
}
