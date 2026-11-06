# Firebase Local Emulator Suite

Auth, Realtime Database ve Storage servislerini bilgisayarda çalıştırır. Uygulamanın debug
sürümü bu yerel servislere bağlanır; gerçek Firebase projesine hiçbir veri yazılmaz ve
Storage (profil resmi) ücretsiz planda da çalışır. `database.rules.json` ve `storage.rules`
emülatörde de uygulanır.

## Gereksinimler
- Node.js 20+ (`node --version`)
- Java 17+ — Android Studio'nun JDK'sı yeterli: `C:\Program Files\Android\Android Studio\jbr`

## Emülatörleri başlatma
Proje kök klasöründe (Git Bash):

```bash
export PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH"
npx -y firebase-tools@15.34.1 emulators:start --only auth,database,storage --project mm-chat-b385c
```

Verileri kapatınca saklamak için sonuna `--import=./emulator-data --export-on-exit` ekleyin.

| Servis | Adres |
|---|---|
| Emulator UI (verileri ve hesapları görme) | http://localhost:4000 |
| Authentication | localhost:9099 |
| Realtime Database | localhost:9000 |
| Storage | localhost:9199 |

## Uygulamayı emülatöre bağlama
Emülatör bağlantısı varsayılan olarak **kapalıdır**; normal derleme gerçek Firebase'i kullanır.

1. **Portları telefona yönlendirin** (Android emülatörü veya USB'li gerçek telefon; emülatör/telefon
   her yeniden başladığında tekrarlayın):

   ```bash
   adb reverse tcp:9099 tcp:9099
   adb reverse tcp:9000 tcp:9000
   adb reverse tcp:9199 tcp:9199
   ```

   Neden gerekli: Realtime Database emülatörü ilk bağlantıda istemciye kendi adresini
   (`127.0.0.1:9000`) bildirir ve SDK sonraki bağlantılarda bu adresi kullanır. `adb reverse` ile
   telefondaki `127.0.0.1` bilgisayara yönlenir. `10.0.2.2` kullanılırsa ilk açılıştan sonra
   veritabanı bağlantısı kurulamaz.

2. **Emülatör modunda derleyin:**
   - **Komut satırı:** `./gradlew assembleDebug -PuseFirebaseEmulator=true`
   - **Android Studio:** `gradle.properties` dosyasına geçici olarak `useFirebaseEmulator=true` ekleyip
     Sync + Run. Gerçek Firebase'e dönmek için satırı silip tekrar çalıştırın.

Release sürümü her zaman gerçek Firebase'e bağlanır.

Not: Kodda veritabanına her zaman `Db.database()` ile, Storage'a `Db.storage()` ile erişin;
`FirebaseDatabase.getInstance()` emülatör ayarı yapılmamış ikinci bir örnek döndürebilir.

## Test hesapları (yalnızca emülatör)
Bu hesaplar sadece yerel Auth emülatöründe vardır; gerçek projede yoktur.

| E-posta | Şifre | Kullanıcı adı |
|---|---|---|
| alice@mechat.test | emulator-test-1 | alice |
| bob@mechat.test | emulator-test-2 | bob |
