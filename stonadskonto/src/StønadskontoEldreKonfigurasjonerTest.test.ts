import { describe, it, expect } from 'vitest';
import { StønadskontoRegelOrkestrering } from './StønadskontoRegelOrkestrering';
import { BeregnKontoerGrunnlag } from './grunnlag/BeregnKontoerGrunnlag';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';
import { Rettighetstype } from './grunnlag/Rettighetstype';
import { Brukerrolle } from './grunnlag/Brukerrolle';
import { StønadskontoKontotype } from './StønadskontoKontotype';

const FØR_WLB = new Date(2022, 6, 1);
const ETTER_WLB_1 = new Date(2022, 11, 1);

const stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

const STØNADSDAGER = new Set([
  StønadskontoKontotype.FELLESPERIODE,
  StønadskontoKontotype.FEDREKVOTE,
  StønadskontoKontotype.MØDREKVOTE,
  StønadskontoKontotype.FORELDREPENGER,
  StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL,
]);

function assertSumDager(stønadskontoer: Map<StønadskontoKontotype, number>, expected: number) {
  const sum = Array.from(stønadskontoer.entries())
    .filter(([konto]) => STØNADSDAGER.has(konto))
    .reduce((acc, [, value]) => acc + value, 0);
  expect(sum).toBe(expected);
}

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

describe('StønadskontoEldreKonfigurasjonerTest', () => {
  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 295);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    assertSumDager(stønadskontoer, 280);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90 + 280);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 295 + 280);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90 + 280);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    assertSumDager(stønadskontoer, 280 + 280);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90 + 105);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 295 + 105);
  });

  it('fødsel_bare_mor_har_rett_begge_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    assertSumDager(stønadskontoer, 295);
  });

  it('adopsjon_bare_mor_har_rett_og_aleneomsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(1);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    assertSumDager(stønadskontoer, 280);
  });

  it('fødsel_bare_mor_rett_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_1)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 105);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    assertSumDager(stønadskontoer, 295 + 105);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 105);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    assertSumDager(stønadskontoer, 280 + 105);
  });

  it('fødsel_bare_mor_rett_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 280);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    assertSumDager(stønadskontoer, 295 + 280);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 280);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    assertSumDager(stønadskontoer, 280 + 280);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(1);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(200);
    assertSumDager(stønadskontoer, 200);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(1);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(250);
    assertSumDager(stønadskontoer, 250);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(FØR_WLB)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 230 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    assertSumDager(stønadskontoer, 200 + 230);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(100 + 280 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    assertSumDager(stønadskontoer, 250 + 280);
  });

  it('wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(4);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(100 + 280 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 250 + 280);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    assertSumDager(stønadskontoer, 200 + 85);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(100 + 105 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    assertSumDager(stønadskontoer, 250 + 105);
  });

  it('wlb_fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(4);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(100 + 105 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 250 + 105);
  });

  it('bfhr_flere_barn_ikke_minsterett_skal_gi_flerbarnsdager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(FØR_WLB)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.has(StønadskontoKontotype.FLERBARNSDAGER)).toBe(true);
    assertSumDager(stønadskontoer, 200 + 85);
  });

  it('wlb_bfhr_mor_ufør_flerbarn', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .antallBarn(2)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 10, 0, 0, 105 + 95, 0);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .antallBarn(4)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 10, 0, 0, 230 + 75, 0);
  });

  it('wlb_begge_rett_før_utvidelse', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .brukerRolle(Brukerrolle.FAR)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 0, 0);
  });

  it('wlb_bfhr_før_utvidelse', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 40, 0);
  });

  it('regeldato_bfhr', () => {
    let grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(FØR_WLB)
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);

    grunnlag = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(new Date(ETTER_WLB_1.getTime() - 30 * 24 * 60 * 60 * 1000))
      .fødselsdato(ETTER_WLB_1)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .build();
    fellesassert(grunnlag, 10, 0, 0, 40, 0);
  });

  it('gammel_bfhr_mor_ufør', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 0, 0, 0, 0, 95);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 0, 0, 0, 0, 75);
  });

  it('gammel_bfhr_mor_ufør_flerbarn', () => {
    const grunnlag80 = BeregnKontoerGrunnlag.builder()
      .antallBarn(2)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag80, 0, 0, 0, 0, 95);

    const grunnlag100 = BeregnKontoerGrunnlag.builder()
      .antallBarn(4)
      .morHarUføretrygd(true)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag100, 0, 0, 0, 0, 75);
  });

  it('gammel_begge_rett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_bfhr', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_både_bfhr_og_alenesomsorg_skal_tolkes_som_aleneomsorg', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .morHarUføretrygd(true)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_totette_gir_ikke_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .omsorgsovertakelseDato(FØR_WLB)
      .familieHendelseDatoNesteSak(new Date(FØR_WLB.getTime() + 40 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_ikke_totette_gir_ikke_dager', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(FØR_WLB)
      .familieHendelseDatoNesteSak(new Date(FØR_WLB.getTime() + 50 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_bfhr_mor_rett_i_eøs_skal_ikke_få_generell_minsterett', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('gammel_begge_rett_adopsjon_gir_ikke_dager_rundt_fødsel', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .omsorgsovertakelseDato(FØR_WLB)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    fellesassert(grunnlag, 0, 0, 0, 0, 0);
  });

  it('adopsjon_bare_far_rett_og_aleneomsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(1);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    assertSumDager(stønadskontoer, 280);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_1)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 280);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 280);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(280);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 280 + 280);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_1)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();
    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280 + 105);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 280 + 105);
  });
});
