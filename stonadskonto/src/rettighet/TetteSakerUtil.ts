export function toTette(regeldato: Date, familieHendelseDato: Date, familieHendelseDatoNesteSak: Date | null): boolean {
  const FØRSTE_DATO_TETTE_SAKER = new Date(2022, 7, 2);
  const TETTE_SAKER_MELLOMROM_UKER = 48 * 7 * 24 * 60 * 60 * 1000;

  if (!familieHendelseDatoNesteSak || regeldato < FØRSTE_DATO_TETTE_SAKER) {
    return false;
  }

  const grenseDate = new Date(familieHendelseDato.getTime() + TETTE_SAKER_MELLOMROM_UKER + 24 * 60 * 60 * 1000);
  return grenseDate > familieHendelseDatoNesteSak;
}

