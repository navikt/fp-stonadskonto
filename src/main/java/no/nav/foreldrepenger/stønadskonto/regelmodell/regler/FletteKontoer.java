package no.nav.foreldrepenger.stønadskonto.regelmodell.regler;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import no.nav.foreldrepenger.stønadskonto.regelmodell.StønadskontoKontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FletteKontoer.ID)
class FletteKontoer extends LeafSpecification<KontoerMellomregning> {

    public static final String ID = "FP_VK 17.4";

    FletteKontoer() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(KontoerMellomregning mellomregning) {
        // Bruker gjeldende utregning dersom input er tom og det ikke skal flettes
        if (mellomregning.getGrunnlag().getTidligereUtregning().isEmpty()) {
            return ja();
        }
        Map<StønadskontoKontotype, Integer> beregnet = new EnumMap<>(StønadskontoKontotype.class);
        beregnet.putAll(mellomregning.getBeregnet());
        Map<StønadskontoKontotype, Integer> opprinnelig = new EnumMap<>(StønadskontoKontotype.class);
        opprinnelig.putAll(mellomregning.getGrunnlag().getTidligereUtregning());
        Map<StønadskontoKontotype, Integer> endelig = new EnumMap<>(StønadskontoKontotype.class);

        /*
         * Opp til konsument å sjekke eventuell differanse og lagre unna.
         * - Stønadsdager, utvidelser og flerbarnsdager: Beholder opprinnelig dersom ulik konfig, ellers maxverdi
         * - Dette vil dekke ulikhet i antall barn, prematurdager, etc. Men ved endret dekningsgrad bør det sendes inn tom tidligere beregning
         * - Øvrige retter: Bruk alltid siste utregning.
         */
        var kontovelger = utledKontoVelger(opprinnelig, beregnet);

        Arrays.stream(StønadskontoKontotype.values()).forEach(konto -> {
            var verdi = switch (konto) {
                case FELLESPERIODE, MØDREKVOTE, FEDREKVOTE, FORELDREPENGER, FORELDREPENGER_FØR_FØDSEL,
                    TILLEGG_FLERBARN, TILLEGG_PREMATUR, FLERBARNSDAGER -> kontovelger.apply(opprinnelig.get(konto), beregnet.get(konto));
                case UFØREDAGER, TETTE_SAKER_MOR, TETTE_SAKER_FAR, BARE_FAR_RETT, FAR_RUNDT_FØDSEL -> Optional.ofNullable(beregnet.get(konto));
            };
            verdi.ifPresent(v -> endelig.put(konto, v));
        });

        mellomregning.getBeregnet().clear();
        mellomregning.getBeregnet().putAll(endelig);
        return ja();
    }

    private static BiFunction<Integer, Integer, Optional<Integer>>
    utledKontoVelger(Map<StønadskontoKontotype, Integer> m1,
                             Map<StønadskontoKontotype, Integer> m2) {
        var fellesperiodeBegge = m1.containsKey(StønadskontoKontotype.FELLESPERIODE) &&
            m2.containsKey(StønadskontoKontotype.FELLESPERIODE);
        var foreldrepengerBegge = m1.containsKey(StønadskontoKontotype.FORELDREPENGER) &&
            m2.containsKey(StønadskontoKontotype.FORELDREPENGER);
        if (fellesperiodeBegge) {
            return m1.get(StønadskontoKontotype.FELLESPERIODE) > m2.get(StønadskontoKontotype.FELLESPERIODE)
                ? FletteKontoer::lhs : FletteKontoer::rhs;
        } else if (foreldrepengerBegge) {
            return m1.get(StønadskontoKontotype.FORELDREPENGER) > m2.get(StønadskontoKontotype.FORELDREPENGER)
                ? FletteKontoer::lhs : FletteKontoer::rhs;
        } else {
            return FletteKontoer::lhs;
        }
    }

    private static Optional<Integer> lhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(lhs);
    }

    private static Optional<Integer> rhs(Integer lhs, Integer rhs) {
        return Optional.ofNullable(rhs);
    }

}
