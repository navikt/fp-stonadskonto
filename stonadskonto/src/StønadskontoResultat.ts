import { StønadskontoKontotype } from './StønadskontoKontotype';

export class StønadskontoResultat {
  private stønadskontoer: Map<StønadskontoKontotype, number>;

  constructor(stønadskontoer: Map<StønadskontoKontotype, number>) {
    if (!stønadskontoer) {
      throw new Error('stønadskontoer cannot be null');
    }
    this.stønadskontoer = new Map(stønadskontoer);
  }

  getStønadskontoer(): Map<StønadskontoKontotype, number> {
    return new Map(this.stønadskontoer);
  }
}

