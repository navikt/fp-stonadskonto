package no.nav.foreldrepenger.stønadskonto.regelmodell;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.FAR_UTTAK_RUNDT_FØDSEL;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.GENERELL_MINSTERETT;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.TETTE_FØDSLER;
import static no.nav.foreldrepenger.stønadskonto.regelmodell.Minsterett.finnMinsterett;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

class MinsterettTest {

    @Test
    void bfhr_mor_ufør() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag80)).containsEntry(GENERELL_MINSTERETT,
            Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_80, LocalDate.now()));

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag100)).containsEntry(GENERELL_MINSTERETT,
                Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_100, LocalDate.now()));
    }

    @Test
    void bfhr_mor_ufør_flerbarn() {
        var grunnlag80 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .antallBarn(2)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag80)).containsEntry(GENERELL_MINSTERETT,
                Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN, Dekningsgrad.DEKNINGSGRAD_80, LocalDate.now())
        + Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_80, LocalDate.now()));

        var grunnlag100 = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .antallBarn(4)
                .morHarUføretrygd(true)
                .bareFarHarRett(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag100)).containsEntry(GENERELL_MINSTERETT,
                Konfigurasjon.STANDARD.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, Dekningsgrad.DEKNINGSGRAD_100, LocalDate.now())
        + Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_100, LocalDate.now()));
    }

    @Test
    void begge_rett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(false)
                .aleneomsorg(false)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(FAR_UTTAK_RUNDT_FØDSEL,
                Konfigurasjon.STANDARD.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, null, LocalDate.now()));
    }

    @Test
    void bfhr() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .bareFarHarRett(true)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(GENERELL_MINSTERETT,
                Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, LocalDate.now()));
    }

    @Test
    void uten_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(false)
                .mor(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag)).isEmpty();
    }

    @Test
    void både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(false)
                .aleneomsorg(true)
                .bareFarHarRett(true)
                .morHarUføretrygd(true)
                .familieHendelseDato(LocalDate.now())
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .build();
        assertThat(finnMinsterett(grunnlag)).doesNotContainKey(GENERELL_MINSTERETT);
    }

    @Test
    void totette_gir_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .gjelderFødsel(true)
                .familieHendelseDato(LocalDate.now())
                .familieHendelseDatoNesteSak(LocalDate.now().plusWeeks(40))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(TETTE_FØDSLER,
                Konfigurasjon.STANDARD.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, null, LocalDate.now()));
    }

    @Test
    void ikke_totette_gir_ikke_dager() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
                .minsterett(true)
                .mor(true)
                .familieHendelseDato(LocalDate.now())
                //Barna ikke tett nok til minsteretten
                .familieHendelseDatoNesteSak(LocalDate.now().plusWeeks(50))
                .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(TETTE_FØDSLER, 0);
    }

    @Test
    void før_totette_gir_ikke_dager() {
        var datoFørTetteFødlser = LocalDate.of(2022,1,1);
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .minsterett(false)
            .mor(true)
            .familieHendelseDato(datoFørTetteFødlser)
            //Barna ikke tett nok til minsteretten
            .familieHendelseDatoNesteSak(datoFørTetteFødlser.plusWeeks(45))
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        assertThat(finnMinsterett(grunnlag)).containsEntry(TETTE_FØDSLER, 0);
    }

    @Test
    void bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .minsterett(true)
            .mor(false)
            .bareFarHarRett(false)
            .familieHendelseDato(LocalDate.now())
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .build();
        assertThat(finnMinsterett(grunnlag)).doesNotContainKey(GENERELL_MINSTERETT);
        assertThat(finnMinsterett(grunnlag)).containsKey(FAR_UTTAK_RUNDT_FØDSEL);
    }

    @Test
    void begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel() {
        var grunnlag = new BeregnMinsterettGrunnlag.Builder()
            .minsterett(true)
            .mor(false)
            .familieHendelseDato(LocalDate.now())
            .gjelderFødsel(false)
            .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .bareFarHarRett(false)
            .aleneomsorg(false)
            .build();
        assertThat(finnMinsterett(grunnlag)).doesNotContainKey(FAR_UTTAK_RUNDT_FØDSEL);
    }

}
