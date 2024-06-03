package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.BARE_FAR_RETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FAR_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TETTE_SAKER_FAR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype.TETTE_SAKER_MOR;
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

class TidligereUtregningTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JANUARY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.of(2022, Month.DECEMBER,1);
    private static final LocalDate ETTER_WLB_2 = LocalDate.of(2024, Month.DECEMBER,1);

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

    @Test
    void bruk_endelt_input_hvis_tredelt_utregning() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 230))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FORELDREPENGER, 230).doesNotContainKey(FELLESPERIODE);
    }

    @Test
    void bruk_tredelt_input_hvis_endelt_utregning() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.ALENEOMSORG)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 80).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_utregning_dersom_input_uten_prematur() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .termindato(ETTER_WLB_1.plusWeeks(8))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 120).containsEntry(StønadskontoKontotype.TILLEGG_PREMATUR, 40).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_input_dersom_utregning_mangler_prematur() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .termindato(ETTER_WLB_1.plusWeeks(8))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 120, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 120).containsEntry(FAR_RUNDT_FØDSEL, 10).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bruk_utregning_for_80_dersom_input_fra_100() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 90).containsEntry(MØDREKVOTE, 95).doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bfhr_gammel_tilkommet_uføre() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 200))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FORELDREPENGER, 200).containsEntry(UFØREDAGER, 75).doesNotContainKey(FELLESPERIODE).doesNotContainKey(BARE_FAR_RETT);

    }

    @Test
    void bfhr_wlb_tilkommet_uføre() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .tidligereUtregning(Map.of(FORELDREPENGER, 200))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FORELDREPENGER, 200).containsEntry(BARE_FAR_RETT, 75).doesNotContainKey(FELLESPERIODE).doesNotContainKey(UFØREDAGER);
    }

    @Test
    void begge_wlb_tilkommet_tette_redusert_dekningsgrad() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2.minusWeeks(40))
            .familieHendelseDatoNesteSak(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(MØDREKVOTE, 75, FEDREKVOTE, 75, FELLESPERIODE, 80, FORELDREPENGER_FØR_FØDSEL, 15))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 90)
            .containsEntry(MØDREKVOTE, 95)
            .containsEntry(TETTE_SAKER_MOR, 110)
            .containsEntry(TETTE_SAKER_FAR, 40)
            .doesNotContainKey(FORELDREPENGER);
    }

    @Test
    void bmht_wlb_adopsjon_tilkommet_tette() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BARE_SØKER_RETT)
            .brukerRolle(Brukerrolle.MOR)
            .omsorgsovertakelseDato(ETTER_WLB_2.minusWeeks(40))
            .familieHendelseDatoNesteSak(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of(FORELDREPENGER, 280))
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FORELDREPENGER, 280).containsEntry(TETTE_SAKER_MOR, 40)
            .doesNotContainKey(TETTE_SAKER_FAR).doesNotContainKey(FELLESPERIODE).doesNotContainKey(FORELDREPENGER_FØR_FØDSEL);
    }

    @Test
    void overgang_wlb_2024_dekning_80_prosent_begge_rett() {
        var tidligere = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(Map.of())
            .build();
        var tidligereUtregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
        assertThat(tidligereUtregnet).containsEntry(FELLESPERIODE, 90).containsEntry(MØDREKVOTE, 95).doesNotContainKey(FORELDREPENGER);
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(Rettighetstype.BEGGE_RETT)
            .brukerRolle(Brukerrolle.FAR)
            .fødselsdato(ETTER_WLB_2)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .tidligereUtregning(tidligereUtregnet)
            .build();
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retterMax = stønadskontoResultat.getStønadskontoer();
        assertThat(retterMax).containsEntry(FELLESPERIODE, 101).containsEntry(MØDREKVOTE, 95).doesNotContainKey(FORELDREPENGER);
    }

}
