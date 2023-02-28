package no.nav.foreldrepenger.stønadskonto.regelmodell;

import no.nav.fpsak.nare.evaluation.summary.EvaluationVersion;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;

public class StønadskontoVersion {

    private StønadskontoVersion() {
    }

    public static final EvaluationVersion STØNADSKONTO_VERSION = NareVersion.readVersionPropertyFor("fp-stonadskonto", "nare/fp-konto-version.properties");


}
