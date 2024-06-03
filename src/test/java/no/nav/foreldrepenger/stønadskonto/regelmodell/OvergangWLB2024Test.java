package no.nav.foreldrepenger.stønadskonto.regelmodell;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Rettighetstype;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

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
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TILLEGG_PREMATUR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.UFØREDAGER;
import static org.assertj.core.api.Assertions.assertThat;

class OvergangWLB2024Test {

    private static final LocalDate ETTER_WLB_1 = LocalDate.of(2022, Month.DECEMBER,1);
    private static final LocalDate ETTER_WLB_2 = LocalDate.of(2024, Month.DECEMBER,1);

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

    @Test
    void overgang_wlb_2024_dekning_80_prosent_begge_rett() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90)
            .containsEntry(MØDREKVOTE, 95)
            .doesNotContainKey(FORELDREPENGER);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101)
            .containsEntry(MØDREKVOTE, 95)
            .doesNotContainKey(FORELDREPENGER)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_begge_rett_flerbarn() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .antallBarn(2)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90 + 105)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 105)
            .containsEntry(TILLEGG_FLERBARN, 105)
            .doesNotContainKey(FORELDREPENGER);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .antallBarn(2)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101 + 106)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(FLERBARNSDAGER, 106)
            .containsEntry(TILLEGG_FLERBARN, 106)
            .doesNotContainKey(FORELDREPENGER)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_begge_rett_prematur() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2.plusWeeks(10))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90)
            .containsEntry(MØDREKVOTE, 95)
            .doesNotContainKey(FORELDREPENGER);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .termindato(ETTER_WLB_2.plusWeeks(10))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101 + 50)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(TILLEGG_PREMATUR, 50)
            .doesNotContainKey(FORELDREPENGER)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_mor_rett() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FAR_RUNDT_FØDSEL);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_mor_alene() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291)
            .doesNotContainKey(FELLESPERIODE);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_far_rett() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250)
            .containsEntry(BARE_FAR_RETT, 40)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 50)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_far_alene() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .doesNotContainKey(BARE_FAR_RETT);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_far_rett_uføre() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .morHarUføretrygd(true)
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250)
            .containsEntry(BARE_FAR_RETT, 95)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .morHarUføretrygd(true)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 95)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_far_rett_flerbarn() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .antallBarn(2)
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250 + 105)
            .containsEntry(BARE_FAR_RETT, 105)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .antallBarn(2)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261 + 106)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 106)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_far_rett_trebarn() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .antallBarn(3)
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250 + 280)
            .containsEntry(BARE_FAR_RETT, 280)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .antallBarn(3)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261 + 288)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 288)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }


    @Test
    void overgang_wlb_2024_bare_far_rett() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 200)
            .containsEntry(BARE_FAR_RETT, 40)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 200)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 50)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }


    @Test
    void overgang_wlb_2024_bare_far_rett_uføre() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .termindato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .morHarUføretrygd(true)
            .tidligereUtregning(Map.of())
            .build();
        var utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 200)
            .containsEntry(BARE_FAR_RETT, 75)
            .doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .morHarUføretrygd(true)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 200)
            .doesNotContainKey(FELLESPERIODE)
            .doesNotContainKey(FORELDREPENGER_FØR_FØDSEL)
            .containsEntry(BARE_FAR_RETT, 75)
            .containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

}
