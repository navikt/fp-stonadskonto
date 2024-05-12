package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.regler.BeregnKontoer;
import no.nav.foreldrepenger.stønadskonto.regelmodell.regler.KontoerMellomregning;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;
import no.nav.fpsak.nare.json.JsonOutput;
import no.nav.fpsak.nare.json.NareJsonException;

public class StønadskontoRegelOrkestrering {

    public StønadskontoResultat beregnKontoer(BeregnKontoerGrunnlag grunnlag) {
        var grunnlagJson = toJson(grunnlag);

        if (grunnlag.getDekningsgrad() == null) {
            throw new IllegalArgumentException("Mangler dekningsgrad");
        }
        var mellomregning = new KontoerMellomregning(grunnlag);
        var beregnKontoer = new BeregnKontoer();
        var evaluation = beregnKontoer.evaluer(mellomregning);
        var evaluationJson = EvaluationSerializer.asJson(evaluation, StønadskontoVersion.STØNADSKONTO_VERSION, NareVersion.NARE_VERSION);

        var førFletting = mellomregning.getBeregnet();
        var stønadskontoer = mellomregning.getFlettet().isEmpty() ? førFletting : mellomregning.getFlettet();
        var stønadkontoerBeholdStønadsdager = mellomregning.getFlettetBeholdStønadsdager().isEmpty() ?
            stønadskontoer : mellomregning.getFlettetBeholdStønadsdager();
        var antallFlerbarnsdager = hentAntallFlerbarnsdager(stønadskontoer);
        var antallPrematurDager = hentAntallPrematurDager(stønadskontoer);

        return new StønadskontoResultat(stønadskontoer, stønadkontoerBeholdStønadsdager, førFletting,
            antallFlerbarnsdager, evaluationJson, grunnlagJson, antallPrematurDager);
    }

    private int hentAntallFlerbarnsdager(Map<StønadskontoKontotype, Integer> kontoer) {
        return kontoer.getOrDefault(StønadskontoKontotype.TILLEGG_FLERBARN, 0);
    }

    private int hentAntallPrematurDager(Map<StønadskontoKontotype, Integer> kontoer) {
        return kontoer.getOrDefault(StønadskontoKontotype.TILLEGG_PREMATUR, 0);
    }

    private String toJson(BeregnKontoerGrunnlag grunnlag) {
        try {
            return JsonOutput.asJson(grunnlag);
        } catch (NareJsonException e) {
            throw new KontoRegelFeil("Kunne ikke serialisere regelinput for beregning av stønadskontoer.", e);
        }
    }

}
