package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.BARE_FAR_RETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FAR_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TETTE_SAKER_FAR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TETTE_SAKER_MOR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TILLEGG_FLERBARN;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.UFØREDAGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Rettighetstype;

class StønadskontoEldreKonfigurasjonerTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JULY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.of(2022, Month.DECEMBER,1);

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

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
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80() {
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
     Totale stønadskonto: - 59 uker (295 stønadsdager)
     Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 56 uker (280 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
        - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
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
        Totale stønadskonto: - 56 uker (280 stønadsdager)
         Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 56 uker (280 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn() {
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
    Totale stønadskonto: - 59 uker (295 stønadsdager)
     Stønadskonto fordeler seg slik:
    - Fellesperiode: 18 uker (90 stønadsdager) + 21 uker (105 stønadsdager)
    - Fedrekovte: 19 uker (95 stønadsdager)
    - Mødrekvote: 19 uker (95 stønadsdager)
    - Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
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
        Totale stønadskonto: - 56 uker (280 stønadsdager)
        Stønadskonto fordeler seg slik:
        - Fellesperiode: 18 uker (90 stønadsdager) + 21 uker (105 stønadsdager)
        - Fedrekovte: 19 uker (95 stønadsdager)
        - Mødrekvote: 19 uker (95 stønadsdager)
    */
    @Test
    void adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn() {
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
        Bare mor har rett til foreldrepenger.
        Foreldrepenger: 56 uker (280 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_80() {
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
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_80() {
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
        Mor har aleneomsorg og rett til foreldrepenger. 2 barn
        Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
        Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_2_barn() {
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
        Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
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
    Mor har aleneomsorg og rett til foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    Foreldrepenger før fødsel: 3 uker (15 stønadsdager)
    */
    @Test
    void fødsel_bare_mor_rett_dekningsgrad_80_3_barn() {
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
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
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
    Far har aleneomsorg og rett til  Foreldrepenger
    Foreldrepenger: 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80() {
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
    Far har aleneomsorg og rett til Foreldrepenger. 3 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 56 uker (280 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn() {
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
    Far har aleneomsorg og rett til Foreldrepenger. 2 barn
    Foreldrepenger: 56 uker (280 stønadsdager) + 21 uker (105 stønadsdager)
    */
    @Test
    void fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn() {
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
       Far har rett til Foreldrepenger. 100% dekningsgrad.
       Foreldrepenger: 40 uker (200 stønadsdager) med 8 uker minsterett
    */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100() {
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
       Far har rett til Foreldrepenger. 80% dekningsgrad.
       Foreldrepenger: 50 uker (250 stønadsdager) med 8 uker minsterett
   */
    @Test
    void fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80() {
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
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn() {
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
    void wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn() {
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
    void wlb_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(2)
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 10, 0, 0, 105 + 95, 0);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(4)
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
    }

    @Test
    void wlb_begge_rett_før_utvidelse() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .brukerRolle(Brukerrolle.FAR)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }
    @Test
    void wlb_bfhr_før_utvidelse() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void regeldato_bfhr() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);

        grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1.minusMonths(1))
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void gammel_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 0, 0, 0, 0, 75);
    }

    @Test
    void gammel_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(2)
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(4)
            .morHarUføretrygd(true)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 0, 0, 0, 0, 75);
    }

    @Test
    void gammel_begge_rett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .omsorgsovertakelseDato(FØR_WLB)
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(FØR_WLB)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(50))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .omsorgsovertakelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    private void fellesassert(BeregnKontoerGrunnlag grunnlag, int rundtfødsel, int tettemor, int tettefar, int bfhrMinste, int bfhrUtenAkt) {
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertEntry(retter, FAR_RUNDT_FØDSEL, rundtfødsel);
        assertEntry(retter, TETTE_SAKER_MOR, tettemor);
        assertEntry(retter, TETTE_SAKER_FAR, tettefar);
        assertEntry(retter, BARE_FAR_RETT, bfhrMinste);
        assertEntry(retter, UFØREDAGER, bfhrUtenAkt);
    }

    private void assertEntry(Map<StønadskontoKontotype, Integer> retter, StønadskontoKontotype type, int forventet) {
        if (forventet == 0) {
            assertThat(retter).doesNotContainKey(type);
        } else {
            assertThat(retter).containsEntry(type, forventet);
        }
    }

}
