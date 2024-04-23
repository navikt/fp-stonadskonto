package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

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

    public static final String ID = "FP_VK 17.3";

    OpprettKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var kontokonfigurasjoner = mellomregning.getKontokonfigurasjon();
        Map<StønadskontoKontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoKontotype.class);
        kontokonfigurasjoner.forEach(k -> kontoerMap.put(k.stønadskontotype(), hentParameter(k.parametertype(), grunnlag)));

        if (kontoerMap.containsKey(StønadskontoKontotype.TILLEGG_PREMATUR) && kontoerMap.get(StønadskontoKontotype.TILLEGG_PREMATUR) == null) {
            kontoerMap.put(StønadskontoKontotype.TILLEGG_PREMATUR, antallVirkedagerFomFødselTilTermin(grunnlag));
        }

        var tilleggPrematur = Optional.ofNullable(kontoerMap.get(StønadskontoKontotype.TILLEGG_PREMATUR)).orElse(0);
        var tilleggFlerbarn = Optional.ofNullable(kontoerMap.get(StønadskontoKontotype.TILLEGG_FLERBARN)).orElse(0);

        if (tilleggFlerbarn > 0 && harVerdiBareFarRett(kontoerMap)) {
            justerMinsterettBareFarFlerbarn(kontoerMap, grunnlag);
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

    private static void justerMinsterettBareFarFlerbarn(Map<StønadskontoKontotype, Integer> kontoerMap, BeregnKontoerGrunnlag grunnlag) {
        var dagerMinsterett = kontoerMap.get(StønadskontoKontotype.BARE_FAR_RETT);
        var dagerFlerbarn = kontoerMap.get(StønadskontoKontotype.TILLEGG_FLERBARN);
        // Flerbarn og mor ufør summerers. Ellers teller flerbarnsdagene som minsterett
        if (dagerMinsterett > hentParameter(BARE_FAR_RETT_DAGER_MINSTERETT, grunnlag)) {
            kontoerMap.put(StønadskontoKontotype.BARE_FAR_RETT, dagerFlerbarn + dagerMinsterett);
        } else {
            kontoerMap.put(StønadskontoKontotype.BARE_FAR_RETT, dagerFlerbarn);
        }
    }

    private static Integer hentParameter(Parametertype parametertype, BeregnKontoerGrunnlag grunnlag) {
        return Optional.ofNullable(parametertype)
            .map(p -> Konfigurasjon.STANDARD.getParameter(p, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato()))
            .orElse(null);
    }

    private static boolean harVerdiBareFarRett(Map<StønadskontoKontotype, Integer> kontoerMap) {
        return kontoerMap.containsKey(StønadskontoKontotype.BARE_FAR_RETT) &&
            kontoerMap.get(StønadskontoKontotype.BARE_FAR_RETT) > 0;
    }


    private int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        //Fra termin, ikke inkludert termin
        return PrematurukerUtil.beregnAntallVirkedager(grunnlag.getFødselsdato().orElseThrow(),
                grunnlag.getTermindato().orElseThrow().minusDays(1));
    }

}
