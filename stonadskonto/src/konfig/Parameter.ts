import { Dekningsgrad } from '../grunnlag/Dekningsgrad';

export class Parameter {
  readonly dekningsgrad: Dekningsgrad | null;
  readonly fom: Date;
  readonly tom: Date;
  readonly verdi: number;

  constructor(dekningsgrad: Dekningsgrad | null, fom: Date | null, tom: Date | null, verdi: number) {
    if (fom && tom && tom < fom) {
      throw new Error(`Til og med dato før fra og med dato: ${fom} > ${tom}`);
    }
    this.dekningsgrad = dekningsgrad;
    this.fom = fom || new Date(-8640000000000000);
    this.tom = tom || new Date(8640000000000000);
    this.verdi = verdi;
  }

  overlapper(annen: Parameter): boolean {
    if (this.dekningsgrad !== annen.dekningsgrad) {
      return false;
    }
    return this.overlappestDato(annen.fom) || this.overlappestDato(annen.tom) || this.erOmsluttetAv(annen);
  }

  overlappestDato(dato: Date): boolean {
    return dato >= this.fom && dato <= this.tom;
  }

  erOmsluttetAv(periode: Parameter): boolean {
    return periode.fom <= this.fom && periode.tom >= this.tom;
  }
}

