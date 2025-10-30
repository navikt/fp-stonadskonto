import { StønadskontoKontotype } from '../StønadskontoKontotype';
import { Parametertype } from '../konfig/Parametertype';

export interface Kontokonfigurasjon {
  stønadskontotype: StønadskontoKontotype;
  parametertype: Parametertype | null;
}

export function createKontokonfigurasjon(
  stønadskontotype: StønadskontoKontotype,
  parametertype: Parametertype | null
): Kontokonfigurasjon {
  return { stønadskontotype, parametertype };
}

