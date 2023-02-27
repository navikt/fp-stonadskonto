package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoBeregningStønadskontotype;
import no.nav.foreldrepenger.stønadskonto.regelmodell.konfig.Parametertype;

record Kontokonfigurasjon(StønadskontoBeregningStønadskontotype stønadskontotype, Parametertype parametertype) {
}
