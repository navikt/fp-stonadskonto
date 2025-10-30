import { describe, it, expect } from 'vitest';
import { oppfyllerKravTilPrematuruker } from './PrematurukerUtil';

describe('PrematurukerUtil', () => {
  it('false_hvis_fødselsdato_er_null', () => {
    const resultat = oppfyllerKravTilPrematuruker(null, new Date(2019, 6, 1));
    expect(resultat).toBe(false);
  });

  it('false_hvis_termindato_er_null', () => {
    const resultat = oppfyllerKravTilPrematuruker(new Date(2019, 6, 1), null);
    expect(resultat).toBe(false);
  });

  it('false_hvis_fødselsdato_er_før_første_juli_og_fødsel_er_mer_enn_7_uker_før_termin', () => {
    const fødselsdato = new Date(2019, 5, 30);
    const resultat = oppfyllerKravTilPrematuruker(fødselsdato, new Date(2019, 7, 20));
    expect(resultat).toBe(false);
  });

  it('false_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_akkurat_7_uker_før_termin', () => {
    const fødselsdato = new Date(2019, 6, 1);
    const resultat = oppfyllerKravTilPrematuruker(fødselsdato, new Date(2019, 7, 19));
    expect(resultat).toBe(false);
  });

  it('false_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_etter_termin', () => {
    const fødselsdato = new Date(2019, 7, 1);
    const resultat = oppfyllerKravTilPrematuruker(fødselsdato, fødselsdato);
    expect(resultat).toBe(false);
  });

  it('true_hvis_fødselsdato_er_etter_første_juli_og_fødsel_er_mer_enn_7_uker_før_termin', () => {
    const fødselsdato = new Date(2019, 6, 1);
    const resultat = oppfyllerKravTilPrematuruker(fødselsdato, new Date(2019, 7, 23));
    expect(resultat).toBe(true);
  });
});

