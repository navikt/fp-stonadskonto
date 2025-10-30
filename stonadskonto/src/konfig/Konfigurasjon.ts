import { Parametertype } from './Parametertype';
import { Parameter } from './Parameter';
import { Dekningsgrad } from '../grunnlag/Dekningsgrad';

export class KonfigurasjonBuilder {
  private parameterMap: Map<Parametertype, Parameter[]> = new Map();

  private constructor() {}

  static create(): KonfigurasjonBuilder {
    return new KonfigurasjonBuilder();
  }

  leggTilParameter(
    parametertype: Parametertype,
    fom: Date | null,
    tom: Date | null,
    verdi: number
  ): KonfigurasjonBuilder {
    return this.leggTilParameterMedDekningsgrad(parametertype, null, fom, tom, verdi);
  }

  leggTilParameterMedDekningsgrad(
    parametertype: Parametertype,
    dekningsgrad: Dekningsgrad | null,
    fom: Date | null,
    tom: Date | null,
    verdi: number
  ): KonfigurasjonBuilder {
    const nyParameter = new Parameter(dekningsgrad, fom, tom, verdi);
    let parameterListe = this.parameterMap.get(parametertype);

    if (!parameterListe) {
      parameterListe = [nyParameter];
      this.parameterMap.set(parametertype, parameterListe);
    } else {
      const overlapp = parameterListe.some(p => p.overlapper(nyParameter));
      if (overlapp) {
        throw new Error('Overlappende perioder kan ikke eksistere i konfigurasjon.');
      }
      parameterListe.push(nyParameter);
    }

    return this;
  }

  build(): Konfigurasjon {
    return new Konfigurasjon(this.parameterMap);
  }
}

export class Konfigurasjon {
  private parameterMap: Map<Parametertype, Parameter[]>;
  private static _standard: Konfigurasjon | null = null;

  static get STANDARD(): Konfigurasjon {
    if (!Konfigurasjon._standard) {
      Konfigurasjon._standard = Konfigurasjon.initializeStandard();
    }
    return Konfigurasjon._standard;
  }

  constructor(parameterMap: Map<Parametertype, Parameter[]>) {
    this.parameterMap = new Map(parameterMap);
  }

  private static initializeStandard(): Konfigurasjon {
    const DATO_TIDLIGST = new Date(2010, 0, 1);
    const DATO_VEDTAK = new Date(2019, 0, 1);
    const DATO_MINSTERETT_1 = new Date(2022, 7, 2);
    const DAG_FØR_MINSTERETT_1 = new Date(2022, 7, 1);
    const DATO_UTLIGNE_80 = new Date(2024, 6, 1);
    const DAG_FØR_UTLIGNE_80 = new Date(2024, 5, 30);
    const DATO_MINSTERETT_2 = new Date(2024, 7, 2);

    const builder = KonfigurasjonBuilder.create();

    builder
      .leggTilParameterMedDekningsgrad(Parametertype.MØDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 75)
      .leggTilParameterMedDekningsgrad(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 75)
      .leggTilParameterMedDekningsgrad(Parametertype.FELLESPERIODE_DAGER, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 80)

      .leggTilParameterMedDekningsgrad(Parametertype.MØDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_VEDTAK, null, 95)
      .leggTilParameterMedDekningsgrad(Parametertype.MØDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, new Date(2018, 11, 31), 75)

      .leggTilParameterMedDekningsgrad(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_VEDTAK, null, 95)
      .leggTilParameterMedDekningsgrad(Parametertype.FEDREKVOTE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, new Date(2018, 11, 31), 75)

      .leggTilParameterMedDekningsgrad(Parametertype.FELLESPERIODE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_UTLIGNE_80, null, 101)
      .leggTilParameterMedDekningsgrad(Parametertype.FELLESPERIODE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_VEDTAK, DAG_FØR_UTLIGNE_80, 90)
      .leggTilParameterMedDekningsgrad(Parametertype.FELLESPERIODE_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, new Date(2018, 11, 31), 130)

      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 230)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_UTLIGNE_80, null, 291)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_UTLIGNE_80, 280)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 200)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_UTLIGNE_80, null, 261)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_UTLIGNE_80, 250)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_FØR_FØDSEL, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 15)
      .leggTilParameterMedDekningsgrad(Parametertype.FORELDREPENGER_FØR_FØDSEL, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, null, 15)

      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TO_BARN, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 85)
      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TO_BARN, Dekningsgrad.DEKNINGSGRAD_80, DATO_UTLIGNE_80, null, 106)
      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TO_BARN, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_UTLIGNE_80, 105)
      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, null, 230)
      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, Dekningsgrad.DEKNINGSGRAD_80, DATO_UTLIGNE_80, null, 288)
      .leggTilParameterMedDekningsgrad(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_UTLIGNE_80, 280)

      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, Dekningsgrad.DEKNINGSGRAD_100, DATO_MINSTERETT_1, null, 0)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, Dekningsgrad.DEKNINGSGRAD_80, DATO_MINSTERETT_1, null, 0)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 75)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 95)
      .leggTilParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, DATO_MINSTERETT_2, null, 50)
      .leggTilParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, DATO_MINSTERETT_1, new Date(2024, 7, 1), 40)
      .leggTilParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_100, DATO_MINSTERETT_1, null, 75)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_100, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_80, DATO_MINSTERETT_1, null, 95)
      .leggTilParameterMedDekningsgrad(Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT, Dekningsgrad.DEKNINGSGRAD_80, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)

      .leggTilParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, DATO_MINSTERETT_1, null, 10)
      .leggTilParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)

      .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, DATO_MINSTERETT_1, null, 110)
      .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
      .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, DATO_MINSTERETT_1, null, 40)
      .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0)
      .leggTilParameter(Parametertype.FAR_TETTE_SAKER_DAGER, DATO_MINSTERETT_1, null, 40)
      .leggTilParameter(Parametertype.FAR_TETTE_SAKER_DAGER, DATO_TIDLIGST, DAG_FØR_MINSTERETT_1, 0);

    return builder.build();
  }

  getParameter(parametertype: Parametertype, dekningsgrad: Dekningsgrad | null, dato: Date): number {
    const parameters = this.getParameterVerdier(parametertype);
    const found = parameters.find(
      p => (p.dekningsgrad === dekningsgrad || p.dekningsgrad === null) && p.overlappestDato(dato)
    );

    if (!found) {
      throw new Error(
        `Ingen parameter funnet for ${parametertype} med dekningsgrad ${dekningsgrad || ''} på dato ${dato}`
      );
    }

    return found.verdi;
  }

  private getParameterVerdier(parametertype: Parametertype): Parameter[] {
    const params = this.parameterMap.get(parametertype);
    if (!params) {
      throw new Error(`Konfigurasjon-feil/Utvikler-feil: mangler parameter av type ${parametertype}`);
    }
    return params;
  }
}

