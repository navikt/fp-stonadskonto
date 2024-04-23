package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.List;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStønadskontoStruktur.ID)
public class FastsettStønadskontoStruktur extends LeafSpecification<KontoerMellomregning> {

    static final String ID = "FP_VK 17.2.1";
    private static final String DESC = "Velg og sett opp stønadskonto";

    private static final List<Kontokonfigurasjon> KONFIGURASJON_BEGGE = List.of(
        new Kontokonfigurasjon(StønadskontoKontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_DAGER),
        new Kontokonfigurasjon(StønadskontoKontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER),
        new Kontokonfigurasjon(StønadskontoKontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER));

    public FastsettStønadskontoStruktur() {
        super(ID, DESC);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var erMor = Brukerrolle.MOR.equals(mellomregning.getGrunnlag().getBrukerrolle());
        if (mellomregning.getGrunnlag().isBeggeRett()) {
            mellomregning.getKontokonfigurasjon().addAll(KONFIGURASJON_BEGGE);
        } else if (erMor || mellomregning.getGrunnlag().isAleneomsorg()) { // Bare mor rett eller Aleneomsorg (begge foreldre)
            mellomregning.getKontokonfigurasjon().add(getKonfigurasjonForeldrepenger(Parametertype.FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER));
        } else if (mellomregning.getGrunnlag().isBareFarHarRett()) {
            mellomregning.getKontokonfigurasjon().add(getKonfigurasjonForeldrepenger(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER));
        } else {
            throw new IllegalArgumentException("Ukjent kombinasjon av rettighet og rolle");
        }
        return ja();
    }

    private Kontokonfigurasjon getKonfigurasjonForeldrepenger(Parametertype parametertype) {
        return new Kontokonfigurasjon(StønadskontoKontotype.FORELDREPENGER, parametertype);
    }
}
