package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.BARE_FAR_GENERELL_MINSTERETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.BARE_FAR_UTEN_AKTIVITETSKRAV;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.FAR_UTTAK_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.TETTE_FØDSLER_FAR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.TETTE_FØDSLER_MOR;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.finnMinsterett;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

class MinsterettTest {

    private static final LocalDate FØR_WLB = LocalDate.of(2022, Month.JANUARY, 1);
    private static final LocalDate ETTER_WLB_1 = LocalDate.now();

    @Test
    void wlb_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        fellesassert(grunnlag80, 10, 0, 0, 95, 0);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        fellesassert(grunnlag100, 10, 0, 0, 75, 0);
    }

    @Test
    void wlb_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
                .antallBarn(2)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        fellesassert(grunnlag80, 10, 0, 0, 105 + 95, 0);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
                .antallBarn(4)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
    }

    @Test
    void wlb_begge_rett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(false)
                .aleneomsorg(false)
                .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_bfhr() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(true)
                .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void wlb_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .aleneomsorg(true)
                .bareFarHarRett(true)
                .morHarUføretrygd(true)
                .familieHendelseDato(ETTER_WLB_1)
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_totette_gir_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .gjelderFødsel(true)
                .familieHendelseDato(ETTER_WLB_1)
                .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(40))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        fellesassert(grunnlag, 10, 110, 40, 0, 0);
    }

    @Test
    void wlb_totette_adopsjon_gir_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .gjelderFødsel(false)
            .familieHendelseDato(ETTER_WLB_1)
            .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 40, 40, 0, 0);
    }

    @Test
    void wlb_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .familieHendelseDato(ETTER_WLB_1)
                //Barna ikke tett nok til minsteretten
                .familieHendelseDatoNesteSak(ETTER_WLB_1.plusWeeks(50))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .bareFarHarRett(false)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 0, 0);
    }

    @Test
    void wlb_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .familieHendelseDato(ETTER_WLB_1)
            .gjelderFødsel(false)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 75, 0);
    }

    @Test
    void regeldato_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .antallBarn(2)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1)
            .antallBarn(4)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
    }

    @Test
    void regeldato_begge_rett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(true)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);

        grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(ETTER_WLB_1.minusMonths(1))
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(true)
            .build();
        fellesassert(grunnlag, 10, 0, 0, 40, 0);
    }

    @Test
    void regeldato_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .aleneomsorg(true)
            .bareFarHarRett(true)
            .morHarUføretrygd(true)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .gjelderFødsel(false)
            .familieHendelseDato(ETTER_WLB_1)
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .familieHendelseDato(ETTER_WLB_1)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(50))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .bareFarHarRett(false)
            .familieHendelseDato(ETTER_WLB_1)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void regeldato_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .regelvalgsdato(FØR_WLB)
            .familieHendelseDato(ETTER_WLB_1)
            .gjelderFødsel(false)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr_mor_ufør() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 0, 0, 0, 0, 75);
    }

    @Test
    void gammel_bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
            .antallBarn(2)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag80, 0, 0, 0, 0, 95);

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
            .antallBarn(4)
            .morHarUføretrygd(true)
            .bareFarHarRett(true)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag100, 0, 0, 0, 0, 75);
    }

    @Test
    void gammel_begge_rett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(true)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .aleneomsorg(true)
            .bareFarHarRett(true)
            .morHarUføretrygd(true)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .gjelderFødsel(false)
            .familieHendelseDato(FØR_WLB)
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(40))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .familieHendelseDato(FØR_WLB)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(FØR_WLB.plusWeeks(50))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .bareFarHarRett(false)
            .familieHendelseDato(FØR_WLB)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    @Test
    void gammel_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .familieHendelseDato(FØR_WLB)
            .gjelderFødsel(false)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        fellesassert(grunnlag, 0, 0, 0, 0, 0);
    }

    private void fellesassert(BeregnMinsterettGrunnlag grunnlag, int rundtfødsel, int tettemor, int tettefar, int bfhrMinste, int bfhrUtenAkt) {
        var retter = finnMinsterett(grunnlag);
        assertThat(retter).containsEntry(FAR_UTTAK_RUNDT_FØDSEL, rundtfødsel);
        assertThat(retter).containsEntry(TETTE_FØDSLER_MOR, tettemor);
        assertThat(retter).containsEntry(TETTE_FØDSLER_FAR, tettefar);
        assertThat(retter).containsEntry(BARE_FAR_GENERELL_MINSTERETT, bfhrMinste);
        assertThat(retter).containsEntry(BARE_FAR_UTEN_AKTIVITETSKRAV, bfhrUtenAkt);

    }

}
