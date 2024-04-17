package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.BARE_FAR_RETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FAR_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_FAR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_MOR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.UFØREDAGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

class TidligereUtregningTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JANUARY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.now();

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

    @Test
    void bruk_endelt_input_hvis_tredelt_utregning() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 230))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FORELDREPENGER, 230);
        assertThat(retter).doesNotContainKey(FELLESPERIODE);
    }

    @Test
    void bruk_tredelt_input_hvis_endelt_utregning() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.ALENEOMSORG)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FELLESPERIODE, 80);
        assertThat(retter).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_utregning_dersom_input_uten_prematur() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .termindato(ETTER_WLB_1.plusWeeks(8))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FELLESPERIODE, 120);
        assertThat(retter).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_input_dersom_utregning_mangler_prematur() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .termindato(ETTER_WLB_1.plusWeeks(8))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 120, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FELLESPERIODE, 120);
        assertThat(retter).containsEntry(FAR_RUNDT_FØDSEL, 10);
        assertThat(retter).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_utregning_for_80_dersom_input_fra_100() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FELLESPERIODE, 90);
        assertThat(retter).containsEntry(MØDREKVOTE, 95);
        assertThat(retter).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bfhr_gammel_tilkommet_uføre() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 200))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FORELDREPENGER, 200);
        assertThat(retter).containsEntry(UFØREDAGER, 75);
        assertThat(retter).doesNotContainKey(FELLESPERIODE);
        assertThat(retter).doesNotContainKey(BARE_FAR_RETT);
    }

    @Test
    void bfhr_wlb_tilkommet_uføre() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 200))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FORELDREPENGER, 200);
        assertThat(retter).containsEntry(BARE_FAR_RETT, 75);
        assertThat(retter).doesNotContainKey(FELLESPERIODE);
        assertThat(retter).doesNotContainKey(UFØREDAGER);
    }

    @Test
    void begge_wlb_tilkommet_tette() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1.minusWeeks(40))
            .familieHendelseDatoNesteSak(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FELLESPERIODE, 90);
        assertThat(retter).containsEntry(MØDREKVOTE, 95);
        assertThat(retter).containsEntry(TETTE_FØDSLER_MOR, 110);
        assertThat(retter).containsEntry(TETTE_FØDSLER_FAR, 40);
        assertThat(retter).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bmht_wlb_adopsjon_tilkommet_tette() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .omsorgsovertakelseDato(ETTER_WLB_1.minusWeeks(40))
            .familieHendelseDatoNesteSak(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(FORELDREPENGER, 280))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertThat(retter).containsEntry(FORELDREPENGER, 280);
        assertThat(retter).containsEntry(TETTE_FØDSLER_MOR, 40);
        assertThat(retter).doesNotContainKey(TETTE_FØDSLER_FAR);
        assertThat(retter).doesNotContainKey(FELLESPERIODE);
        assertThat(retter).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
    }

}
