package no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

public class BeregnMinsterettGrunnlag {

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
    private LocalDate regelvalgsdato;

    private boolean minsterett;
    private boolean morHarUføretrygd;
    private boolean mor;
    private boolean bareFarHarRett;
    private boolean aleneomsorg;
    private boolean gjelderFødsel = true;
    private int antallBarn = 1;
    private Dekningsgrad dekningsgrad;
    private LocalDate familieHendelseDato;
    private LocalDate familieHendelseDatoNesteSak;

    private BeregnMinsterettGrunnlag() {
    }

    public boolean isMinsterett() {
        return minsterett;
    }

    public boolean isMorHarUføretrygd() {
        return morHarUføretrygd;
    }

    public boolean isMor() {
        return mor;
    }

    public boolean isBareFarHarRett() {
        return bareFarHarRett;
    }

    public boolean isAleneomsorg() {
        return aleneomsorg;
    }

    public boolean isGjelderFødsel() {
        return gjelderFødsel;
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public LocalDate getFamilieHendelseDato() {
        return familieHendelseDato;
    }

    public LocalDate getFamilieHendelseDatoNesteSak() {
        return familieHendelseDatoNesteSak;
    }

    public Optional<LocalDate> getRegelvalgsdato() {
        return Optional.ofNullable(regelvalgsdato);
    }

    public LocalDate getKonfigurasjonsvalgdato() {
        return getRegelvalgsdato().orElseGet(this::getFamilieHendelseDato);
    }

    public static class Builder {

        private BeregnMinsterettGrunnlag grunnlag = new BeregnMinsterettGrunnlag();

        public Builder minsterett(boolean minsterett) {
            grunnlag.minsterett =  minsterett;
            return this;
        }

        public Builder morHarUføretrygd(boolean morHarUføretrygd) {
            grunnlag.morHarUføretrygd =  morHarUføretrygd;
            return this;
        }

        public Builder mor(boolean mor) {
            grunnlag.mor =  mor;
            return this;
        }

        public Builder bareFarHarRett(boolean bareFarHarRett) {
            grunnlag.bareFarHarRett =  bareFarHarRett;
            return this;
        }

        public Builder aleneomsorg(boolean aleneomsorg) {
            grunnlag.aleneomsorg =  aleneomsorg;
            return this;
        }

        public Builder gjelderFødsel(boolean gjelderFødsel) {
            grunnlag.gjelderFødsel =  gjelderFødsel;
            return this;
        }

        public Builder antallBarn(int antallBarn) {
            grunnlag.antallBarn =  antallBarn;
            return this;
        }

        public Builder dekningsgrad(Dekningsgrad dekningsgrad) {
            grunnlag.dekningsgrad =  dekningsgrad;
            return this;
        }

        public Builder familieHendelseDato(LocalDate familieHendelseDato) {
            grunnlag.familieHendelseDato =  familieHendelseDato;
            return this;
        }

        public Builder familieHendelseDatoNesteSak(LocalDate familieHendelseDatoNesteSak) {
            grunnlag.familieHendelseDatoNesteSak =  familieHendelseDatoNesteSak;
            return this;
        }

        public BeregnMinsterettGrunnlag build() {
            var o = grunnlag;
            grunnlag = null;
            return o;
        }
    }



}
