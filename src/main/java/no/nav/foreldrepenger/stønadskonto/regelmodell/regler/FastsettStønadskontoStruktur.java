package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.List;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStønadskontoStruktur.ID)
public class FastsettStønadskontoStruktur extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.1";

    private static final List<Kontokonfigurasjon> KONFIGURASJON_BEGGE = List.of(
        new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FELLESPERIODE, Parametertype.FELLESPERIODE_DAGER),
        new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.MØDREKVOTE, Parametertype.MØDREKVOTE_DAGER),
        new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FEDREKVOTE, Parametertype.FEDREKVOTE_DAGER));

    public FastsettStønadskontoStruktur() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var rettighet = mellomregning.getGrunnlag().getRettighetType();
        var erMor = BeregnKontoerGrunnlag.BrukerRolle.MOR.equals(mellomregning.getGrunnlag().getBrukerRolle());
        if (mellomregning.getGrunnlag().isBeggeRett()) {
            mellomregning.getKontokonfigurasjon().addAll(KONFIGURASJON_BEGGE);
        } else if (erMor) {
            mellomregning.getKontokonfigurasjon().add(getKonfigurasjonForeldrepenger(Parametertype.FORELDREPENGER_MOR_ALENEOMSORG_DAGER));
        } else if (mellomregning.getGrunnlag().isFarAleneomsorg()) {
            mellomregning.getKontokonfigurasjon().add(getKonfigurasjonForeldrepenger(Parametertype.FORELDREPENGER_FAR_ALENEOMSORG_DAGER));
        } else if (mellomregning.getGrunnlag().isBareFarHarRett()) {
            mellomregning.getKontokonfigurasjon().add(getKonfigurasjonForeldrepenger(Parametertype.FORELDREPENGER_BARE_FAR_RETT_DAGER));
        } else {
            throw new IllegalArgumentException("Ukjent kombinasjon av rettighet og rolle");
        }
        return ja();
    }

    private Kontokonfigurasjon getKonfigurasjonForeldrepenger(Parametertype parametertype) {
        return new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.FORELDREPENGER, parametertype);
    }
}
