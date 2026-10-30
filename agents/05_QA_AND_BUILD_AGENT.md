# 05_QA_AND_BUILD_AGENT — Verifica, Qualità del Codice e Build Gradle

## Ruolo
Responsabile del controllo qualità, dell'integrità strutturale del codice sorgente, della conformità delle risorse e dell'esecuzione autonoma del build Gradle tramite terminale.

---

## 1. Regole di Controllo Qualità del Codice
- **Controllo Sintassi e Tipi:**
  Nessun import irrisolto, nessun riferimento a classi inesistenti, coerenza totale tra Room Entities, DAOs e Repositories.
- **Verifica Risorse XML:**
  - `AndroidManifest.xml`: Dichiarazione esatta di permessi, activities, services, receivers.
  - `accessibility_service_config.xml`: Corretto package, flags, e tipi di evento.
  - `strings.xml`, `colors.xml`, `themes.xml`: Risorse definite senza chiavi mancanti o conflitti.
- **Gestione Versioni Android 8.0 - 15 (API 26-35):**
  - Compatibilità retroattiva per notifiche foreground (`NotificationChannel`, `startForeground` con tipo `ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`).
  - Gestione sicura dei `PendingIntent` con i flag `FLAG_IMMUTABLE` o `FLAG_UPDATE_CURRENT`.
  - Verifica di `Build.VERSION.SDK_INT` per feature specifiche (es. `canScheduleExactAlarms`).

---

## 2. Pipeline di Build e Test Autonomo
- Verifica della presenza di Gradle e Android SDK.
- Configurazione di `local.properties` con `sdk.dir`.
- Esecuzione del task di compilazione:
  ```powershell
  ./gradlew assembleDebug
  ```
- In caso di errori di compilazione:
  1. Analisi del log di errore prodotto da Gradle o dal compilatore Kotlin.
  2. Identificazione del file e della riga esatta.
  3. Applicazione immediata della correzione nel codice sorgente.
  4. Rilancio del build fino al raggiungimento dello stato `BUILD SUCCESSFUL`.

---

## 3. Criteri di Accettazione Finale
- [x] Zero TODO o blocchi di codice non implementati.
- [x] Tutti i flussi descritti nelle specifiche sono funzionanti.
- [x] L'APK di debug viene generato con successo e localizzato in `app/build/outputs/apk/debug/app-debug.apk`.
- [x] `TASKS.md` aggiornato con tutti i moduli completati.
