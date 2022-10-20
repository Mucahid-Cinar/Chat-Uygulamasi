package com.mucahid.mm_chat.Model;

public class User {

    private String Ad;
    private String KullaniciAdi;
    private String Soyad;
    private String Hakkimda;
    private String Durum;
    private String Resim;
    private String Uid;

    public User() {

    }

    public User(String ad, String kullaniciAdi, String soyad, String hakkimda, String durum, String resim, String uid) {
        Ad = ad;
        KullaniciAdi = kullaniciAdi;
        Soyad = soyad;
        Hakkimda = hakkimda;
        Durum = durum;
        Resim = resim;
        Uid = uid;
    }

    public String getAd() {
        return Ad;
    }

    public void setAd(String ad) {
        Ad = ad;
    }

    public String getKullaniciAdi() {
        return KullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciAdi) {
        KullaniciAdi = kullaniciAdi;
    }

    public String getSoyad() {
        return Soyad;
    }

    public void setSoyad(String soyad) {
        Soyad = soyad;
    }

    public String getHakkimda() {
        return Hakkimda;
    }

    public void setHakkimda(String hakkimda) {
        Hakkimda = hakkimda;
    }

    public String getDurum() {
        return Durum;
    }

    public void setDurum(String durum) {
        Durum = durum;
    }

    public String getResim() {
        return Resim;
    }

    public void setResim(String resim) {
        Resim = resim;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
