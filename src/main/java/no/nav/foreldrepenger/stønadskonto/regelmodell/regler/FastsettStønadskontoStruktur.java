package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStønadskontoStruktur.ID)
public class FastsettStønadskontoStruktur extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.1";

    private final Konfigurasjonsfaktorer.Berettiget berettiget;

    public FastsettStønadskontoStruktur(Konfigurasjonsfaktorer.Berettiget berettiget) {
        super(ID);
        this.berettiget = berettiget;
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        if (berettiget != null) {
            mellomregning.getKontokonfigurasjon().addAll(Konfigurasjonsfaktorer.KONFIGURASJONER_FELLES.get(berettiget));
            return ja();
        } else {
            return nei();
        }
    }
}
