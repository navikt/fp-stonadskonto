package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VirkedagerTest {
    private Map<DayOfWeek, LocalDate> uke;

    @BeforeEach
    void setUp() {
        var iDag = LocalDate.now();
        var mandag = iDag.minusDays(iDag.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        uke = Stream.of(DayOfWeek.values()).collect(Collectors.toMap(day -> day, day -> mandag.plusDays(day.ordinal())));
    }

    @Test
    void skalBeregneAntallVirkedager() {
        var mandag = getDayOfWeek(DayOfWeek.MONDAY);
        var søndag = getDayOfWeek(DayOfWeek.SUNDAY);

        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag.plusDays(1))).isEqualTo(6);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag.plusDays(10))).isEqualTo(13);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(1), søndag)).isEqualTo(4);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(1), søndag.plusDays(1))).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(4), søndag)).isEqualTo(1);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(5), søndag)).isEqualTo(0);

        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(1), søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(2), søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(3), søndag)).isEqualTo(6);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(3), søndag.plusDays(1))).isEqualTo(7);
    }

    private LocalDate getDayOfWeek(DayOfWeek dayOfWeek) {
        var date = uke.get(dayOfWeek);
        assertThat(date.getDayOfWeek()).isEqualTo(dayOfWeek);
        return date;
    }
}
