const FØRSTE_DATO_PREMATUR = new Date(2019, 6, 1);
const PREMATUR_FØDSEL_DAGER_FØR_TERMIN = 52;

const DAGER_PR_UKE = 7;
const VIRKEDAGER_PR_UKE = 5;
const HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;

export function oppfyllerKravTilPrematuruker(fødselsdato: Date | null, termindato: Date | null): boolean {
  if (!fødselsdato || !termindato || fødselsdato < FØRSTE_DATO_PREMATUR || fødselsdato >= termindato) {
    return false;
  }

  const grenseDate = new Date(fødselsdato.getTime() + PREMATUR_FØDSEL_DAGER_FØR_TERMIN * 24 * 60 * 60 * 1000);
  return grenseDate < termindato;
}

export function beregnPrematurdager(fødselsdato: Date, termindato: Date): number {
  if (!oppfyllerKravTilPrematuruker(fødselsdato, termindato)) {
    return 0;
  }
  return beregnAntallVirkedager(fødselsdato, new Date(termindato.getTime() - 24 * 60 * 60 * 1000));
}

function beregnAntallVirkedager(fom: Date, tom: Date): number {
  if (!fom || !tom) {
    throw new Error('fom and tom cannot be null');
  }
  if (fom > tom) {
    throw new Error(`Utviklerfeil: fom ${fom} kan ikke være etter tom ${tom}`);
  }

  const padBefore = daysBetween(getMondayBefore(fom), fom);
  const padAfter = daysBetween(tom, getSundayAfter(tom));

  const mondayBefore = getMondayBefore(fom);
  const sundayAfter = getSundayAfter(tom);

  const weeksBetween = weeksBetweenDates(mondayBefore, sundayAfter);
  const virkedagerPadded = weeksBetween * VIRKEDAGER_PR_UKE;

  const virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);

  return virkedagerPadded - virkedagerPadding;
}

function getMondayBefore(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}

function getSundayAfter(date: Date): Date {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? 0 : 7);
  return new Date(d.setDate(diff));
}

function daysBetween(date1: Date, date2: Date): number {
  return Math.floor((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24));
}

function weeksBetweenDates(date1: Date, date2: Date): number {
  return Math.floor(daysBetween(date1, date2) / 7);
}

