package com.eliftekin.odemetakip;

public class data {
    String aciklama;
    String tarih;
    String vadetarihi;
    long tutar;
    boolean durum;
    private String documentId;

    public data(String aciklama, String tarih, String vadetarihi, long tutar, boolean durum, String documentId){
        this.aciklama = aciklama;
        this.tarih = tarih;
        this.vadetarihi = vadetarihi;
        this.tutar = tutar;
        this.durum = durum;
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public boolean isChecked() {
        return durum;
    }

    public void setChecked(boolean checked) {
        durum = checked;
    }

}

