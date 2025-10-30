import { KontoerMellomregning } from './KontoerMellomregning';
import { BeregnKontoerGrunnlag } from '../grunnlag/BeregnKontoerGrunnlag';
import { Brukerrolle } from '../grunnlag/Brukerrolle';
import { StønadskontoKontotype } from '../StønadskontoKontotype';
import { Parametertype } from '../konfig/Parametertype';
import { createKontokonfigurasjon } from './Kontokonfigurasjon';
import { toTette } from '../rettighet/TetteSakerUtil';
import { oppfyllerKravTilPrematuruker, beregnPrematurdager } from '../rettighet/PrematurukerUtil';
import { Konfigurasjon } from '../konfig/Konfigurasjon';

export function fastsettStønadskontoStruktur(mellomregning: KontoerMellomregning): boolean {
  const grunnlag = mellomregning.getGrunnlag();
  const erMor = grunnlag.getBrukerrolle() === Brukerrolle.MOR;

  if (grunnlag.isBeggeRett()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_DAGER));
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER));
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER));
  } else if (erMor || grunnlag.isAleneomsorg()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER));
  } else if (grunnlag.isBareFarHarRett()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FORELDREPENGER, Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER));
  } else {
    throw new Error('Ukjent kombinasjon av rettighet og rolle');
  }

  return true;
}

export function sjekkOmFødsel(mellomregning: KontoerMellomregning): boolean {
  return mellomregning.getGrunnlag().erFødsel();
}

export function sjekkOmMerEnnEttBarn(mellomregning: KontoerMellomregning): boolean {
  return mellomregning.getGrunnlag().getAntallBarn() > 1;
}

export function sjekkOmBareFarHarRett(mellomregning: KontoerMellomregning): boolean {
  return mellomregning.getGrunnlag().isBareFarHarRett();
}

export function sjekkOmTetteSaker(mellomregning: KontoerMellomregning): boolean {
  const grunnlag = mellomregning.getGrunnlag();
  const konfDato = grunnlag.getKonfigurasjonsvalgdato();
  const familieHendelseDato = grunnlag.getFamilieHendelseDato();

  if (!konfDato || !familieHendelseDato) {
    return false;
  }

  return toTette(konfDato, familieHendelseDato, grunnlag.getFamilieHendelseDatoNesteSak());
}

export function leggTilDagerVedFødsel(mellomregning: KontoerMellomregning): void {
  const grunnlag = mellomregning.getGrunnlag();

  if (grunnlag.isMorRett()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL));
  }

  if (grunnlag.isFarRett()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FAR_RUNDT_FØDSEL, Parametertype.FAR_DAGER_RUNDT_FØDSEL));
  }

  const fødselsdato = grunnlag.getFødselsdato();
  const termindato = grunnlag.getTermindato();

  if (oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.TILLEGG_PREMATUR, null));
  }
}

export function flerbarnsParametertype(grunnlag: BeregnKontoerGrunnlag): Parametertype | null {
  if (grunnlag.getAntallBarn() === 2) {
    return Parametertype.EKSTRA_DAGER_TO_BARN;
  } else if (grunnlag.getAntallBarn() > 2) {
    return Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN;
  }
  return null;
}

export function aktivitetsKravIkkeMinsterett(grunnlag: BeregnKontoerGrunnlag): boolean {
  const beggeRett = grunnlag.isBeggeRett();
  const bareFarRett = grunnlag.isBareFarHarRett();
  const bareFarMinsterett = beregnMinsterett(grunnlag);
  return beggeRett || (bareFarRett && !bareFarMinsterett);
}

export function leggTilDagerVedFlereBarn(mellomregning: KontoerMellomregning): void {
  const grunnlag = mellomregning.getGrunnlag();
  const parametertypeFlerbarn = flerbarnsParametertype(grunnlag);

  if (!parametertypeFlerbarn) {
    return;
  }

  mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.TILLEGG_FLERBARN, parametertypeFlerbarn));

  if (aktivitetsKravIkkeMinsterett(grunnlag)) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.FLERBARNSDAGER, parametertypeFlerbarn));
  }
}

export function beregnMinsterett(grunnlag: BeregnKontoerGrunnlag): boolean {
  const konfDato = grunnlag.getKonfigurasjonsvalgdato();
  if (!konfDato) {
    return false;
  }
  return Konfigurasjon.STANDARD.getParameter(Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT, null, konfDato) > 0;
}

