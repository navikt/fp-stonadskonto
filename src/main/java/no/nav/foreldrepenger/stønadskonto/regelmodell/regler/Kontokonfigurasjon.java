package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

record Kontokonfigurasjon(StønadskontoKontotype stønadskontotype, Parametertype parametertype) {
}
