package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.BARE_FAR_RETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FAR_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TILLEGG_FLERBARN;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TILLEGG_PREMATUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Rettighetstype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.PrematurukerUtil;

class StønadskontoRegelOrkestreringTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JULY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.of(2022, Month.DECEMBER,1);
    private static final LocalDate ETTER_WLB_2 = LocalDate.of(2024, Month.DECEMBER,1);

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();


    /*
    Totale stønadskoto: 49 uker  (245 stønadsdager)
    Stønadskonto fordeler seg slik:
    - Fellesperiode: 16 uker (80 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 80)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void fødsel_far_har_rett_innenlands_annen_europeisk_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 80)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void fødsel_mor_har_rett_innenlands_annen_europeisk_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 80)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }


    @Test
    void skal_legge_til_prematurdager_på_fellesperiode() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(fødselsdato)
            .termindato(fødselsdato.plusWeeks(7).plusDays(4))
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5);
        var ekstradager = PrematurukerUtil.beregnPrematurdager(grunnlag.getFødselsdato().get(), grunnlag.getTermindato().get());
        var forventetFellesperiode = 80 + ekstradager;
        assertThat(stønadskontoer).containsEntry(FELLESPERIODE, forventetFellesperiode)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_PREMATUR, ekstradager);
    }

    @Test
    void skal_legge_til_prematurdager_på_foreldrepenger() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(fødselsdato)
            .termindato(fødselsdato.plusWeeks(8))
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        var ekstradager = PrematurukerUtil.beregnPrematurdager(grunnlag.getFødselsdato().get(), grunnlag.getTermindato().get());
        var forventetForeldrepenger = 80 + 75 + 75 + ekstradager;
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, forventetForeldrepenger).containsEntry(TILLEGG_PREMATUR, ekstradager);
    }

    @Test
    void skal_ikke_legge_til_prematurdager_på_flerbarnsdager() {
        var fødselsdato = LocalDate.of(2019, 7, 1);
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(fødselsdato)
            .termindato(fødselsdato.plusWeeks(7).plusDays(1))
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).containsEntry(FLERBARNSDAGER, 85).doesNotContainKey(TILLEGG_PREMATUR);
    }

    /*
    Totale stønadskoto: 46 uker  (230 stønadsdager)
    Stønadskonto fordeler seg slik:
    - Fellesperiode: 16 uker (80 stønadsdager)
    - Fedrekovte: 15 uker (75 stønadsdager)
    - Mødrekvote: 15 uker (75 stønadsdager)
*/
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FELLESPERIODE, 80).containsEntry(FEDREKVOTE, 75).containsEntry(MØDREKVOTE, 75);
    }

    /*
       Totale stønadskonto: - 59 uker (295 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 18 uker (90 stønadsdager)
       - Fedrekovte: 19 uker (95 stønadsdager)
       - Mødrekvote: 19 uker (95 stønadsdager)
       - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 90)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Totale stønadskonto: - 61,2 uker (306 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 20,2 uker (101 stønadsdager)
       - Fedrekovte: 19 uker (95 stønadsdager)
       - Mødrekvote: 19 uker (95 stønadsdager)
       - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 101)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }


    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FELLESPERIODE, 90).containsEntry(FEDREKVOTE, 95).containsEntry(MØDREKVOTE, 95);
    }

    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 20,11 uker (101 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FELLESPERIODE, 101).containsEntry(FEDREKVOTE, 95).containsEntry(MØDREKVOTE, 95);
    }

    /*
        Totale stønadskonto:  - 49 uker  (245 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 16 uker (80 stønadsdager) + 46 uker (230 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 80 + 230)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FLERBARNSDAGER, 230)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 230)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Totale stønadskonto:  - 46 uker  (230 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 16 uker (80 stønadsdager) (3 uker forbeholdt mor før fødsel)   + 46 uker (230 stønadsdager)
       - Fedrekovte: 15 uker (75 stønadsdager)
       - Mødrekvote: 15 uker (75 stønadsdager)
   */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 80 + 230)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FLERBARNSDAGER, 230)
            .containsEntry(TILLEGG_FLERBARN, 230);
    }

    /*
     Totale stønadskonto: - 59 uker (295 stønadsdager)
     Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 56 uker (280 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 90 + 280)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FLERBARNSDAGER, 280)
            .containsEntry(TILLEGG_FLERBARN, 280)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
     Totale stønadskonto: - 59 uker (295 stønadsdager)
     Stønadskonto fordeler seg slik:
        - Fellesperiode: 20,2 uker (101 stønadsdager) + 57,4 uker (288 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 101 + 288)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FLERBARNSDAGER, 288)
            .containsEntry(TILLEGG_FLERBARN, 288)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 56 uker (280 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 90 + 280)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 280)
            .containsEntry(TILLEGG_FLERBARN, 280);
    }

    /*
        Totale stønadskonto: - 58,2 uker (291 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 20,2 uker (101 stønadsdager) + 57,4 uker (288 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 101 + 288)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 288)
            .containsEntry(TILLEGG_FLERBARN, 288);
    }

    /*
        Totale stønadskonto:  - 49 uker  (245 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 16 uker (80 stønadsdager) + 17 uker (85 stønadsdager)
        - Fedrekovte: 15 uker (75 stønadsdager)
        - Mødrekvote: 15 uker (75 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 80 + 85)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FLERBARNSDAGER, 85)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 85)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Totale stønadskonto:  - 46 uker  (230 stønadsdager)
       Stønadskonto fordeler seg slik:
       - Fellesperiode: 16 uker (80 stønadsdager) (3 uker forbeholdt mor før fødsel)   + 17 uker (85 stønadsdager)
       - Fedrekovte: 15 uker (75 stønadsdager)
       - Mødrekvote: 15 uker (75 stønadsdager)
   */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 80 + 85)
            .containsEntry(FEDREKVOTE, 75)
            .containsEntry(MØDREKVOTE, 75)
            .containsEntry(FLERBARNSDAGER, 85)
            .containsEntry(TILLEGG_FLERBARN, 85);
    }

    /*
    Totale stønadskonto: - 59 uker (295 stønadsdager)
     Stønadskonto fordeler seg slik:
    - Fellesperiode: 18 uker (90 stønadsdager) + 21 uker (105 stønadsdager)
    - Fedrekovte: 19 uker (95 stønadsdager)
    - Mødrekvote: 19 uker (95 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 90 + 105)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FLERBARNSDAGER, 105)
            .containsEntry(TILLEGG_FLERBARN, 105)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Totale stønadskonto: - 61,2 uker (306 stønadsdager)
     Stønadskonto fordeler seg slik:
    - Fellesperiode: 20,2 uker (101 stønadsdager) + 21,2 uker (106 stønadsdager)
    - Fedrekovte: 19 uker (95 stønadsdager)
    - Mødrekvote: 19 uker (95 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(7)
            .containsEntry(FELLESPERIODE, 101 + 106)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(FLERBARNSDAGER, 106)
            .containsEntry(TILLEGG_FLERBARN, 106)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 21 uker (105 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 90 + 105)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 105)
            .containsEntry(TILLEGG_FLERBARN, 105);
    }

    /*
        Totale stønadskonto: - 58,2 uker (291 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 20,2 uker (101 stønadsdager) + 21,2 uker (106 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(5)
            .containsEntry(FELLESPERIODE, 101 + 106)
            .containsEntry(FEDREKVOTE, 95)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 106)
            .containsEntry(TILLEGG_FLERBARN, 106);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 46 uker (230 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 80 + 75 + 75).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15);
    }

    /*
    Bare mor har rett til foreldrepenger.
    Foreldrepenger: - 46 uker (230 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(1).containsEntry(FORELDREPENGER, 80 + 75 + 75);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 56 uker (280 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 280).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 58,2 uker (291 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 291).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15);
    }

    /*
    Bare mor har rett til foreldrepenger.
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(1).containsEntry(FORELDREPENGER, 280);
    }

    /*
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 58,2 uker (291 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(1).containsEntry(FORELDREPENGER, 291);
    }

    /*
       Bare mor har rett til foreldrepenger. 2 barn
       Foreldrepenger: 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
       Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 80 + 85 + 75 + 75).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 85);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: - 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 80 + 85 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 85);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 280 + 105).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 105);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: 58,2 uker (291 stønadsdager) + 21,2 uker (106 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 291 + 106).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 106);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 280 + 105)
            .containsEntry(TILLEGG_FLERBARN, 105);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: 8,2 uker (291 stønadsdager) + 21,2 uker (106 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 291 + 106)
            .containsEntry(TILLEGG_FLERBARN, 106);
    }

    /*
       Bare mor har rett til foreldrepenger. 3 barn
       Foreldrepenger: 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
       Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
   */
    @Test
    void fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 80 + 230 + 75 + 75).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 230);
    }

    /*
        Mor har aleneomsorg og rett til foreldrepenger. 3 barn
        Foreldrepenger: - 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 80 + 230 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 230);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 280 + 280).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 280);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 58,2 uker (291 stønadsdager) + 57,6 uker (288 stønadsdager)
    Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 291 + 288).containsEntry(FORELDREPENGER_FØR_FØDSEL, 15)
            .containsEntry(TILLEGG_FLERBARN, 288);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 280 + 280).containsEntry(TILLEGG_FLERBARN, 280);
    }

    /*
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 58,2 uker (291 stønadsdager) + 57,6 uker (288 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .omsorgsovertakelseDato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 291 + 288).containsEntry(TILLEGG_FLERBARN, 288);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger
    Foreldrepenger: 46 uker (230 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 80 + 75 + 75).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til  Foreldrepenger
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 280).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til  Foreldrepenger
    Foreldrepenger: 58,2 uker (291 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(2).containsEntry(FORELDREPENGER, 291).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 46 uker (230 stønadsdager) + 46 uker (230 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 80 + 230 + 75 + 75).containsEntry(TILLEGG_FLERBARN, 230).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 280 + 280).containsEntry(TILLEGG_FLERBARN, 280).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 58,2 uker (291 stønadsdager) + 57,6 uker (288 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 291 + 288).containsEntry(TILLEGG_FLERBARN, 288).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 46 uker (230 stønadsdager) + 17 uker (85 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 80 + 85 + 75 + 75).containsEntry(TILLEGG_FLERBARN, 85).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 280 + 105).containsEntry(TILLEGG_FLERBARN, 105).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 58,2 uker (291 stønadsdager) + 21,2 uker (106 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 291 + 106).containsEntry(TILLEGG_FLERBARN, 106).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad.
       Foreldrepenger: 40 uker (200 stønadsdager) med 8 uker minsterett
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 50 + 75 + 75).containsEntry(BARE_FAR_RETT, 40).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad.
       Foreldrepenger: 40 uker (200 stønadsdager) med 10 uker minsterett
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 50 + 75 + 75).containsEntry(BARE_FAR_RETT, 50).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 80% dekningsgrad.
       Foreldrepenger: 50 uker (250 stønadsdager) med 8 uker minsterett
   */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 250).containsEntry(BARE_FAR_RETT, 40).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 80% dekningsgrad.
       Foreldrepenger: 52,2 uker (261 stønadsdager) med 10 uker minsterett
   */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 261).containsEntry(BARE_FAR_RETT, 50).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad. 3 barn.
       Foreldrepenger: 40 uker (200 stønadsdager) + 46 uker (230 stønadsdager)
       Flerbarnsdager: 46 uker (230 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(FØR_WLB)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 50 + 230 + 75 + 75).containsEntry(FLERBARNSDAGER, 230).containsEntry(TILLEGG_FLERBARN, 230);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad. 3 barn.
       Foreldrepenger: 40 uker (200 stønadsdager) + 46 uker (230 stønadsdager)
       Flerbarnsdager: 46 uker (230 stønadsdager)
       Minsterett: 10 uker
    */
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .termindato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4).containsEntry(FORELDREPENGER, 50 + 230 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 230)
            .containsEntry(BARE_FAR_RETT, 230)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 3 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 56 uker (280 stønadsdager)
    Flerbarnsdager: 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(FØR_WLB)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 100 + 280 + 75 + 75).containsEntry(FLERBARNSDAGER, 280).containsEntry(TILLEGG_FLERBARN, 280);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 3 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 56 uker (280 stønadsdager)
    Minsterett: 56 uker (280 stønadsdager)
    */
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4)
            .containsEntry(FORELDREPENGER, 100 + 280 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 280)
            .containsEntry(BARE_FAR_RETT, 280)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 3 barn.
    Foreldrepenger: 52,2 uker (261 stønadsdager) + 57,6 uker (288 stønadsdager)
    Flerbarnsdager: 57,6 uker (288 stønadsdager)
    Minsterett: 57,6 uker (288 stønadsdager)
    */
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(3)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4)
            .containsEntry(FORELDREPENGER, 261 + 288)
            .containsEntry(TILLEGG_FLERBARN, 288)
            .containsEntry(BARE_FAR_RETT, 288)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
       Far har rett til Foreldrepenger. 100% dekningsgrad. 2 barn.
       Foreldrepenger: 40 uker (200 stønadsdager) + 17 uker (85 stønadsdager)
       Flerbarnsdager: 17 uker (85 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(FØR_WLB)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 50 + 85 + 75 + 75)
            .containsEntry(FLERBARNSDAGER, 85)
            .containsEntry(TILLEGG_FLERBARN, 85);
    }

    /*
   Far har rett til Foreldrepenger. 100% dekningsgrad. 2 barn.
   Foreldrepenger: 40 uker (200 stønadsdager) + 17 uker (85 stønadsdager)
   Minsterett: 17 uker (85 stønadsdager)
*/
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4)
            .containsEntry(FORELDREPENGER, 50 + 85 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 85)
            .containsEntry(BARE_FAR_RETT, 85)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 2 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 21 uker (105 stønadsdager)
    Flerbarnsdager: 21 uker (105 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(FØR_WLB)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(3).containsEntry(FORELDREPENGER, 100 + 105 + 75 + 75).containsEntry(FLERBARNSDAGER, 105).containsEntry(TILLEGG_FLERBARN, 105);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 2 barn.
    Foreldrepenger: 50 uker (250 stønadsdager) + 21 uker (105 stønadsdager)
    Minsterett: 21 uker (105 stønadsdager)
    */
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn_før_utvidelse() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_1)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4)
            .containsEntry(FORELDREPENGER, 100 + 105 + 75 + 75)
            .containsEntry(TILLEGG_FLERBARN, 105)
            .containsEntry(BARE_FAR_RETT, 105)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    /*
    Far har rett til Foreldrepenger. 80% dekningsgrad. 2 barn.
    Foreldrepenger: 52,2 uker (261 stønadsdager) + 21,2 uker (106 stønadsdager)
    Minsterett: 21,2 uker (106 stønadsdager)
    */
    @Test
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).hasSize(4)
            .containsEntry(FORELDREPENGER, 261 + 106)
            .containsEntry(TILLEGG_FLERBARN, 106)
            .containsEntry(BARE_FAR_RETT, 106)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @SuppressWarnings("unchecked")
    @Test
    void bergegn_kontoer_regel_skal_produsere_sporing_med_json() throws IOException {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);

        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var input = mapper.readValue(stønadskontoResultat.getInnsendtGrunnlag(), BeregnKontoerGrunnlag.class);
        assertThat(input.getRettighetstype()).isEqualTo(Rettighetstype.BEGGE_RETT);
        assertThat(input.getDekningsgrad()).isEqualTo(Dekningsgrad.DEKNINGSGRAD_100);
        assertThat(input.getAntallBarn()).isEqualTo(1);
        assertThat(input.erFødsel()).isTrue();
        assertThat(input.getFamilieHendelseDato()).isEqualTo(ETTER_WLB_2);
        assertThat(input.getFødselsdato()).isPresent();
        assertThat(input.getTidligereUtregning()).isEmpty();
        assertThat(input.getKonfigurasjonsvalgdato()).isEqualTo(ETTER_WLB_2);

        assertThat(stønadskontoResultat.getRegelVersjon()).isEqualTo(StønadskontoVersion.STØNADSKONTO_VERSION.version());

        assertThat(mapper.readValue(stønadskontoResultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
    }

    @Test
    void skal_bruke_omsorgsovertakelse_og_ikke_fødselsdato_som_konfig_parameter() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            //Så tidlig at det vil skape exception hvis bruk ettersom vi ikke har noe konfig for 2016
            .fødselsdato(LocalDate.of(2016, 1, 1))
            .omsorgsovertakelseDato(LocalDate.of(2020, 2, 10))
            .antallBarn(1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        assertThatCode(() -> stønadskontoRegelOrkestrering.beregnKontoer(grunnlag)).doesNotThrowAnyException();
    }

    @Test
    void bfhr_flere_barn_ikke_minsterett_skal_gi_flerbarnsdager() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(FØR_WLB)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).containsKey(FLERBARNSDAGER);
    }


    @Test
    void bfhr_flere_barn_minsterett_skal_ikke_gi_flerbarnsdager() {
        var grunnlag = BeregnKontoerGrunnlag.builder()
            .fødselsdato(ETTER_WLB_2)
            .antallBarn(2)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)

            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();

        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var stønadskontoer = stønadskontoResultat.getStønadskontoer();
        assertThat(stønadskontoer).doesNotContainKey(FLERBARNSDAGER);
    }

}
