import { BeregnKontoerGrunnlag } from '../grunnlag/BeregnKontoerGrunnlag';
import { StønadskontoKontotype } from '../StønadskontoKontotype';
import { Kontokonfigurasjon } from './Kontokonfigurasjon';

export class KontoerMellomregning {
  private grunnlag: BeregnKontoerGrunnlag;
  private kontokonfigurasjon: Kontokonfigurasjon[] = [];
  private beregnet: Map<StønadskontoKontotype, number> = new Map();

  constructor(grunnlag: BeregnKontoerGrunnlag) {
    this.grunnlag = grunnlag;
  }

  getGrunnlag(): BeregnKontoerGrunnlag {
    return this.grunnlag;
  }

  getKontokonfigurasjon(): Kontokonfigurasjon[] {
    return this.kontokonfigurasjon;
  }

  getBeregnet(): Map<StønadskontoKontotype, number> {
    return this.beregnet;
  }
}

