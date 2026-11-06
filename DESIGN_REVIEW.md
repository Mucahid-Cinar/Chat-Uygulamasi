# ME Chat tasarım ve akış kontrolü

## Tasarım

- Açık zemin, koyu yeşil başlık alanı, ortak renk ve tipografi stilleri.
- Yeni vektör logo ve varsayılan avatar; yuvarlatılmış, dokunma geri bildirimli butonlar.
- Etiketli alt sekmeler ve 220 ms ekran geçişleri.
- Kaydırılabilir giriş, kayıt, telefon, profil ve hesap ayarları.
- Ortak kişi kartları, grup satırları, mesaj balonları ve mesaj yazma alanları.
- Liste yükleme, boş sonuç ve hata durumları.

## Düzeltilen akışlar

- E-posta, şifre, telefon ve doğrulama kodu alanlarının istemci doğrulamaları.
- Kayıttan girişe dönüşte yinelenen giriş ekranı açılmaması.
- Telefon doğrulamasında numarayı değiştirme ve yeniden kod isteme yoluna dönüş.
- Başarısız grup gönderiminde, alan hâlâ boşsa mesaj metninin geri getirilmesi.
- Grup oluşturma başarısız olduğunda formun açık kalması ve tekrar deneme.
- Liste satırları yeniden kullanılırken eski kişinin bilgilerinin temizlenmesi.
- Sohbette klavye açıldığında mesaj yazma alanının yeniden boyutlandırılması.

## Doğrulama

- `assembleDebug`: başarılı.
- `testDebugUnitTest`: 17 test, sıfır hata.
- `lintDebug`: başarılı; ölümcül hata yok. Kullanılmayan eski kaynaklar, metinlerin kaynaklara taşınması ve bağımlılık sürümleri gibi uyarılar sürüyor.
- Android 7.0 / API 24 emülatöründe 4 cihaz testi başarılı.
- 19 ekran/bileşen örnek verilerle çizdirildi; giriş, ayarlar, profil, sohbet ve mesaj balonlarının görüntüleri incelendi.
- 480×800 piksel / 240 dpi dar ekran ayarında 3 arayüz testi tekrar başarılı. Emülatör çözünürlüğü ardından eski haline getirildi.

Ekran çizim testi, oturum gerektiren sayfaları örnek yerleşim olarak açar; sunucuya veri yazmaz. Gerçek SMS, fotoğraf yükleme ve iki hesap arasında Firebase üzerinden mesajlaşma bu çalışmada uçtan uca doğrulanmadı. Yeni arayüz açık tema kullanır.

APK: `app/build/outputs/apk/debug/app-debug.apk`

Testler: `app/src/androidTest/java/com/mucahid/mm_chat/PremiumUiTest.java`
