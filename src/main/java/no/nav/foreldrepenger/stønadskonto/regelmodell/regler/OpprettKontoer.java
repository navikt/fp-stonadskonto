package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.rettighet.PrematurukerUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.3";
    private static final String DESC = "Opprett kontoer";

    OpprettKontoer() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var kontokonfigurasjoner = mellomregning.getKontokonfigurasjon();
        Map<StønadskontoKontotype, Integer> kontoerMap = kontokonfigurasjoner.stream()
            .collect(Collectors.groupingBy(Kontokonfigurasjon::stønadskontotype,
                    Collectors.reducing(0, k -> hentParameter(k.stønadskontotype(), k.parametertype(), grunnlag), Integer::sum)));

        var tilleggPrematur = kontoerMap.getOrDefault(StønadskontoKontotype.TILLEGG_PREMATUR, 0);
        var tilleggFlerbarn = kontoerMap.getOrDefault(StønadskontoKontotype.TILLEGG_FLERBARN, 0);

        // Legg til utvidelser / tilleggsdager på fellesperiode eller foreldrepenger
        if (tilleggFlerbarn + tilleggPrematur > 0) {
            kontoerMap.computeIfPresent(StønadskontoKontotype.FELLESPERIODE, (k, v) -> tilleggFlerbarn + tilleggPrematur + v);
            kontoerMap.computeIfPresent(StønadskontoKontotype.FORELDREPENGER, (k, v) -> tilleggFlerbarn + tilleggPrematur + v);
        }

        var beregnet = mellomregning.getBeregnet();
        kontoerMap.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .forEach(e -> beregnet.put(e.getKey(), e.getValue()));

        return ja();
    }

    private static Integer hentParameter(StønadskontoKontotype konto, Parametertype parametertype, BeregnKontoerGrunnlag grunnlag) {
        if (StønadskontoKontotype.TILLEGG_PREMATUR.equals(konto)) {
            return antallVirkedagerFomFødselTilTermin(grunnlag);
        }
        return Konfigurasjon.STANDARD.getParameter(parametertype, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato());
    }

    private static int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        return PrematurukerUtil.beregnPrematurdager(grunnlag.getFødselsdato().orElseThrow(), grunnlag.getTermindato().orElseThrow());
    }

}
