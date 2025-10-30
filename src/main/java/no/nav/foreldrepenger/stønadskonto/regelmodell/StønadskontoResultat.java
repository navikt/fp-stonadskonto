package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class StønadskontoResultat {

    private final Map<StønadskontoKontotype, Integer> stønadskontoer = new EnumMap<>(StønadskontoKontotype.class);

    public StønadskontoResultat(Map<StønadskontoKontotype, Integer> stønadskontoer) {
        Objects.requireNonNull(stønadskontoer);
        this.stønadskontoer.putAll(stønadskontoer);
    }

    public Map<StønadskontoKontotype, Integer> getStønadskontoer() {
        return Collections.unmodifiableMap(stønadskontoer);
    }

}
