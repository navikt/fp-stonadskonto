package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Konfigurasjon;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(LeggTilDagerDersomTetteFødsler.ID)
public class LeggTilDagerDersomTetteFødsler extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.2.6";

    public LeggTilDagerDersomTetteFødsler() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        var grunnlag = mellomregning.getGrunnlag();
        var regeldato = grunnlag.getKonfigurasjonsvalgdato();
        var toTette = toTette(regeldato, grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        if (toTette && grunnlag.isMorRett()) {
            if (grunnlag.isGjelderFødsel()) {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL));
            } else {
                mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_MOR, Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON));
            }
        }
        if (toTette && grunnlag.isFarRett()) {
            mellomregning.getKontokonfigurasjon().add(new Kontokonfigurasjon(StønadskontoBeregningStønadskontotype.TETTE_FØDSLER_FAR, Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT));
        }
        return ja();
    }

    private static boolean toTette(LocalDate regeldato, LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null) {
            return false;
        }
        return Konfigurasjon.STANDARD.getIntervallgrenseParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, regeldato)
            .map(grenseToTette -> familieHendelseDato.plus(Period.ofWeeks(grenseToTette)).plusDays(1))
            .filter(grenseToTette -> grenseToTette.isAfter(familieHendelseDatoNesteSak))
            .isPresent();
    }
}
