package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.BARE_FAR_RETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype.FAR_RUNDT_FØDSEL;
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

class MinsterettTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JANUARY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.now();

    private final StønadskontoRegelOrkestrering stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

    @Test
    void wlb_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 10, 0, 0, 95, 0);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 75, 0);
    }

    @Test
    void wlb_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(2)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 10, 0, 0, 105 + 95, 0);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(4)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
    }

    @Test
    void wlb_begge_rett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_bfhr() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void wlb_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.ALENEOMSORG)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_totette_gir_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .fødselsdato(ETTER_WLB_1)
            .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 10, 110, 40, 0, 0);
    }

    @Test
    void wlb_totette_adopsjon_gir_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 40, 40, 0, 0);
    }

    @Test
    void wlb_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .fødselsdato(ETTER_WLB_1)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(50))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT_EØS)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 75, 0);
    }

    @Test
    void regeldato_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .antallBarn(2)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .antallBarn(4)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
    }

    @Test
    void regeldato_begge_rett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);

        grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1.minusMonths(1))
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void regeldato_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.ALENEOMSORG)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .regelvalgsdato(FØR_WLB)
            .fødselsdato(ETTER_WLB_1)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(50))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT_EØS)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT_EØS)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .omsorgsovertakelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
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
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnKontoerGrunnlag.Builder()
            .antallBarn(4)
            .morHarUføretrygd(true)
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 0, 0, 0, 0, 75);
    }

    @Test
    void gammel_begge_rett() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BARE_SØKER_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.ALENEOMSORG)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .morHarUføretrygd(true)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .omsorgsovertakelseDato(FØR_WLB)
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
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
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT_EØS)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.FAR)
            .fødselsdato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnKontoerGrunnlag.Builder()
            .rettighetType(BeregnKontoerGrunnlag.RettighetType.BEGGE_RETT)
            .brukerRolle(BeregnKontoerGrunnlag.BrukerRolle.MOR)
            .omsorgsovertakelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    private void fellesassert(BeregnKontoerGrunnlag grunnlag, int rundtfødsel, int tettemor, int tettefar, int bfhrMinste, int bfhrUtenAkt) {
        var stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
        var retter = stønadskontoResultat.getStønadskontoer();
        assertEntry(retter, FAR_RUNDT_FØDSEL, rundtfødsel);
        assertEntry(retter, TETTE_FØDSLER_MOR, tettemor);
        assertEntry(retter, TETTE_FØDSLER_FAR, tettefar);
        assertEntry(retter, BARE_FAR_RETT, bfhrMinste);
        assertEntry(retter, UFØREDAGER, bfhrUtenAkt);
    }

    private void assertEntry(Map<StønadskontoBeregningStønadskontotype, Integer> retter, StønadskontoBeregningStønadskontotype type, int forventet) {
        if (forventet == 0) {
            assertThat(retter).doesNotContainKey(type);
        } else {
            assertThat(retter).containsEntry(type, forventet);
        }
    }

}
