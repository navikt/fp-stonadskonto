package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public class KonfigurasjonBuilder {

    private final Map<Parametertype, Collection<Parameter>> parameterMap;

    private KonfigurasjonBuilder() {
        this.parameterMap = new EnumMap<>(Parametertype.class);
    }

    public static KonfigurasjonBuilder create() {
        return new KonfigurasjonBuilder();
    }

    public KonfigurasjonBuilder leggTilParameter(Parametertype parametertype, LocalDate fom, LocalDate tom, int verdi) {
        return leggTilParameter(parametertype, null, fom, tom, verdi);
    }

    public KonfigurasjonBuilder leggTilParameter(Parametertype parametertype, Dekningsgrad dekningsgrad, LocalDate fom, LocalDate tom, int verdi) {
        var nyParameter = new Parameter(dekningsgrad, fom, tom, verdi);
        var parameterListe = parameterMap.get(parametertype);
        if (parameterListe == null) {
            Collection<Parameter> coll = new ArrayList<>(List.of(nyParameter));
            parameterMap.put(parametertype, coll);
        } else {
            var overlapp = parameterListe.stream().anyMatch(nyParameter::overlapper);
            if (overlapp) {
                throw new IllegalArgumentException("Overlappende perioder kan ikke eksistere i konfigurasjon.");
            }
            parameterListe.add(nyParameter);
        }
        return this;
    }

    public Konfigurasjon build() {
        return new Konfigurasjon(parameterMap);
    }

}
