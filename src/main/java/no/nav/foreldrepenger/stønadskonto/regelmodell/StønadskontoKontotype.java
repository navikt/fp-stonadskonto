package no.nav.foreldrepenger.stønadskonto.regelmodell;

public enum StønadskontoKontotype {

    // Ordinære stønadsdager. Enten foreldrepenger eller kvote+fellesperiode.
    FELLESPERIODE,
    MØDREKVOTE,
    FEDREKVOTE,
    FORELDREPENGER,
    FORELDREPENGER_FØR_FØDSEL, // Kun i tilfelle fødsel

    // Tilleggsdager (de blir lagt til fellesperiode eller foreldrepenger)
    TILLEGG_FLERBARN, // Jfr Ftl 14-9
    TILLEGG_PREMATUR, // Jfr Ftl 14-10 a

    // Uten aktivitetskrav
    FLERBARNSDAGER, // Kan tas uten krav til aktivitet, jfr Ftl 14-12, 14-14, 14-13. Opprettes for begge rett og bare far rett
    UFØREDAGER, // Kan tas uten krav til aktivitet, jfr Ftl 14-14, 14-13. Bare aktuelle før WLB-direktiv aug 2022

    // Minsterett
    TETTE_SAKER_MOR, // Jfr 14-10 tredje ledd
    TETTE_SAKER_FAR, // Jfr 14-10 tredje ledd
    BARE_FAR_RETT, // Jfr Ftl 14-14 som gir minste antall dager. Kan påvirkes av flerbarn og mor uføretrygdet

    // Annet
    FAR_RUNDT_FØDSEL // Gir periode og tillater 200% samtidig uttak Jfr 14-10 sjette ledd
    ;

}
