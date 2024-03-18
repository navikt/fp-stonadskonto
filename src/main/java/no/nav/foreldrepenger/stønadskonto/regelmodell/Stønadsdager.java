package no.nav.foreldrepenger.stønadskonto.regelmodell;

import java.time.LocalDate;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public interface Stønadsdager {

    static Stønadsdager instance(LocalDate regelverksdato) {
        return new StønadsdagerImpl(regelverksdato);
    }


    Integer ekstradagerPrematur(LocalDate fødselsdato, LocalDate termindato);

    Integer ekstradagerFlerbarn(LocalDate familieHendelseDato, int antallBarn, Dekningsgrad dekningsgrad);

    //Integer minsterettdagerTetteFødsler(LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak, boolean minsterett, boolean mor, boolean gjelderFødsel);

    //Integer andredagerFarRundtFødsel(LocalDate hendelsedato);

}
