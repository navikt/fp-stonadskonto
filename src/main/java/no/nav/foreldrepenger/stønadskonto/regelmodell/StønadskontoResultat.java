package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class StønadskontoResultat {

    // Fletting som velger max av input
    private final Map<StønadskontoKontotype, Integer> stønadskontoer = new EnumMap<>(StønadskontoKontotype.class);
    // Normal fletting av input (behold kontoer) med dynamisk rettighet
    private final Map<StønadskontoKontotype, Integer> stønadskontoerBeholdStønadsdager = new EnumMap<>(StønadskontoKontotype.class);

    private final Map<StønadskontoKontotype, Integer> stønadskontoerFørFletting = new EnumMap<>(StønadskontoKontotype.class);
    private final String evalueringResultat;
    private final String innsendtGrunnlag;
    private final String  regelVersjon;
    private final Integer antallFlerbarnsdager;
    private final Integer antallPrematurDager;

    public StønadskontoResultat(Map<StønadskontoKontotype, Integer> stønadskontoer,
                                Map<StønadskontoKontotype, Integer> stønadskontoerBeholdStønadsdager,
                                Map<StønadskontoKontotype, Integer> stønadskontoerFørFletting,
                                Integer antallFlerbarnsdager,
                                String evalueringResultat,
                                String innsendtGrunnlag,
                                Integer antallPrematurDager) {
        this.antallPrematurDager = antallPrematurDager;
        Objects.requireNonNull(stønadskontoer);
        Objects.requireNonNull(evalueringResultat);
        Objects.requireNonNull(innsendtGrunnlag);
        this.stønadskontoer.putAll(stønadskontoer);
        this.stønadskontoerBeholdStønadsdager.putAll(stønadskontoerBeholdStønadsdager);
        this.stønadskontoerFørFletting.putAll(stønadskontoerFørFletting);
        this.antallFlerbarnsdager = antallFlerbarnsdager;
        this.evalueringResultat = evalueringResultat;
        this.innsendtGrunnlag = innsendtGrunnlag;
        this.regelVersjon = StønadskontoVersion.STØNADSKONTO_VERSION.version();
    }

    public Map<StønadskontoKontotype, Integer> getStønadskontoer() {
        return Collections.unmodifiableMap(stønadskontoer);
    }

    public Map<StønadskontoKontotype, Integer> getStønadskontoerBeholdStønadsdager() {
        return Collections.unmodifiableMap(stønadskontoerBeholdStønadsdager);
    }

    public Map<StønadskontoKontotype, Integer> getStønadskontoerFørFletting() {
        return stønadskontoerFørFletting;
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
