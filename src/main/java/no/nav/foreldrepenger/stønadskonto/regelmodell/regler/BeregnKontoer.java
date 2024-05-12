package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som beregner antall stønadsdager for foreldrepenger.
 */
@RuleDocumentation(value = BeregnKontoer.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174837789")
public class BeregnKontoer implements RuleService<KontoerMellomregning> {

    static final String ID = "FP_VK 17";
    private static final String DESC = "Beregn kontoer";

    public BeregnKontoer() {
        //For dokumentasjonsgenerering
    }

    @Override
    public Evaluation evaluer(KontoerMellomregning mellomregning) {
        return getSpecification().evaluate(mellomregning);
    }

    @Override
    public Specification<KontoerMellomregning> getSpecification() {
        var rs = new Ruleset<KontoerMellomregning>();
        return rs.sekvensRegel(BeregnKontoer.ID, DESC)
            .neste(new FastsettStønadskontoStruktur())
            .hvis(new SjekkOmFødsel(), new LeggTilDagerVedFødsel())
            .hvis(new SjekkOmMerEnnEttBarn(), new LeggTilDagerVedFlereBarn())
            .hvis(new SjekkOmBareFarHarRett(), new LeggTilDagerVedBareFarRett())
            .hvis(new SjekkOmTetteSaker(), new LeggTilMinsterettVedTetteSaker())
            .neste(new OpprettKontoer())
            .hvis(new SjekkOmTidligereBeregnet(), new FletteKontoer())
            .siste(new FerdigBeregnetKontoer());
    }

}

