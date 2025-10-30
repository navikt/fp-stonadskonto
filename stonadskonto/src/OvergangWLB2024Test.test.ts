import { describe, it, expect } from 'vitest';
import { StønadskontoRegelOrkestrering } from './StønadskontoRegelOrkestrering';
import { BeregnKontoerGrunnlag } from './grunnlag/BeregnKontoerGrunnlag';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';
import { Rettighetstype } from './grunnlag/Rettighetstype';
import { Brukerrolle } from './grunnlag/Brukerrolle';
import { StønadskontoKontotype } from './StønadskontoKontotype';

const ETTER_WLB_1 = new Date(2022, 11, 1);
const ETTER_WLB_2 = new Date(2024, 11, 1);

const stønadskontoRegelOrkestrering = new StønadskontoRegelOrkestrering();

describe('OvergangWLB2024Test', () => {
  it('overgang_wlb_2024_dekning_80_prosent_begge_rett', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_begge_rett_flerbarn', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(2)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90 + 105);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(105);
    expect(utregnet.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(105);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(2)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101 + 106);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.get(StønadskontoKontotype.FLERBARNSDAGER)).toBe(106);
    expect(utregnet.get(StønadskontoKontotype.TILLEGG_FLERBARN)).toBe(106);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_begge_rett_prematur', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(new Date(ETTER_WLB_2.getTime() + 10 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(90);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BEGGE_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .termindato(new Date(ETTER_WLB_2.getTime() + 10 * 7 * 24 * 60 * 60 * 1000))
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FELLESPERIODE)).toBe(101 + 45);
    expect(utregnet.get(StønadskontoKontotype.MØDREKVOTE)).toBe(95);
    expect(utregnet.get(StønadskontoKontotype.TILLEGG_PREMATUR)).toBe(45);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_mor_rett', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(false);
  });

  it('overgang_wlb_2024_dekning_80_prosent_mor_alene', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.MOR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
  });

  it('overgang_wlb_2024_dekning_80_prosent_far_rett', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(250);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(40);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_far_alene', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(280);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.ALENEOMSORG)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(291);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.BARE_FAR_RETT)).toBe(false);
  });

  it('overgang_wlb_2024_dekning_80_prosent_far_rett_uføre', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .morHarUføretrygd(true)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(250);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(95);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .morHarUføretrygd(true)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(95);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_far_rett_flerbarn', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(2)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(250 + 105);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(105);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(2)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261 + 106);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(106);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_dekning_80_prosent_far_rett_trebarn', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(3)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(250 + 280);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(280);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
      .antallBarn(3)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(261 + 288);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(288);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_bare_far_rett', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(200);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(40);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(200);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(50);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });

  it('overgang_wlb_2024_bare_far_rett_uføre', () => {
    const tidligere = BeregnKontoerGrunnlag.builder()
      .regelvalgsdato(ETTER_WLB_1)
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .termindato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .morHarUføretrygd(true)
      .build();
    let utregnet = stønadskontoRegelOrkestrering.beregnKontoer(tidligere).getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(200);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(75);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);

    const grunnlag = BeregnKontoerGrunnlag.builder()
      .rettighetType(Rettighetstype.BARE_SØKER_RETT)
      .brukerRolle(Brukerrolle.FAR)
      .fødselsdato(ETTER_WLB_2)
      .dekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
      .morHarUføretrygd(true)
      .build();
    const stønadskontoResultat = stønadskontoRegelOrkestrering.beregnKontoer(grunnlag);
    utregnet = stønadskontoResultat.getStønadskontoer();
    expect(utregnet.get(StønadskontoKontotype.FORELDREPENGER)).toBe(200);
    expect(utregnet.has(StønadskontoKontotype.FELLESPERIODE)).toBe(false);
    expect(utregnet.has(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL)).toBe(false);
    expect(utregnet.get(StønadskontoKontotype.BARE_FAR_RETT)).toBe(75);
    expect(utregnet.get(StønadskontoKontotype.FAR_RUNDT_FØDSEL)).toBe(10);
  });
});

