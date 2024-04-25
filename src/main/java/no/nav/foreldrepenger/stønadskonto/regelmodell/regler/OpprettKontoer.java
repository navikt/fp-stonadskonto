package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
        Map<StønadskontoKontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoKontotype.class);
        kontokonfigurasjoner.forEach(k -> kontoerMap.put(k.stønadskontotype(), hentParameter(k.stønadskontotype(), k.parametertype(), grunnlag)));

        var tilleggPrematur = kontoerMap.getOrDefault(StønadskontoKontotype.TILLEGG_PREMATUR, 0);
        var tilleggFlerbarn = kontoerMap.getOrDefault(StønadskontoKontotype.TILLEGG_FLERBARN, 0);

        if (tilleggFlerbarn > 0 && harVerdiBareFarRett(kontoerMap)) {
            justerMinsterettBareFarFlerbarn(kontoerMap, tilleggFlerbarn, kontokonfigurasjoner);
        }

        if (tilleggFlerbarn + tilleggPrematur > 0) {
            if (kontoerMap.containsKey(StønadskontoKontotype.FELLESPERIODE)) {
                kontoerMap.put(StønadskontoKontotype.FELLESPERIODE, tilleggFlerbarn + tilleggPrematur + kontoerMap.get(StønadskontoKontotype.FELLESPERIODE));
            } else if (kontoerMap.containsKey(StønadskontoKontotype.FORELDREPENGER)) {
                kontoerMap.put(StønadskontoKontotype.FORELDREPENGER, tilleggFlerbarn + tilleggPrematur + kontoerMap.get(StønadskontoKontotype.FORELDREPENGER));
            }
        }

        var beregnet = mellomregning.getBeregnet();
        kontoerMap.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .forEach(e -> beregnet.put(e.getKey(), e.getValue()));

        return ja();
    }

    private static void justerMinsterettBareFarFlerbarn(Map<StønadskontoKontotype, Integer> kontoerMap, int dagerFlerbarn, List<Kontokonfigurasjon> konfig) {
        var dagerMinsterett = kontoerMap.get(StønadskontoKontotype.BARE_FAR_RETT);
        var ikkeStandardMinsterett = konfig.stream()
            .filter(k -> StønadskontoKontotype.BARE_FAR_RETT.equals(k.stønadskontotype()))
            .anyMatch(k -> !BARE_FAR_RETT_DAGER_MINSTERETT.equals(k.parametertype()));
        // Etter WLB 1: Flerbarn og mor ufør summeres. Ellers teller flerbarnsdagene som minsterett. Situasjonen kan utvikles over tid.
        if (ikkeStandardMinsterett) {
            kontoerMap.put(StønadskontoKontotype.BARE_FAR_RETT, dagerFlerbarn + dagerMinsterett);
        } else {
            kontoerMap.put(StønadskontoKontotype.BARE_FAR_RETT, dagerFlerbarn);
        }
    }

    private static Integer hentParameter(StønadskontoKontotype konto, Parametertype parametertype, BeregnKontoerGrunnlag grunnlag) {
        if (StønadskontoKontotype.TILLEGG_PREMATUR.equals(konto)) {
            return antallVirkedagerFomFødselTilTermin(grunnlag);
        }
        return Konfigurasjon.STANDARD.getParameter(parametertype, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato());
    }

    private static boolean harVerdiBareFarRett(Map<StønadskontoKontotype, Integer> kontoerMap) {
        return kontoerMap.containsKey(StønadskontoKontotype.BARE_FAR_RETT) && kontoerMap.get(StønadskontoKontotype.BARE_FAR_RETT) > 0;
    }

    private static int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        return PrematurukerUtil.beregnPrematurdager(grunnlag.getFødselsdato().orElseThrow(), grunnlag.getTermindato().orElseThrow());
    }

}
