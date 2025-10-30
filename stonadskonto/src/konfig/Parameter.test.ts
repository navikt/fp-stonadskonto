import { describe, it, expect } from 'vitest';
import { Parameter } from './Parameter';
import { Dekningsgrad } from '../grunnlag/Dekningsgrad';

describe('Parameter', () => {
  it('periode_med_start_og_slutt_og_dato_utenfor_skal_ikke_overlappe', () => {
    const testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, new Date(2016, 0, 1), new Date(2018, 0, 1), 1);

    expect(testPeriode.overlappestDato(new Date(2019, 0, 1))).toBe(false);
  });

  it('periode_uten_start_og_slutt_skal_overlappe', () => {
    const testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, null, null, 1);

    expect(testPeriode.overlappestDato(new Date(2017, 0, 1))).toBe(true);
  });

  it('periode_med_start_og_slutt_skal_overlappe', () => {
    const testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, new Date(2016, 0, 1), new Date(2018, 0, 1), 1);

    expect(testPeriode.overlappestDato(new Date(2017, 0, 1))).toBe(true);
  });

  it('periode_med_bare_start_skal_overlappe', () => {
    const testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, new Date(2016, 0, 1), null, 1);

    expect(testPeriode.overlappestDato(new Date(2017, 0, 1))).toBe(true);
  });

  it('periode_med_bare_start_og_dato_før_start_skal_ikke_overlappe', () => {
    const testPeriode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, new Date(2016, 0, 1), null, 1);

    expect(testPeriode.overlappestDato(new Date(2015, 0, 1))).toBe(false);
  });

  it('helePeriodenOverlapper', () => {
    const fom = new Date();
    const tom = new Date(fom.getTime() + 2 * 7 * 24 * 60 * 60 * 1000);
    const periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

    const before = new Date(fom.getTime() - 1 * 24 * 60 * 60 * 1000);
    const after = new Date(tom.getTime() + 1 * 24 * 60 * 60 * 1000);

    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, before, after, 1))).toBe(true);

    const inside1 = new Date(fom.getTime() + 1 * 24 * 60 * 60 * 1000);
    const inside2 = new Date(tom.getTime() - 1 * 24 * 60 * 60 * 1000);
    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, inside1, inside2, 1))).toBe(true);

    expect(periode.overlapper(periode)).toBe(true);
  });

  it('begynnelsenAvPeriodenOverlapper', () => {
    const fom = new Date();
    const tom = new Date(fom.getTime() + 2 * 7 * 24 * 60 * 60 * 1000);
    const periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

    const before = new Date(fom.getTime() - 1 * 24 * 60 * 60 * 1000);
    const tomMinus = new Date(tom.getTime() - 1 * 24 * 60 * 60 * 1000);

    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, before, tomMinus, 1))).toBe(true);
    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, fom, 1))).toBe(true);
    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, before, fom, 1))).toBe(true);
  });

  it('sluttenAvPeriodenOverlapper', () => {
    const fom = new Date();
    const tom = new Date(fom.getTime() + 2 * 7 * 24 * 60 * 60 * 1000);
    const periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

    const fomPlus = new Date(fom.getTime() + 1 * 24 * 60 * 60 * 1000);
    const tomPlus = new Date(tom.getTime() + 1 * 24 * 60 * 60 * 1000);

    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fomPlus, tomPlus, 1))).toBe(true);
    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, tom, tom, 1))).toBe(true);
    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, tom, tomPlus, 1))).toBe(true);
  });

  it('periodenRettFørOverlapperIkke', () => {
    const fom = new Date();
    const tom = new Date(fom.getTime() + 2 * 7 * 24 * 60 * 60 * 1000);
    const periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

    const before1 = new Date(fom.getTime() - 10 * 24 * 60 * 60 * 1000);
    const before2 = new Date(fom.getTime() - 1 * 24 * 60 * 60 * 1000);

    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, before1, before2, 1))).toBe(false);
  });

  it('periodenRettEtterOverlapperIkke', () => {
    const fom = new Date();
    const tom = new Date(fom.getTime() + 2 * 7 * 24 * 60 * 60 * 1000);
    const periode = new Parameter(Dekningsgrad.DEKNINGSGRAD_100, fom, tom, 1);

    const after1 = new Date(tom.getTime() + 1 * 24 * 60 * 60 * 1000);
    const after2 = new Date(tom.getTime() + 5 * 24 * 60 * 60 * 1000);

    expect(periode.overlapper(new Parameter(Dekningsgrad.DEKNINGSGRAD_100, after1, after2, 1))).toBe(false);
  });
});

