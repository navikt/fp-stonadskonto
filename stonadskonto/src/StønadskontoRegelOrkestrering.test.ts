import { describe, it, expect } from 'vitest';
import { StønadskontoRegelOrkestrering } from './StønadskontoRegelOrkestrering';
import { BeregnKontoerGrunnlag } from './grunnlag/BeregnKontoerGrunnlag';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';
import { Rettighetstype } from './grunnlag/Rettighetstype';
import { Brukerrolle } from './grunnlag/Brukerrolle';
import { StønadskontoKontotype } from './StønadskontoKontotype';
import { beregnPrematurdager } from './rettighet/PrematurukerUtil';

const PREMATUR = new Date(2019, 6, 1);
const ETTER_WLB_2 = new Date(2024, 11, 1);

const stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

function assertSumDager(stønadskontoer: Map<StønadskontoKontotype, number>, expected: number) {
  const STØNADSDAGER = new Set([
    StønadskontoKontotype.FELLESPERIODE,
    StønadskontoKontotype.FEDREKVOTE,
    StønadskontoKontotype.MØDREKVOTE,
    StønadskontoKontotype.FORELDREPENGER,
    StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL,
  ]);

  const sum = Array.from(stønadskontoer.entries())
    .filter(([konto]) => STØNADSDAGER.has(konto))
    .reduce((acc, [, value]) => acc + value, 0);
  expect(sum).toBe(expected);
}

describe('StønadskontoRegelOrkestrering', () => {
  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 245);
  });

  it('fødsel_far_har_rett_innenlands_annen_europeisk_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 245);
  });

  it('skal_legge_til_prematurdager_på_fellesperiode', () => {
    const termindato = new Date(PREMATUR.getTime() + 7 * 7 * 24 * 60 * 60 * 1000 + 4 * 24 * 60 * 60 * 1000);
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(PREMATUR)
      .termindato(termindato)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    const ekstradager = beregnPrematurdager(PREMATUR, termindato);
    const forventetFellesperiode = 80 + ekstradager;

    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(forventetFellesperiode);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_PREMATUR)).toBe(ekstradager);
    assertSumDager(stønadskontoer, 245 + ekstradager);
  });

  it('skal_legge_til_prematurdager_på_foreldrepenger', () => {
    const termindato = new Date(PREMATUR.getTime() + 8 * 7 * 24 * 60 * 60 * 1000);
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(PREMATUR)
      .termindato(termindato)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    const ekstradager = beregnPrematurdager(PREMATUR, termindato);
    const forventetForeldrepenger = 80 + 75 + 75 + ekstradager;

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(forventetForeldrepenger);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_PREMATUR)).toBe(ekstradager);
    assertSumDager(stønadskontoer, 230 + ekstradager);
  });

  it('skal_ikke_legge_til_prematurdager_på_flerbarnsdager', () => {
    const termindato = new Date(PREMATUR.getTime() + 7 * 7 * 24 * 60 * 60 * 1000 + 1 * 24 * 60 * 60 * 1000);
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(PREMATUR)
      .termindato(termindato)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(85);
    expect(stønadskontoer.has(StønadskontoKontotype.TILLEGG_PREMATUR)).toBe(false);
    assertSumDager(stønadskontoer, 245 + 85);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    assertSumDager(stønadskontoer, 230);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 306);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    assertSumDager(stønadskontoer, 291);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80 + 230);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 245 + 230);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80 + 230);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    assertSumDager(stønadskontoer, 230 + 230);
  });

  it('fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    assertSumDager(stønadskontoer, 245 + 85);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    assertSumDager(stønadskontoer, 230 + 85);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 230 + 85);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 291 + 106);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    assertSumDager(stønadskontoer, 200);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    assertSumDager(stønadskontoer, 261);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(85);
    assertSumDager(stønadskontoer, 200 + 85);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(106);
    assertSumDager(stønadskontoer, 261 + 106);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 200);
  });

  it('fødsel_bare_far_rett_begge_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 261);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(288);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 306 + 288);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(288);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    assertSumDager(stønadskontoer, 291 + 288);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80 + 85);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 245 + 85);
  });

  it('adopsjon_begge_har_rett_og_omsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80 + 85);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    assertSumDager(stønadskontoer, 230 + 85);
  });

  it('fødsel_begge_har_rett_og_omsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(7);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(106);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 306 + 106);
  });

  it('fødsel_mor_har_rett_innenlands_annen_europeisk_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(5);
    expect(stønadskontoer.get(StønadskontoKontotype.FELLESPERIODE)).toBe(80);
    expect(stønadskontoer.get(StønadskontoKontotype.FEDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.MØDREKVOTE)).toBe(75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 245);
  });

  it('fødsel_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    assertSumDager(stønadskontoer, 230 + 85);
  });

  it('fødsel_bare_mor_rett_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    assertSumDager(stønadskontoer, 306 + 106);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    assertSumDager(stønadskontoer, 291 + 106);
  });

  it('fødsel_bare_mor_rett_begge_omsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 230 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    assertSumDager(stønadskontoer, 245 + 230);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 230 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    assertSumDager(stønadskontoer, 230 + 230);
  });

  it('fødsel_bare_mor_rett_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(15);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    assertSumDager(stønadskontoer, 306 + 288);
  });

  it('adopsjon_bare_mor_rett_og_aleneomsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    assertSumDager(stønadskontoer, 291 + 288);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 230);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 291);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 230 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 230 + 230);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 291 + 288);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_100_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .termindato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(80 + 85 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(85);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 230 + 85);
  });

  it('fødsel_bare_far_rett_og_aleneomsorg_dekningsgrad_80_2_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .fødselsdato(ETTER_WLB_2)
      .antallBarn(2)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291 + 106);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    expect(stønadskontoer.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
    assertSumDager(stønadskontoer, 291 + 106);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_100', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    assertSumDager(stønadskontoer, 200);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_80', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(2);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    assertSumDager(stønadskontoer, 261);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_100_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(50 + 230 + 75 + 75);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(230);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(230);
    assertSumDager(stønadskontoer, 200 + 230);
  });

  it('adopsjon_bare_far_rett_begge_omsorg_dekningsgrad_80_3_barn', () => {
    const grunnlag = BeregnKontoerGrunnlag.builder()
      .omsorgsovertakelseDato(ETTER_WLB_2)
      .antallBarn(3)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();

    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    const stønadskontoer = stønadskontoResultat.getStønadskontoer();

    expect(stønadskontoer.size).toBe(3);
    expect(stønadskontoer.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261 + 288);
    expect(stønadskontoer.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(288);
    expect(stønadskontoer.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(288);
    assertSumDager(stønadskontoer, 261 + 288);
  });
});

