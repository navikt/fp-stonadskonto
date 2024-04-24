package no.nav.foreldrepenger.stønadskonto.regelmodell.konfig;

public enum Parametertype {

    // Stønadskontoer gitt dekningsgrad
    FEDREKVOTE_DAGER,
    MØDREKVOTE_DAGER,
    FELLESPERIODE_DAGER,
    FORELDREPENGER_MOR_ELLER_ALENEOMSORG_DAGER,
    FORELDREPENGER_BARE_FAR_RETT_DAGER,
    FORELDREPENGER_FØR_FØDSEL,

    // Utvidelser av stønadskonto
    EKSTRA_DAGER_TO_BARN,
    EKSTRA_DAGER_TRE_ELLER_FLERE_BARN,

    // Konstruksjoner uten aktivitetskrav
    BARE_FAR_RETT_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV,

    // Rettigheter utenom konti
    BARE_FAR_RETT_DAGER_MINSTERETT,
    BARE_FAR_RETT_MOR_UFØR_DAGER_MINSTERETT,
    MOR_TETTE_SAKER_DAGER_FØDSEL,
    MOR_TETTE_SAKER_DAGER_ADOPSJON,
    FAR_TETTE_SAKER_DAGER,

    // Andre kvanta
    FAR_DAGER_RUNDT_FØDSEL

}
