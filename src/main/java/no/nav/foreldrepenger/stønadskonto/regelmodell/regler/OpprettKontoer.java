package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import static no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype.BARE_FAR_RETT_DAGER_MINSTERETT;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<KontoerMellomregning> {

    private static final String KONTOER = "KONTOER";
    private static final String ANTALL_FLERBARN_DAGER = "ANTALL_FLERBARN_DAGER";
    private static final String ANTALL_PREMATUR_DAGER = "ANTALL_PREMATUR_DAGER";

    public static final String ID = "FP_VK 17.3";

    OpprettKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var kontokonfigurasjoner = mellomregning.getKontokonfigurasjon();
        Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        kontokonfigurasjoner.forEach(k -> kontoerMap.put(k.stønadskontotype(), hentParameter(k.parametertype(), grunnlag)));

        if (kontoerMap.containsKey(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR) && kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR) == null) {
            kontoerMap.put(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR, antallVirkedagerFomFødselTilTermin(grunnlag));
        }

        var tilleggPrematur = Optional.ofNullable(kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_PREMATUR)).orElse(0);
        var tilleggFlerbarn = Optional.ofNullable(kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_FLERBARN)).orElse(0);

        if (tilleggFlerbarn > 0 && harVerdi(kontoerMap, StønadskontoBeregningStønadskontotype.BARE_FAR_RETT)) {
            justerMinsterettBareFarFlerbarn(kontoerMap, grunnlag);
        }


        if (tilleggFlerbarn + tilleggPrematur > 0) {
            if (kontoerMap.containsKey(StønadskontoBeregningStønadskontotype.FELLESPERIODE)) {
                kontoerMap.put(StønadskontoBeregningStønadskontotype.FELLESPERIODE, tilleggFlerbarn + tilleggPrematur + kontoerMap.get(StønadskontoBeregningStønadskontotype.FELLESPERIODE));
            } else if (kontoerMap.containsKey(StønadskontoBeregningStønadskontotype.FORELDREPENGER)) {
                kontoerMap.put(StønadskontoBeregningStønadskontotype.FORELDREPENGER, tilleggFlerbarn + tilleggPrematur + kontoerMap.get(StønadskontoBeregningStønadskontotype.FORELDREPENGER));
            }
        }

        var beregnet = mellomregning.getBeregnet();
        kontoerMap.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .forEach(e -> beregnet.put(e.getKey(), e.getValue()));

        return ja();
    }

    private static void justerMinsterettBareFarFlerbarn(Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap, BeregnKontoerGrunnlag grunnlag) {
        var dagerMinsterett = kontoerMap.get(StønadskontoBeregningStønadskontotype.BARE_FAR_RETT);
        var dagerFlerbarn = kontoerMap.get(StønadskontoBeregningStønadskontotype.TILLEGG_FLERBARN);
        // Flerbarn og mor ufør summerers. Ellers teller flerbarnsdagene som minsterett
        if (dagerMinsterett > hentParameter(BARE_FAR_RETT_DAGER_MINSTERETT, grunnlag)) {
            kontoerMap.put(StønadskontoBeregningStønadskontotype.BARE_FAR_RETT, dagerFlerbarn + dagerMinsterett);
        } else {
            kontoerMap.put(StønadskontoBeregningStønadskontotype.BARE_FAR_RETT, dagerFlerbarn);
        }
    }

    private static Integer hentParameter(Parametertype parametertype, BeregnKontoerGrunnlag grunnlag) {
        if (parametertype == null) {
            return null;
        }
        return Konfigurasjon.STANDARD.getParameter(parametertype, grunnlag.getDekningsgrad(), grunnlag.getKonfigurasjonsvalgdato());
    }

    private static boolean harVerdi(Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap, StønadskontoBeregningStønadskontotype type) {
        return kontoerMap.containsKey(type) && kontoerMap.get(type) > 0;
    }


    private int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        //Fra termin, ikke inkludert termin
        return PrematurukerUtil.beregnAntallVirkedager(grunnlag.getFødselsdato().orElseThrow(),
                grunnlag.getTermindato().orElseThrow().minusDays(1));
    }

}
