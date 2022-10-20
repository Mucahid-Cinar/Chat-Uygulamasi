package com.mucahid.mm_chat;


import androidx.annotation.Keep;

import java.io.Serializable;
@Keep
public class Mesaj {
        String kullaniciAdi;
        String kullaniciId;
        String mesaj;
        String tarih;
        String zaman;

    public Mesaj() {
    }

    public Mesaj(String kullaniciAdi, String kullaniciId, String mesaj, String tarih, String zaman) {
            this.kullaniciAdi = kullaniciAdi;
            this.kullaniciId = kullaniciId;
            this.mesaj = mesaj;
            this.tarih = tarih;
            this.zaman = zaman;
        }
    }
