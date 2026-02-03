package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import no.nav.fpsak.nare.json.JsonOutput;

class LegacyGrunnlagTest {

    private static final String V2022 = """
        {
          "regelvalgsdato" : null,
          "antallBarn" : 1,
          "morRett" : true,
          "farRett" : true,
          "dekningsgrad" : "DEKNINGSGRAD_80",
          "farAleneomsorg" : false,
          "morAleneomsorg" : false,
          "farHarRettEØS" : false,
          "morHarRettEØS" : false,
          "fødselsdato" : null,
          "termindato" : "2024-05-04",
          "omsorgsovertakelseDato" : null,
          "minsterett" : true
        }
        """;

    @Test
    void skal_lese_wlb_grunnlag() {
        var input = JsonOutput.fromJson(V2022, LegacyGrunnlagV1.class);
        assertThat(input.isMorRett()).isTrue();
        assertThat(input.isFarRett()).isTrue();
        assertThat(input.isFarAleneomsorg()).isFalse();
        assertThat(input.isMorAleneomsorg()).isFalse();
        assertThat(input.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_80);
        assertThat(input.getAntallBarn()).isEqualTo(1);
        assertThat(input.erFødsel()).isTrue();
        assertThat(input.getFamiliehendelsesdato()).isEqualTo(LocalDate.parse("2024-05-04", DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(input.getTermindato()).isPresent();
        assertThat(input.isMinsterett()).isTrue();
    }

    private static final String V2019 = """
        {
          "antallBarn" : 1,
          "morRett" : true,
          "farRett" : true,
          "dekningsgrad" : "DEKNINGSGRAD_100",
          "farAleneomsorg" : false,
          "morAleneomsorg" : false,
          "fødselsdato" : null,
          "termindato" : "2019-07-26",
          "omsorgsovertakelseDato" : null
        }
        """;

    @Test
    void skal_lese_2019_grunnlag() {
        var input = JsonOutput.fromJson(V2019, LegacyGrunnlagV1.class);
        assertThat(input.isMorRett()).isTrue();
        assertThat(input.isFarRett()).isTrue();
        assertThat(input.isFarAleneomsorg()).isFalse();
        assertThat(input.isMorAleneomsorg()).isFalse();
        assertThat(input.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_100);
        assertThat(input.getAntallBarn()).isEqualTo(1);
        assertThat(input.erFødsel()).isTrue();
        assertThat(input.getFamiliehendelsesdato()).isEqualTo(LocalDate.parse("2019-07-26", DateTimeFormatter.ISO_LOCAL_DATE));
        assertThat(input.getTermindato()).isPresent();
        assertThat(input.isMinsterett()).isFalse();
    }


    private static final String V2018 = """
        {
          "erFødsel" : true,
          "antallBarn" : 1,
          "morRett" : true,
          "farRett" : true,
          "dekningsgrad" : "DEKNINGSGRAD_100",
          "farAleneomsorg" : false,
          "morAleneomsorg" : false,
          "familiehendelsesdato" : "2019-01-22"
        }
        """;

    @Test
    void skal_lese_2018_grunnlag() {

        var input = JsonOutput.fromJson(V2018, LegacyGrunnlagV0.class);
        assertThat(input.isMorRett()).isTrue();
        assertThat(input.isFarRett()).isTrue();
        assertThat(input.isFarAleneomsorg()).isFalse();
        assertThat(input.isMorAleneomsorg()).isFalse();
        assertThat(input.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_100);
        assertThat(input.getAntallBarn()).isEqualTo(1);
        assertThat(input.erFødsel()).isTrue();
        assertThat(input.getFamiliehendelsesdato()).isEqualTo(LocalDate.parse("2019-01-22", DateTimeFormatter.ISO_LOCAL_DATE));
    }


}
