package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettKontoer.ID)
class OpprettKontoer extends LeafSpecification<BeregnKontoerGrunnlag> {

    private static final String KONTOER = "KONTOER";
    private static final String ANTALL_FLERBARN_DAGER = "ANTALL_FLERBARN_DAGER";
    private static final String ANTALL_PREMATUR_DAGER = "ANTALL_PREMATUR_DAGER";

    private final List<Kontokonfigurasjon> kontokonfigurasjoner;
    public static final String ID = "Opprett kontoer";

    OpprettKontoer(List<Kontokonfigurasjon> kontokonfigurasjoner) {
        super(ID);
        this.kontokonfigurasjoner = kontokonfigurasjoner;
    }

    @Override
    public Evaluation evaluate(BeregnKontoerGrunnlag grunnlag) {
        if (kontokonfigurasjoner.isEmpty()) {
            return manglerOpptjening();
        }
        Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap = new EnumMap<>(StønadskontoBeregningStønadskontotype.class);
        var antallPrematurDager = 0;

        // Finn antall ekstra dager først.
        var antallExtraBarnDager = finnEkstraFlerbarnsdager(grunnlag);

        // Opprette alle kontoer utenom samtidig uttak
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.stønadskontotype() != StønadskontoBeregningStønadskontotype.FLERBARNSDAGER) {
                var antallDager = Konfigurasjon.STANDARD.getParameter(kontokonfigurasjon.parametertype(), grunnlag.getDekningsgrad(), grunnlag.getFamiliehendelsesdato());
                antallDager += getFlerbarnsdager(grunnlag, kontoerMap, antallExtraBarnDager, kontokonfigurasjon);
                if (kontotypeSomKanHaEkstraFlerbarnsdager(kontokonfigurasjon) && skalLeggeTilPrematurUker(grunnlag)) {
                    antallPrematurDager = antallVirkedagerFomFødselTilTermin(grunnlag);
                    antallDager += antallPrematurDager;
                }
                kontoerMap.put(kontokonfigurasjon.stønadskontotype(), antallDager);
            }
        }
        return beregnetMedResultat(kontoerMap, antallExtraBarnDager, antallPrematurDager);
    }

    private int getFlerbarnsdager(BeregnKontoerGrunnlag grunnlag,
                                      Map<StønadskontoBeregningStønadskontotype, Integer> kontoerMap,
                                      int antallExtraBarnDager,
                                      Kontokonfigurasjon kontokonfigurasjon) {
        if (antallExtraBarnDager == 0) {
            return 0;
        }
        // Legg ekstra dager til foreldrepenger eller fellesperiode.
        if ((kontokonfigurasjon.stønadskontotype().equals(StønadskontoBeregningStønadskontotype.FORELDREPENGER))) {
            if (kunFarRettIkkeAleneomsorgFlerbarnsdager(grunnlag) && !grunnlag.isMinsterett()) {
                kontoerMap.put(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
            }
            return antallExtraBarnDager;
        } else if (kontokonfigurasjon.stønadskontotype().equals(StønadskontoBeregningStønadskontotype.FELLESPERIODE)) {
            kontoerMap.put(StønadskontoBeregningStønadskontotype.FLERBARNSDAGER, antallExtraBarnDager);
            return antallExtraBarnDager;
        } else {
            return 0;
        }
    }

    private boolean kontotypeSomKanHaEkstraFlerbarnsdager(Kontokonfigurasjon kontokonfigurasjon) {
        return kontokonfigurasjon.stønadskontotype().equals(StønadskontoBeregningStønadskontotype.FELLESPERIODE)
                || kontokonfigurasjon.stønadskontotype().equals(StønadskontoBeregningStønadskontotype.FORELDREPENGER);
    }

    private int finnEkstraFlerbarnsdager(BeregnKontoerGrunnlag grunnlag) {
        for (var kontokonfigurasjon : kontokonfigurasjoner) {
            if (kontokonfigurasjon.stønadskontotype() == StønadskontoBeregningStønadskontotype.FLERBARNSDAGER) {
                return Konfigurasjon.STANDARD.getParameter(kontokonfigurasjon.parametertype(), grunnlag.getDekningsgrad(), grunnlag.getFamiliehendelsesdato());
            }
        }
        return 0;
    }

    private int antallVirkedagerFomFødselTilTermin(BeregnKontoerGrunnlag grunnlag) {
        //Fra termin, ikke inkludert termin
        return PrematurukerUtil.beregnAntallVirkedager(grunnlag.getFødselsdato().orElseThrow(),
                grunnlag.getTermindato().orElseThrow().minusDays(1));
    }

    private boolean skalLeggeTilPrematurUker(BeregnKontoerGrunnlag grunnlag) {
        if (!grunnlag.erFødsel()) {
            return false;
        }

        var fødselsdato = grunnlag.getFødselsdato();
        var termindato = grunnlag.getTermindato();
        return PrematurukerUtil.oppfyllerKravTilPrematuruker(fødselsdato.orElse(null), termindato.orElse(null));
    }

    private boolean kunFarRettIkkeAleneomsorgFlerbarnsdager(BeregnKontoerGrunnlag grunnlag) {
        return grunnlag.isFarRett() && !grunnlag.isMorRett() && !grunnlag.isFarAleneomsorg() && grunnlag.getAntallBarn() > 0;
    }

    private Evaluation beregnetMedResultat(Map<StønadskontoBeregningStønadskontotype, Integer> kontoer,
                                           Integer antallExtraBarnDager,
                                           Integer antallPrematurDager) {
        var outcome = new KontoOutcome(kontoer)
            .medAntallExtraBarnDager(antallExtraBarnDager)
            .medAntallPrematurDager(antallPrematurDager);
        var eval = ja(outcome);
        eval.setEvaluationProperty(KONTOER, kontoer);
        eval.setEvaluationProperty(ANTALL_FLERBARN_DAGER, antallExtraBarnDager);
        eval.setEvaluationProperty(ANTALL_PREMATUR_DAGER, antallPrematurDager);

        return eval;
    }

    private Evaluation manglerOpptjening() {
        var utfall = KontoOutcome.ikkeOppfylt("Hverken far eller mor har opptjent rett til foreldrepenger.");
        return nei(utfall);
    }
}
