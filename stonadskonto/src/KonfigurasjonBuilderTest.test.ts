import { describe, it, expect } from 'vitest';
import { Konfigurasjon } from './konfig/Konfigurasjon';
import { Parametertype } from './konfig/Parametertype';
import { Dekningsgrad } from './grunnlag/Dekningsgrad';

describe('KonfigurasjonBuilderTest', () => {
  it('konfigurasjon_standard_returns_values', () => {
    const date = new Date(2024, 0, 1);
    const result = Konfigurasjon.STANDARD.getParameter(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, date);
    expect(result).toBeGreaterThan(0);
  });
});

