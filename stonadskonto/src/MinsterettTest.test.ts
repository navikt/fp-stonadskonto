import { describe, it, expect } from 'vitest';
import { StønadskontoRegelOrkestrering } from './StønadskontoRegelOrkestrering';
import { BeregnKontoerGrunnlag } from './grunnlag/BeregnKontoerGrunnlag';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';
import { Rettighetstype } from './grunnlag/Rettighetstype';
import { Brukerrolle } from './grunnlag/Brukerrolle';
import { StønadskontoKontotype } from './StønadskontoKontotype';

const FØR_WLB = new Date(2022, 0, 1);
const ETTER_WLB_1 = new Date(2022, 11, 1);
const ETTER_WLB_2 = new Date(2024, 11, 1);

const stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

function assertEntry(retter: Map<StønadskontoKontotype, number>, type: StønadskontoKontotype, forventet: number) {
  if (forventet === 0) {
    expect(retter.has(type)).toBe(false);
  } else {
    expect(retter.get(type)).toBe(forventet);
  }
}

function fellesassert(grunnlag: BeregnKontoerGrunnlag, rundtfødsel: number, tettemor: number, tettefar: number, bfhrMinste: number, bfhrUtenAkt: number) {
  const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
  const retter = stønadskontoResultat.getStønadskontoer();
  assertEntry(retter, StønadskontoKontotype.FAR_RUNDT_FØDSEL, rundtfødsel);
  assertEntry(retter, StønadskontoKontotype.TETTE_SAKER_MOR, tettemor);
  assertEntry(retter, StønadskontoKontotype.TETTE_SAKER_FAR, tettefar);
  assertEntry(retter, StønadskontoKontotype.BARE_FAR_RETT, bfhrMinste);
  assertEntry(retter, StønadskontoKontotype.UFØREDAGER, bfhrUtenAkt);
}

describe('MinsterettTest', () => {
  it('wlb_bfhr_mor_ufør', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 10, 0, 0, 95, 0);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 10, 0, 0, 75, 0);
  });

  it('wlb_bfhr_mor_ufør_flerbarn', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .antallBarn(2)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 10, 0, 0, 106 + 95, 0);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .antallBarn(4)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
  });

  it('wlb_begge_rett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .brukerRolle(Brukerrolle.FAR)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 0, 0);
  });

  it('wlb_bfhr', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 50, 0);
  });

  it('wlb_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .morHarUføretrygd(true)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 0, 0);
  });

  it('wlb_totette_gir_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(ETTER_WLB_1)
      .familieHendelseDatoNesteSak(new Date(ETTER_WLB_1.getTime() + 40 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 10, 110, 40, 0, 0);
  });

  it('wlb_totette_adopsjon_gir_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .familieHendelseDatoNesteSak(new Date(ETTER_WLB_1.getTime() + 40 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 40, 40, 0, 0);
  });

  it('wlb_ikke_totette_gir_ikke_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .familieHendelseDatoNesteSak(new Date(ETTER_WLB_1.getTime() + 50 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 0, 0);
  });

  it('wlb_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 0, 0);
  });

  it('wlb_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_bfhr_mor_ufør', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 0, 0, 0, 0, 95);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 10, 0, 0, 75, 0);
  });

  it('regeldato_bfhr_mor_ufør_flerbarn', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .antallBarn(2)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 0, 0, 0, 0, 95);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .antallBarn(4)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
  });

  it('regeldato_begge_rett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_bfhr_2024_før_etter', () => {
    let grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 40, 0);

    grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_2)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 50, 0);
  });

  it('regeldato_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .morHarUføretrygd(true)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_totette_gir_ikke_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .familieHendelseDatoNesteSak(new Date(FØR_WLB.getTime() + 40 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_ikke_totette_gir_ikke_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .regelvalgsdato(FØR_WLB)
      .fødselsdato(ETTER_WLB_1)
      .familieHendelseDatoNesteSak(new Date(FØR_WLB.getTime() + 50 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('regeldato_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });
});

