package com.mucahid.mm_chat;

public class OzelMesaj {
    String Kimden;
    String Mesaj;
    String Mesaj_Turu;
    String Zaman;

    public OzelMesaj() {
    }

    public OzelMesaj(String kimden, String mesaj, String mesaj_Turu, String zaman) {
        this.Kimden = kimden;
        this.Mesaj = mesaj;
        this.Mesaj_Turu = mesaj_Turu;
        this.Zaman = zaman;
    }

    public String getKimden() {
        return Kimden;
    }

    public void setKimden(String kimden) {
        Kimden = kimden;
    }

    public String getMesaj() {
        return Mesaj;
    }

    public void setMesaj(String mesaj) {
        Mesaj = mesaj;
    }

    public String getMesaj_Turu() {
        return Mesaj_Turu;
    }

    public void setMesaj_Turu(String mesaj_Turu) {
        Mesaj_Turu = mesaj_Turu;
    }

    public String getZaman() {
        return Zaman;
    }

    public void setZaman(String zaman) {
        Zaman = zaman;
    }
}


