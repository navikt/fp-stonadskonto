package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

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

}
