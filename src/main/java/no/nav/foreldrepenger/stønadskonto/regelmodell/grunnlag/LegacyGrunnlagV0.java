package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import java.time.LocalDate;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

/*
 * Format brukt fom oktober 2018 fram til medio juni 20219.
 */

@RuleDocumentationGrunnlag
public class LegacyGrunnlagV0 {

    private boolean erFødsel;
    private int antallBarn;
    private boolean morRett;
    private boolean farRett;
    private Dekningsgrad dekningsgrad;
    private boolean farAleneomsorg;
    private boolean morAleneomsorg;
    private LocalDate familiehendelsesdato;

    private LegacyGrunnlagV0() {
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public boolean isMorRett() {
        return morRett;
    }

    public boolean isFarRett() {
        return farRett;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public boolean isFarAleneomsorg() {
        return farAleneomsorg;
    }

    public boolean isMorAleneomsorg() {
        return morAleneomsorg;
    }

    public boolean erFødsel() {
        return erFødsel;
    }

    public LocalDate getFamiliehendelsesdato() {
        return familiehendelsesdato;
    }


}

