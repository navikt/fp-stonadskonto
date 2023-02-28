package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;


class ParameterTest {

    @Test
    void periode_med_start_og_slutt_og_dato_utenfor_skal_ikke_overlappe() {
        var testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1), 1);

        assertThat(testPeriode.overlapper(LocalDate.of(2019, 1, 1))).isFalse();
    }


    @Test
    void periode_uten_start_og_slutt_skal_overlappe() {
        var testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, null, null, 1);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }


    @Test
    void periode_med_start_og_slutt_skal_overlappe() {
        var testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, LocalDate.of(2016, 1, 1), LocalDate.of(2018, 1, 1), 1);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    void periode_med_bare_start_skal_overlappe() {
        var testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, LocalDate.of(2016, 1, 1), null, 1);

        assertThat(testPeriode.overlapper(LocalDate.of(2017, 1, 1))).isTrue();
    }

    @Test
    void periode_med_bare_start_og_dato_før_start_skal_ikke_overlappe() {
        var testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, LocalDate.of(2016, 1, 1), null, 1);

        assertThat(testPeriode.overlapper(LocalDate.of(2015, 1, 1))).isFalse();
    }

    @Test
    void helePeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.minusDays(1), tom.plusDays(1), 1))).isTrue();
        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.plusDays(1), tom.minusDays(1), 1))).isTrue();
        assertThat(periode.overlapper(periode)).isTrue();
    }

    @Test
    void begynnelsenAvPeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.minusDays(1), tom.minusDays(1), 1))).isTrue();
        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, fom, 1))).isTrue();
        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.minusDays(1), fom, 1))).isTrue();
    }

    @Test
    void sluttenAvPeriodenOverlapper() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.plusDays(1), tom.plusDays(1), 1))).isTrue();
        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, tom, tom, 1))).isTrue();
        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, tom, tom.plusDays(1), 1))).isTrue();
    }

    @Test
    void periodenRettFørOverlapperIkke() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom.minusDays(10), fom.minusDays(1), 1))).isFalse();
    }

    @Test
    void periodenRettEtterOverlapperIkke() {
        var fom = LocalDate.now();
        var tom = fom.plusWeeks(2);
        var periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

        assertThat(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, tom.plusDays(1), tom.plusDays(5), 1))).isFalse();
    }


}
