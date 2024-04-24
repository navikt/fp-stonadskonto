package no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class VirkedagerTest {

    @Test
    void skalBeregneAntallVirkedager() {
        var iDag = LocalDate.now();
        var mandag = iDag.with(DayOfWeek.MONDAY);
        var søndag = iDag.with(DayOfWeek.SUNDAY);

        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag.plusDays(1))).isEqualTo(6);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag, søndag.plusDays(10))).isEqualTo(13);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(1), søndag)).isEqualTo(4);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(1), søndag.plusDays(1))).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(4), søndag)).isEqualTo(1);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.plusDays(5), søndag)).isZero();

        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(1), søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(2), søndag)).isEqualTo(5);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(3), søndag)).isEqualTo(6);
        assertThat(PrematurukerUtil.beregnAntallVirkedager(mandag.minusDays(3), søndag.plusDays(1))).isEqualTo(7);
    }

}
