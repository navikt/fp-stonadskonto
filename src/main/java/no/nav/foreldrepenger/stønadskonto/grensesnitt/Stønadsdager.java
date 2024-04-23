package no.nav.foreldrepenger.stønadskonto.grensesnitt;

import java.time.LocalDate;

import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Brukerrolle;
import no.nav.foreldrepenger.stønadskonto.regelmodell.grunnlag.Dekningsgrad;

public interface Stønadsdager {

    static Stønadsdager instance(LocalDate regelverksdato) {
        return new StønadsdagerImpl(regelverksdato);
    }


    int ekstradagerPrematur(LocalDate fødselsdato, LocalDate termindato);

    int ekstradagerFlerbarn(LocalDate familieHendelseDato, int antallBarn, Dekningsgrad dekningsgrad);

    int andredagerFarRundtFødsel(LocalDate hendelsedato, boolean gjelderFødsel);

    int minsterettTetteFødsler(Brukerrolle brukerrolle, boolean gjelderFødsel, LocalDate familiehendelse, LocalDate familiehendelseNesteSak);

    int minsterettBareFarRett(LocalDate familiehendelse, int antallBarn, boolean bareFarRett, boolean morAvklartUfør, Dekningsgrad dekningsgrad);

    int aktivitetskravUføredager(LocalDate familiehendelse, boolean bareFarRett, boolean morAvklartUfør, Dekningsgrad dekningsgrad);

}
