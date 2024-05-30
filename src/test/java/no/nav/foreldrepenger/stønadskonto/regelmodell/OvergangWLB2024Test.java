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
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90 + 105);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).containsEntry(FLERBARNSDAGER, 105);
        assertThat(utregnet).containsEntry(TILLEGG_FLERBARN, 105);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
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
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101 + 106);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).containsEntry(FLERBARNSDAGER, 106);
        assertThat(utregnet).containsEntry(TILLEGG_FLERBARN, 106);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FELLESPERIODE, 90);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
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
        assertThat(utregnet).containsEntry(FELLESPERIODE, 101 + 50);
        assertThat(utregnet).containsEntry(MØDREKVOTE, 95);
        assertThat(utregnet).containsEntry(TILLEGG_PREMATUR, 50);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FAR_RUNDT_FØDSEL);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 40);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 50);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 280);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(utregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        utregnet = stønadskontoResultat.getStønadskontoer();
        assertThat(utregnet).containsEntry(FORELDREPENGER, 291);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
        assertThat(utregnet).doesNotContainKey(BARE_FAR_RETT);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 95);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 95);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250 + 105);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 105);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261 + 106);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 106);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 250 + 280);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 280);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
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
        assertThat(utregnet).containsEntry(FORELDREPENGER, 261 + 288);
        assertThat(utregnet).doesNotContainKey(FELLESPERIODE);
        assertThat(utregnet).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
        assertThat(utregnet).containsEntry(BARE_FAR_RETT, 288);
        assertThat(utregnet).containsEntry(FAR_RUNDT_FØDSEL, 10);
    }

}
