package no.nav.foreldrepenger.stønadskonto.regelmodell;

public enum StønadskontoKontotype {
    FELLESPERIODE(KontoKategori.STØNADSDAGER),
    MØDREKVOTE(KontoKategori.STØNADSDAGER),
    FEDREKVOTE(KontoKategori.STØNADSDAGER),
    FORELDREPENGER(KontoKategori.STØNADSDAGER),
    FORELDREPENGER_FØR_FØDSEL(KontoKategori.STØNADSDAGER),

    TILLEGG_FLERBARN(KontoKategori.UTVIDELSE), // Jfr Ftl 14-9
    TILLEGG_PREMATUR(KontoKategori.UTVIDELSE), // Jfr Ftl 14-10 a

    FLERBARNSDAGER(KontoKategori.AKTIVITETSKRAV), // Kan tas uten krav til aktivitet, jfr Ftl 14-13
    UFØREDAGER(KontoKategori.AKTIVITETSKRAV), // Kan tas uten krav til aktivitet, jfr Ftl 14-13. Før WLB-direktiv

    TETTE_SAKER_MOR(KontoKategori.MINSTERETT),
    TETTE_SAKER_FAR(KontoKategori.MINSTERETT),
    BARE_FAR_RETT(KontoKategori.MINSTERETT),

    FAR_RUNDT_FØDSEL(KontoKategori.ANNET)
    ;

    public enum KontoKategori { STØNADSDAGER, UTVIDELSE, AKTIVITETSKRAV, MINSTERETT, ANNET }

    private final KontoKategori kontoKategori;

    StønadskontoKontotype(KontoKategori kategori) {
        this.kontoKategori = kategori;
    }

    public KontoKategori getKontoKategori() {
        return kontoKategori;
    }
}
