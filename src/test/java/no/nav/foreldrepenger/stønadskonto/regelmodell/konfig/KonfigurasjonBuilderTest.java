package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

class KonfigurasjonBuilderTest {

    @Test
    void konfigurasjon_med_en_verdi() {
        var nå = LocalDate.now();
        var konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå, null, 75)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå)).isEqualTo(75);
    }

    @Test
    void konfigurasjon_med_en_verdi_i_to_intervaller() {
        var nå = LocalDate.now();
        var konfigurasjon = KonfigurasjonBuilder.create()
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå, nå.plusDays(6), 50)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100,  nå.plusDays(7), null, 75)
                .build();
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå)).isEqualTo(50);
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå.plusDays(7))).isEqualTo(75);
        assertThat(konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå.plusDays(70))).isEqualTo(75);
    }

    @Test
    void konfigurasjon_med_en_verdi_i_to_intervaller_med_overlapp() {
        var nå = LocalDate.now();
        var nåPLuss5 = nå.plusDays(5);
        var initKonfigBuilder = KonfigurasjonBuilder.create()
            .leggTilParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nå, nå.plusDays(6), 50);
        assertThrows(IllegalArgumentException.class, () -> initKonfigBuilder
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, nåPLuss5, null, 75));
    }

}
