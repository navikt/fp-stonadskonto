package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class StandardKonfigurasjonTest {

    @Test
    void test_standard_konfiguration() {
        assertThat(Konfigurasjon.STANDARD.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100,
                LocalDate.of(2017, 12, 5))).isEqualTo(75);
        assertThat(Konfigurasjon.STANDARD.getParameter(Parametertype.MØDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100,
                LocalDate.of(2017, 12, 5))).isEqualTo(75);
    }

    @Test
    void hent_parameter_utenfor_periode_skal_gi_exception() {
        var fortidlig = LocalDate.of(1970, 12, 5);
        assertThrows(IllegalArgumentException.class,
                () -> Konfigurasjon.STANDARD.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, fortidlig));
    }

    private record Gruppering(Parametertype type, Dekningsgrad dekningsgrad) {}

    @Test
    void kontinuerlig_konfigurasjon() {
        var tidligsteDato = LocalDate.of(2010, JANUARY, 1);
        var tidslinje = new LocalDateTimeline<>(tidligsteDato, LocalDate.now(), Boolean.TRUE);
        Map<Gruppering, LocalDateTimeline<Integer>> parameterTidslinjer = new LinkedHashMap<>();
        Arrays.stream(Parametertype.values()).forEach(p -> {
            var parametre = Konfigurasjon.STANDARD.getParameterVerdier(p).stream()
                .collect(Collectors.groupingBy(pv -> new Gruppering(p, pv.dekningsgrad())));
            parametre.forEach((key, value) -> {
                var tl = value.stream()
                        .map(pv -> new LocalDateSegment<>(pv.fom(),
                            pv.tom() == LocalDate.MAX ? LocalDate.now() : pv.tom(), pv.verdi()))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
                parameterTidslinjer.put(key, tl);
            });
        });
        var ikketomme = parameterTidslinjer.entrySet().stream()
            .filter(e -> !tidslinje.disjoint(e.getValue()).isEmpty())
            .map(Map.Entry::getKey).toList();

        assertThat(ikketomme).isEqualTo(List.of());

    }

}
