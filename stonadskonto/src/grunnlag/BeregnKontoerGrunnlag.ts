import { Dekningsgrad } from './Dekningsgrad';
import { Rettighetstype } from './Rettighetstype';
import { Brukerrolle } from './Brukerrolle';

export class BeregnKontoerGrunnlag {
  public regelvalgsdato: Date | null = null;
  public dekningsgrad: Dekningsgrad | null = null;
  public rettighetstype: Rettighetstype | null = null;
  public brukerrolle: Brukerrolle | null = null;
  public antallBarn: number = 1;
  public fødselsdato: Date | null = null;
  public termindato: Date | null = null;
  public omsorgsovertakelseDato: Date | null = null;
  public morHarUføretrygd: boolean = false;
  public familieHendelseDatoNesteSak: Date | null = null;

  constructor() {}

  getAntallBarn(): number {
    return this.antallBarn;
  }

  isMorRett(): boolean {
    return this.isBeggeRett() || this.brukerrolle === Brukerrolle.MOR;
  }

  isFarRett(): boolean {
    return this.isBeggeRett() || this.brukerrolle !== Brukerrolle.MOR;
  }

  isBeggeRett(): boolean {
    return this.rettighetstype === Rettighetstype.BEGGE_RETT;
  }

  getDekningsgrad(): Dekningsgrad | null {
    return this.dekningsgrad;
  }

  getRettighetstype(): Rettighetstype | null {
    return this.rettighetstype;
  }

  getBrukerrolle(): Brukerrolle | null {
    return this.brukerrolle;
  }

  isMorHarUføretrygd(): boolean {
    return this.morHarUføretrygd;
  }

  getFamilieHendelseDatoNesteSak(): Date | null {
    return this.familieHendelseDatoNesteSak;
  }

  isBareFarHarRett(): boolean {
    return this.rettighetstype === Rettighetstype.BARE_SØKER_RETT && this.brukerrolle !== Brukerrolle.MOR;
  }

  isAleneomsorg(): boolean {
    return this.rettighetstype === Rettighetstype.ALENEOMSORG;
  }

  isGjelderFødsel(): boolean {
    return this.erFødsel();
  }

  getFamilieHendelseDato(): Date | null {
    return this.omsorgsovertakelseDato || this.fødselsdato || this.termindato || null;
  }

  getKonfigurasjonsvalgdato(): Date | null {
    return this.regelvalgsdato || this.getFamilieHendelseDato();
  }

  erFødsel(): boolean {
    return this.fødselsdato !== null || this.termindato !== null;
  }

  getFødselsdato(): Date | null {
    return this.fødselsdato;
  }

  getTermindato(): Date | null {
    return this.termindato;
  }

  static builder(): Builder {
    return new Builder();
  }
}

export class Builder {
  private kladd = new BeregnKontoerGrunnlag();

  regelvalgsdato(regelvalgsdato: Date | null): Builder {
    this.kladd.regelvalgsdato = regelvalgsdato;
    return this;
  }

  antallBarn(antallBarn: number): Builder {
    this.kladd.antallBarn = antallBarn;
    return this;
  }

  rettighetType(rettighetstype: Rettighetstype): Builder {
    this.kladd.rettighetstype = rettighetstype;
    return this;
  }

  brukerRolle(brukerrolle: Brukerrolle): Builder {
    this.kladd.brukerrolle = brukerrolle;
    return this;
  }

  dekningsgrad(dekningsgrad: Dekningsgrad): Builder {
    this.kladd.dekningsgrad = dekningsgrad;
    return this;
  }

  fødselsdato(dato: Date | null): Builder {
    this.kladd.fødselsdato = dato;
    return this;
  }

  termindato(dato: Date | null): Builder {
    this.kladd.termindato = dato;
    return this;
  }

  omsorgsovertakelseDato(dato: Date | null): Builder {
    this.kladd.omsorgsovertakelseDato = dato;
    return this;
  }

  morHarUføretrygd(morHarUføretrygd: boolean): Builder {
    this.kladd.morHarUføretrygd = morHarUføretrygd;
    return this;
  }

  familieHendelseDatoNesteSak(familieHendelseDatoNesteSak: Date | null): Builder {
    this.kladd.familieHendelseDatoNesteSak = familieHendelseDatoNesteSak;
    return this;
  }

  build(): BeregnKontoerGrunnlag {
    if (!this.kladd.rettighetstype) {
      throw new Error('rettighetType is required');
    }
    if (!this.kladd.brukerrolle) {
      throw new Error('brukerRolle is required');
    }
    if (!this.kladd.fødselsdato && !this.kladd.termindato && !this.kladd.omsorgsovertakelseDato) {
      throw new Error('At least one family event date is required (fødselsdato, termindato, or omsorgsovertakelseDato)');
    }
    return this.kladd;
  }
}

