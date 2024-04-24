package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

/*
 * Format brukt fom medio juni 2019 fram til ultimo april 2024.
 * Kan skille v0/v1 på om det finnes felt "familiehendelsesdato"
 */
@RuleDocumentationGrunnlag
public class LegacyGrunnlagV1 {

    /*
     * Migreringsformål - lese opp gamle regelinput's
     */
    private int antallBarn;
    private boolean morRett;
    private boolean farRett;
    private Dekningsgrad dekningsgrad;
    private boolean farAleneomsorg;
    private boolean morAleneomsorg;
    private boolean farHarRettEØS = false;
    private boolean morHarRettEØS = false;
    private LocalDate fødselsdato;
    private LocalDate termindato;
    private LocalDate omsorgsovertakelseDato;
    private boolean minsterett = false; // brukt bare for å unngå flerbarnsdager ved minsterett BFHR

    private LegacyGrunnlagV1() {
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public boolean isMorRett() {
        return morRett || morHarRettEØS;
    }

    public boolean isFarRett() {
        return farRett || farHarRettEØS;
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
        return fødselsdato != null || termindato != null;
    }

    public boolean isMinsterett() {
        return minsterett;
    }

    public Optional<LocalDate> getFødselsdato() {
        return Optional.ofNullable(fødselsdato);
    }

    public Optional<LocalDate> getTermindato() {
        return Optional.ofNullable(termindato);
    }

    public LocalDate getFamiliehendelsesdato() {
        if (omsorgsovertakelseDato != null) {
            return omsorgsovertakelseDato;
        }
        var fd = getFødselsdato();
        var td = getTermindato().orElse(null);
        return fd.orElse(td);
    }


}

