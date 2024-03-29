package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Denne implementerer regeltjenesten som beregner antall stønadsdager for foreldrepenger.
 */
@RuleDocumentation(value = BeregnKontoer.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=174837789")
public class BeregnKontoer implements RuleService<BeregnKontoerGrunnlag> {

    public static final String ID = "FP_VK 17";
    private static final String SJEKK_OM_MER_ENN_ETT_BARN = "Sjekk om det er mer enn ett barn?";
    private static final String SJEKK_OM_DET_ER_FØDSEL = "Sjekk om det er fødsel?";
    private static final String SJEKK_OM_TO_BARN = "Sjekk om det er to barn?";


    public BeregnKontoer() {
        //For dokumentasjonsgenerering
    }

    @Override
    public Evaluation evaluer(BeregnKontoerGrunnlag beregnKontoerGrunnlag) {
        return getSpecification().evaluate(beregnKontoerGrunnlag);
    }

    @Override
    public Specification<BeregnKontoerGrunnlag> getSpecification() {
        var rs = new Ruleset<BeregnKontoerGrunnlag>();

        return rs.hvisRegel(SjekkOmMorHarAleneomsorg.ID, "Sjekk om mor har aleneomsorg?")
                .hvis(new SjekkOmMorHarAleneomsorg(),
                        opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder(Konfigurasjonsfaktorer.Berettiget.MOR)))
                .ellers(sjekkFarAleneomsorgNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunFarRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, "Sjekk om kun far har rett til foreldrepenger?")
                .hvis(new SjekkOmBareFarHarRett(),
                    opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder(Konfigurasjonsfaktorer.Berettiget.FAR)))
                .ellers(new OpprettKontoer(List.of()));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkKunMorRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareMorHarRett.ID, "Sjekk om kun mor har rett til foreldrepenger?")
                .hvis(new SjekkOmBareMorHarRett(),
                        opprettKontoer(rs, new Konfigurasjonsfaktorer.Builder(Konfigurasjonsfaktorer.Berettiget.MOR)))
                .ellers(sjekkKunFarRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkBeggeRettNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBådeMorOgFarHarRett.ID, "Sjekk om begge har opptjent rett til foreldrepenger?")
                .hvis(new SjekkOmBådeMorOgFarHarRett(), opprettKontoer(rs,
                        new Konfigurasjonsfaktorer.Builder(Konfigurasjonsfaktorer.Berettiget.BEGGE)))
                .ellers(sjekkKunMorRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkFarAleneomsorgNode(Ruleset<BeregnKontoerGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmFarHarAleneomsorg.ID, "Sjekk om far har aleneomsorg?")
                .hvis(new SjekkOmFarHarAleneomsorg(), opprettKontoer(rs,
                        new Konfigurasjonsfaktorer.Builder(Konfigurasjonsfaktorer.Berettiget.FAR_ALENE)))
                .ellers(sjekkBeggeRettNode(rs));
    }

    private Specification<BeregnKontoerGrunnlag> opprettKontoer(Ruleset<BeregnKontoerGrunnlag> rs,
                                                                Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {

        return rs.hvisRegel(SjekkOmMerEnnEttBarn.ID, SJEKK_OM_MER_ENN_ETT_BARN)
                .hvis(new SjekkOmMerEnnEttBarn(), sjekkOmToBarnNode(rs, konfigfaktorBuilder))
                .ellers(sjekkFødselNode(rs, konfigfaktorBuilder.antallLevendeBarn(1)));
    }

    private OpprettKontoer byggKonfigurasjon(Konfigurasjonsfaktorer faktorer) {

        if (faktorer.getAntallLevendeBarn() == null) {
            throw new IllegalArgumentException("Antall levende barn er ikke oppgitt");
        }
        if (faktorer.erFødsel() == null) {
            throw new IllegalArgumentException("Det er ikke oppgitt om dette gjelder fødsel");
        }
        if (faktorer.getBerettiget() == null) {
            throw new IllegalArgumentException("Berettigede parter er ikke oppgitt");
        }

        // Spesifikke for dekningsgrad
        var konfigurasjoner = byggKonfigurasjonFelles(faktorer);

        // Uavhengig av dekningsgrad
        if (faktorer.erFødsel()) { //NOSONAR
            konfigurasjoner.add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER_FØR_FØDSEL, Parametertype.FORELDREPENGER_FØR_FØDSEL));
        }

        return new OpprettKontoer(konfigurasjoner);
    }

    private List<Kontokonfigurasjon> byggKonfigurasjonFelles(Konfigurasjonsfaktorer faktorer) {
        List<Kontokonfigurasjon> konfigurasjoner = new ArrayList<>(
            Konfigurasjonsfaktorer.KONFIGURASJONER_FELLES.get(faktorer.getBerettiget()));

        int antallBarn = faktorer.getAntallLevendeBarn();
        if (antallBarn == 2) {
            konfigurasjoner.add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TO_BARN));
        } else if (antallBarn >= 3) {
            konfigurasjoner.add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN));
        }
        return konfigurasjoner;
    }


    private Specification<BeregnKontoerGrunnlag> sjekkFødselNode(Ruleset<BeregnKontoerGrunnlag> rs,
                                                                 Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmFødsel.ID, SJEKK_OM_DET_ER_FØDSEL)
                .hvis(new SjekkOmFødsel(), byggKonfigurasjon(konfigfaktorBuilder.erFødsel(true).build()))
                .ellers(byggKonfigurasjon(konfigfaktorBuilder.erFødsel(false).build()));
    }

    private Specification<BeregnKontoerGrunnlag> sjekkOmToBarnNode(Ruleset<BeregnKontoerGrunnlag> rs,
                                                                   Konfigurasjonsfaktorer.Builder konfigfaktorBuilder) {
        return rs.hvisRegel(SjekkOmToBarn.ID, SJEKK_OM_TO_BARN)
                .hvis(new SjekkOmToBarn(), sjekkFødselNode(rs, konfigfaktorBuilder.antallLevendeBarn(2)))
                .ellers(sjekkFødselNode(rs, konfigfaktorBuilder.antallLevendeBarn(3)));
    }

}

