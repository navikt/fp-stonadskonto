import { BeregnKontoerGrunnlag } from './grunnlag/BeregnKontoerGrunnlag';
import { StønadskontoResultat } from './StønadskontoResultat';
import { KontoerMellomregning } from './regler/KontoerMellomregning';
import {
  fastsettStønadskontoStruktur,
  sjekkOmFødsel,
  leggTilDagerVedFødsel,
  sjekkOmMerEnnEttBarn,
  leggTilDagerVedFlereBarn,
  sjekkOmBareFarHarRett,
  leggTilDagerVedBareFarRett,
  sjekkOmTetteSaker,
  leggTilMinsterettVedTetteSaker,
  opprettKontoer,
  ferdigBeregnetKontoer,
} from './regler/Rules';

export class StønadskontoRegelOrkestrering {
  beregnKontoer(grunnlag: BeregnKontoerGrunnlag): StønadskontoResultat {
    if (!grunnlag.getDekningsgrad()) {
      throw new Error('Mangler dekningsgrad');
    }

    const mellomregning = new KontoerMellomregning(grunnlag);

    fastsettStønadskontoStruktur(mellomregning);

    if (sjekkOmFødsel(mellomregning)) {
      leggTilDagerVedFødsel(mellomregning);
    }

    if (sjekkOmMerEnnEttBarn(mellomregning)) {
      leggTilDagerVedFlereBarn(mellomregning);
    }

    if (sjekkOmBareFarHarRett(mellomregning)) {
      leggTilDagerVedBareFarRett(mellomregning);
    }

    if (sjekkOmTetteSaker(mellomregning)) {
      leggTilMinsterettVedTetteSaker(mellomregning);
    }

    opprettKontoer(mellomregning);
    ferdigBeregnetKontoer(mellomregning);

    return new StønadskontoResultat(mellomregning.getBeregnet());
  }
}

