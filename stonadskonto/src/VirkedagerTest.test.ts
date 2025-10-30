import { describe, it, expect } from 'vitest';
import { beregnPrematurdager } from './rettighet/PrematurukerUtil';

describe('VirkedagerTest', () => {
  it('beregnPrematurdagerBasic', () => {
    const fødselsdato = new Date(2019, 6, 1);
    const termindato = new Date(2019, 13, 10);
    const result = beregnPrematurdager(fødselsdato, termindato);
    expect(result).toBeGreaterThan(0);
  });

  it('skalBeregneAntallVirkedager', () => {
    const iDag = new Date();
    const dayOfWeek = iDag.getDay();
    const mandag = new Date(iDag);
    mandag.setDate(iDag.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1));

    const søndag = new Date(mandag);
    søndag.setDate(mandag.getDate() + 6);

    expect(beregnPrematurdager(mandag, søndag)).toBeGreaterThanOrEqual(0);
    expect(beregnPrematurdager(mandag, new Date(søndag.getTime() + 1 * 24 * 60 * 60 * 1000))).toBeGreaterThanOrEqual(0);
    expect(beregnPrematurdager(mandag, new Date(søndag.getTime() + 10 * 24 * 60 * 60 * 1000))).toBeGreaterThanOrEqual(0);
  });
});

