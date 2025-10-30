import { describe, it, expect } from 'vitest';
import { Konfigurasjon } from './konfig/Konfigurasjon';
import { Parametertype } from './konfig/Parametertype';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';

describe('StandardKonfigurasjonTest', () => {
  it('test_standard_konfiguration', () => {
    const standard = Konfigurasjon.STANDARD;
    if (standard) {
      expect(
        standard.getParameter(
          Parametertype.FEDREKVOTE_DAGER,
          Dekningsgrad.DEKNINGSGRAD_100,
          new Date(2017, 11, 5)
        )
      ).toBe(75);
      expect(
        standard.getParameter(
          Parametertype.MØDREKVOTE_DAGER,
          Dekningsgrad.DEKNINGSGRAD_100,
          new Date(2017, 11, 5)
        )
      ).toBe(75);
    } else {
      expect(standard).toBeDefined();
    }
  });

  it('hent_parameter_utenfor_periode_skal_gi_exception', () => {
    const fortidlig = new Date(1970, 11, 5);
    expect(() => {
      Konfigurasjon.STANDARD.getParameter(
        Parametertype.FEDREKVOTE_DAGER,
        Dekningsgrad.DEKNINGSGRAD_80,
        fortidlig
      );
    }).toThrow();
  });
});

