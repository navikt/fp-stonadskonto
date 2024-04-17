package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregnKontoerGrunnlag {

    /*
     * Normalt velges konti ut fra fødselsdato/termindato/omsorgsdato.
     * Normalt vil endringer tre i kraft "for nye tilfelle" - som enten går på familiehendelsedato eller på første uttaksdato.
     * For regler som gjelder begge foreldrene brukes gjerne familiehendelsesdato. Andre regler som er mer individuelt orientert ser gjerne på uttaksdato
     *
     * For tilfelle terminbasert søknad så vil man på søknadstidspunktet ikke nødvendigvis vite om fødselsdato tilsier annet regelverk enn termin.
     * Dessuten trer nye regler først i kraft når dagens dato har passert ikraftredelsesdato for endrete regler - må innvilges på gamle regler inntil nye trer i kraft
     *
     * Parameter regelvalgsdato settes kun når man ønsker å "overstyre" familiehendelsedato for regelvalg og kan brukes i utviklingsmiljø + produksjon fram til ikrafttredelse.
     */
    public enum RettighetType { ALENEOMSORG, BARE_SØKER_RETT, BEGGE_RETT, BEGGE_RETT_EØS}
    public enum BrukerRolle { MOR, FAR, MEDMOR, UKJENT }

    private final Map<StønadskontoBeregningStønadskontotype, Integer> tidligereUtregning = new LinkedHashMap<>();

    private LocalDate regelvalgsdato;
    private Dekningsgrad dekningsgrad;
    private RettighetType rettighetType;
    private BrukerRolle brukerRolle;

    // For utregning av tilleggsdager
    private int antallBarn = 1;
    private LocalDate fødselsdato;
    private LocalDate termindato;
    private LocalDate omsorgsovertakelseDato;

    // For utregning av tilkommet minsterett
    private boolean morHarUføretrygd;
    private LocalDate familieHendelseDatoNesteSak;

    private BeregnKontoerGrunnlag() {
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public boolean isMorRett() {
        return RettighetType.BEGGE_RETT.equals(rettighetType) || RettighetType.BEGGE_RETT_EØS.equals(rettighetType) || BrukerRolle.MOR.equals(brukerRolle);
    }

    public boolean isFarRett() {
        return RettighetType.BEGGE_RETT.equals(rettighetType) || RettighetType.BEGGE_RETT_EØS.equals(rettighetType) || !BrukerRolle.MOR.equals(brukerRolle);
    }

    public boolean isBeggeRett() {
        return RettighetType.BEGGE_RETT.equals(rettighetType) || RettighetType.BEGGE_RETT_EØS.equals(rettighetType);
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public boolean isFarAleneomsorg() {
        return RettighetType.ALENEOMSORG.equals(rettighetType) && !BrukerRolle.MOR.equals(brukerRolle);
    }

    public boolean isMorAleneomsorg() {
        return RettighetType.ALENEOMSORG.equals(rettighetType) && BrukerRolle.MOR.equals(brukerRolle);
    }

    public Map<StønadskontoBeregningStønadskontotype, Integer> getTidligereUtregning() {
        return tidligereUtregning;
    }

    public RettighetType getRettighetType() {
        return rettighetType;
    }

    public BrukerRolle getBrukerRolle() {
        return brukerRolle;
    }

    public boolean isMorHarUføretrygd() {
        return morHarUføretrygd;
    }

    public LocalDate getFamilieHendelseDatoNesteSak() {
        return familieHendelseDatoNesteSak;
    }

    public boolean isBareFarHarRett() {
        return RettighetType.BARE_SØKER_RETT.equals(rettighetType) && !BrukerRolle.MOR.equals(brukerRolle);
    }

    public boolean isBareMorHarRett() {
        return RettighetType.BARE_SØKER_RETT.equals(rettighetType) && BrukerRolle.MOR.equals(brukerRolle);
    }

    public boolean isAleneomsorg() {
        return RettighetType.ALENEOMSORG.equals(rettighetType);
    }

    public boolean isGjelderFødsel() {
        return erFødsel();
    }

    public LocalDate getFamilieHendelseDato() {
        return getFamiliehendelsesdato();
    }

    private Optional<LocalDate> getRegelvalgsdato() {
        return Optional.ofNullable(regelvalgsdato);
    }

    public LocalDate getKonfigurasjonsvalgdato() {
        return getRegelvalgsdato().orElseGet(this::getFamilieHendelseDato);
    }


    public boolean erFødsel() {
        return fødselsdato != null || termindato != null;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregnKontoerGrunnlag kladd = new BeregnKontoerGrunnlag();

        public Builder regelvalgsdato(LocalDate regelvalgsdato) {
            kladd.regelvalgsdato = regelvalgsdato;
            return this;
        }

        public Builder tidligereUtregning(Map<StønadskontoBeregningStønadskontotype, Integer> tidligereUtregnet) {
            kladd.tidligereUtregning.putAll(tidligereUtregnet);
            return this;
        }

        public Builder antallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public Builder rettighetType(RettighetType rettighetType) {
            kladd.rettighetType = rettighetType;
            return this;
        }

        public Builder brukerRolle(BrukerRolle brukerRolle) {
            kladd.brukerRolle = brukerRolle;
            return this;
        }

        public Builder dekningsgrad(Dekningsgrad dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder fødselsdato(LocalDate dato) {
            kladd.fødselsdato = dato;
            return this;
        }

        public Builder termindato(LocalDate dato) {
            kladd.termindato = dato;
            return this;
        }

        public Builder omsorgsovertakelseDato(LocalDate dato) {
            kladd.omsorgsovertakelseDato = dato;
            return this;
        }

        public Builder morHarUføretrygd(boolean morHarUføretrygd) {
            kladd.morHarUføretrygd =  morHarUføretrygd;
            return this;
        }

        public Builder familieHendelseDatoNesteSak(LocalDate familieHendelseDatoNesteSak) {
            kladd.familieHendelseDatoNesteSak = familieHendelseDatoNesteSak;
            return this;
        }

        public BeregnKontoerGrunnlag build() {
            Objects.requireNonNull(kladd.rettighetType, "rettighetType");
            Objects.requireNonNull(kladd.brukerRolle, "brukerRolle");
            if (kladd.fødselsdato == null && kladd.termindato == null && kladd.omsorgsovertakelseDato == null) {
                throw new IllegalArgumentException("Forventer minst en familiehendelsedato");
            }
            return kladd;
        }
    }
}
