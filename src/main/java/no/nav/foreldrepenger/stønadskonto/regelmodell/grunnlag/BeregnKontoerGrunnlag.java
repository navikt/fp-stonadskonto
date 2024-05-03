package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
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
    private final Map<StønadskontoKontotype, Integer> tidligereUtregning = new LinkedHashMap<>();

    private LocalDate regelvalgsdato;
    private Dekningsgrad dekningsgrad;
    private Rettighetstype rettighetstype;
    private Brukerrolle brukerrolle;

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
        return isBeggeRett() || Brukerrolle.MOR.equals(brukerrolle);
    }

    public boolean isFarRett() {
        return isBeggeRett() || !Brukerrolle.MOR.equals(brukerrolle);
    }

    public boolean isBeggeRett() {
        return Rettighetstype.BEGGE_RETT.equals(rettighetstype);
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public Map<StønadskontoKontotype, Integer> getTidligereUtregning() {
        return tidligereUtregning;
    }

    public Rettighetstype getRettighetstype() {
        return rettighetstype;
    }

    public Brukerrolle getBrukerrolle() {
        return brukerrolle;
    }

    public boolean isMorHarUføretrygd() {
        return morHarUføretrygd;
    }

    public LocalDate getFamilieHendelseDatoNesteSak() {
        return familieHendelseDatoNesteSak;
    }

    public boolean isBareFarHarRett() {
        return Rettighetstype.BARE_SØKER_RETT.equals(rettighetstype) && !Brukerrolle.MOR.equals(brukerrolle);
    }

    public boolean isAleneomsorg() {
        return Rettighetstype.ALENEOMSORG.equals(rettighetstype);
    }

    public boolean isGjelderFødsel() {
        return erFødsel();
    }

    public LocalDate getFamilieHendelseDato() {
        return Optional.ofNullable(omsorgsovertakelseDato)
            .or(this::getFødselsdato)
            .or(this::getTermindato)
            .orElse(null);
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregnKontoerGrunnlag kladd = new BeregnKontoerGrunnlag();

        public Builder regelvalgsdato(LocalDate regelvalgsdato) {
            kladd.regelvalgsdato = regelvalgsdato;
            return this;
        }

        public Builder tidligereUtregning(Map<StønadskontoKontotype, Integer> tidligereUtregnet) {
            kladd.tidligereUtregning.putAll(tidligereUtregnet);
            return this;
        }

        public Builder antallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public Builder rettighetType(Rettighetstype rettighetstype) {
            kladd.rettighetstype = rettighetstype;
            return this;
        }

        public Builder brukerRolle(Brukerrolle brukerrolle) {
            kladd.brukerrolle = brukerrolle;
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
            Objects.requireNonNull(kladd.rettighetstype, "rettighetType");
            Objects.requireNonNull(kladd.brukerrolle, "brukerRolle");
            if (kladd.fødselsdato == null && kladd.termindato == null && kladd.omsorgsovertakelseDato == null) {
                throw new IllegalArgumentException("Forventer minst en familiehendelsedato");
            }
            return kladd;
        }
    }
}