export function leggTilDagerVedBareFarRett(mellomregning: KontoerMellomregning): void {
  const grunnlag = mellomregning.getGrunnlag();
  const flerbarn = flerbarnsParametertype(grunnlag);
  const minsterett = beregnMinsterett(grunnlag);

  if (minsterett) {
    if (grunnlag.isMorHarUføretrygd()) {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT));
      if (flerbarn) {
        mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, flerbarn));
      }
    } else if (flerbarn) {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, flerbarn));
    } else {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.BARE_FAR_RETT, Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT));
    }
  } else {
    if (grunnlag.isMorHarUføretrygd()) {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.UFØREDAGER, Parametertype.BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV));
    }
  }
}

export function leggTilMinsterettVedTetteSaker(mellomregning: KontoerMellomregning): void {
  const grunnlag = mellomregning.getGrunnlag();

  if (grunnlag.isMorRett()) {
    if (grunnlag.isGjelderFødsel()) {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL));
    } else {
      mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON));
    }
  }

  if (grunnlag.isFarRett()) {
    mellomregning.getKontokonfigurasjon().push(createKontokonfigurasjon(StønadskontoKontotype.TETTE_SAKER_FAR, Parametertype.FAR_TETTE_SAKER_DAGER));
  }
}

export function opprettKontoer(mellomregning: KontoerMellomregning): void {

  const grunnlag = mellomregning.getGrunnlag();
  const kontokonfigurasjoner = mellomregning.getKontokonfigurasjon();
  const kontoerMap: Map<StønadskontoKontotype, number> = new Map();

  for (const konfigListe of kontokonfigurasjoner) {
    const konto = konfigListe.stønadskontotype;
    const parametertype = konfigListe.parametertype;

    const value = hentParameter(konto, parametertype, grunnlag);
    const existing = kontoerMap.get(konto) || 0;
    kontoerMap.set(konto, existing + value);
  }

  const tilleggPrematur = kontoerMap.get(StønadskontoKontotype.TILLEGG_PREMATUR) || 0;
  const tilleggFlerbarn = kontoerMap.get(StønadskontoKontotype.TILLEGG_FLERBARN) || 0;

  if (tilleggFlerbarn + tilleggPrematur > 0) {
    const fellesperiode = kontoerMap.get(StønadskontoKontotype.FELLESPERIODE) || 0;
    if (fellesperiode > 0) {
      kontoerMap.set(StønadskontoKontotype.FELLESPERIODE, fellesperiode + tilleggFlerbarn + tilleggPrematur);
    }
    const foreldrepenger = kontoerMap.get(StønadskontoKontotype.FORELDREPENGER) || 0;
    if (foreldrepenger > 0) {
      kontoerMap.set(StønadskontoKontotype.FORELDREPENGER, foreldrepenger + tilleggFlerbarn + tilleggPrematur);
    }
  }

  const beregnet = mellomregning.getBeregnet();
  for (const [key, value] of kontoerMap.entries()) {
    if (value > 0) {
      beregnet.set(key, value);
    }
  }
}

function hentParameter(konto: StønadskontoKontotype, parametertype: Parametertype | null, grunnlag: BeregnKontoerGrunnlag): number {
  if (konto === StønadskontoKontotype.TILLEGG_PREMATUR) {
    const fødselsdato = grunnlag.getFødselsdato();
    const termindato = grunnlag.getTermindato();
    if (!fødselsdato || !termindato) {
      return 0;
    }
    return beregnPrematurdager(fødselsdato, termindato);
  }

  if (!parametertype) {
    return 0;
  }

  const konfDato = grunnlag.getKonfigurasjonsvalgdato();
  if (!konfDato) {
    return 0;
  }
  return Konfigurasjon.STANDARD.getParameter(parametertype, grunnlag.getDekningsgrad(), konfDato);
}

export function ferdigBeregnetKontoer(mellomregning: KontoerMellomregning): void {
  const beregnet = mellomregning.getBeregnet();

  if (Array.from(beregnet.values()).some(v => v <= 0)) {
    const midlertidig = new Map<StønadskontoKontotype, number>();
    for (const [key, value] of beregnet.entries()) {
      if (value > 0) {
        midlertidig.set(key, value);
      }
    }
    beregnet.clear();
    for (const [key, value] of midlertidig.entries()) {
      beregnet.set(key, value);
    }
  }
}

