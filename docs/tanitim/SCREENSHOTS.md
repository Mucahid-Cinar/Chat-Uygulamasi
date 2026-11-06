# Galeri görsellerini yeniden üretme

Galeri, `ShowcaseScreenshotsTest` ile uygulamanın gerçek yerleşimleri üzerinde oluşturulur. Test; giriş, kayıt, sohbet listesi, birebir konuşma, hesap ayarları ve profil ekranlarını çizer. Kişi ve mesaj verileri yalnızca test belleğinde oluşturulur.

## Gerekenler

- Çalışan bir Android emülatörü veya test cihazı.
- Android SDK içindeki `adb` aracının PATH üzerinde bulunması.
- Galeriyle aynı boyut için 720 × 1600 piksel, 240 dpi ekran yapılandırması.

## PowerShell komutları

Depo kökünden çalıştır:

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

adb shell am instrument -w -e class com.mucahid.mm_chat.ShowcaseScreenshotsTest com.mucahid.mm_chat.test/androidx.test.runner.AndroidJUnitRunner

adb pull /sdcard/Android/data/com.mucahid.mm_chat/files/showcase/. docs/tanitim/assets/screenshots/
```

Birden fazla cihaz bağlıysa `adb` komutlarına `-s CIHAZ_SERI_NUMARASI` ekle. Test APK'sı test cihazında kurulu kalır; ana uygulamanın release paketine dahil edilmez.

## Kontrol listesi

- Altı PNG dosyasının oluştuğunu doğrula.
- Ekran geçişi tamamlanmadan alınmış soluk veya boş görüntüler olmadığını kontrol et.
- Kurgusal isim ve mesajların korunduğundan, gerçek hesap verisi görünmediğinden emin ol.
- Ana README ve tanıtım sayfasındaki göreli görsel yollarını koru.
- Ekranları değiştirdiysen görselleri yeniden üret; eski tasarımı gösteren dosyaları bırakma.

`cover.svg`, uygulamanın renkleri ve marka işareti kullanılarak hazırlanan bağımsız bir vektör kapaktır. Ekran görüntüsü değildir. PNG dosyaları, uygulama ekranlarının emülatörden alınan görüntüleridir.
