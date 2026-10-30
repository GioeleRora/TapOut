# 01_ARCHITECT_AGENT — Architettura, Build & Data Layer

## Ruolo
Definisce e mantiene la struttura architetturale complessiva di **TapOut** seguendo i principi di Clean Architecture e MVVM, configurando le dipendenze Gradle per Android 15 (API 35), `AndroidManifest.xml` con tutti i permessi di sistema e la persistenza locale (Room Database + Jetpack DataStore).

---

## 1. Stack Tecnologico
- **Linguaggio:** Kotlin 2.0.21 con Coroutine e Kotlin Flow.
- **UI:** Jetpack Compose con Material 3 (Tema Cyber Dark con accenti Neon Orange & Purple).
- **Target Platform:** `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 35` (Android 15), `compileSdk = 35`.
- **Computer Vision:** CameraX 1.4.0 (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) + Google ML Kit Pose Detection (`com.google.mlkit:pose-detection:18.0.0-beta3`).
- **Database:** Room Database 2.6.1 con annotazioni KSP per entità e DAO.
- **DataStore:** Jetpack DataStore Preferences 1.1.1 per impostazioni globali e configurazione tag NFC.

---

## 2. Struttura dei Package (`com.tapout`)
```text
com.tapout
├── TapOutApplication.kt           // Setup canali di notifica ad alta priorità e Room Singleton
├── data
│   ├── local
│   │   ├── AppDatabase.kt         // Database Room con AlarmDao, BlockedAppDao, BlockScheduleDao
│   │   ├── dao
│   │   │   ├── AlarmDao.kt
│   │   │   ├── BlockedAppDao.kt
│   │   │   └── BlockScheduleDao.kt
│   │   └── entity
│   │       ├── AlarmEntity.kt
│   │       ├── BlockedAppEntity.kt
│   │       └── BlockScheduleEntity.kt
│   ├── repository
│   │   ├── SettingsRepository.kt  // DataStore: UID tag registrato, test mode, flag onboarding
│   │   └── BillingManager.kt      // Gestione stato Freemium/Premium e Feature Flags
├── alarm
│   ├── AlarmScheduler.kt          // Programmazione esatta con AlarmManager.setAlarmClock()
│   ├── AlarmReceiver.kt           // BroadcastReceiver di scatto sveglia con WakeLock
│   └── BootReceiver.kt            // Ripristino sveglie post-riavvio (BOOT_COMPLETED)
├── service
│   ├── AlarmService.kt            // Foreground Service con notifica ad alta priorità e fullScreenIntent
│   ├── AudioGuardManager.kt       // Forzatura volume STREAM_ALARM al 100%, anti-silenziamento
│   ├── ToneGeneratorHelper.kt     // Riproduzione suoneria o sintesi tono d'emergenza in loop
│   └── AppBlockerAccessibilityService.kt // Monitoraggio app in tempo reale e overlay di blocco
├── nfc
│   └── NfcHelper.kt               // enableReaderMode, estrazione UID esadecimale e comparazione
├── vision
│   ├── DanceDetector.kt           // Calcolo varianza cinematica dei landmark scheletrici
│   └── DancePoseAnalyzer.kt       // ImageAnalysis.Analyzer collegato a ML Kit PoseDetector
└── ui
    ├── theme
    │   ├── Color.kt               // Palette Cyber Dark, Electric Orange, Electric Purple
    │   ├── Theme.kt               // Sveglia Material3 theme wrapper
    │   └── Type.kt                // Tipografia ad alto contrasto
    └── screens
        ├── dashboard              // Lista sveglie e creazione
        ├── blocker                // Gestione app bloccate, orari e blocco istantaneo
        ├── alarmactive            // Schermata allarme a tutto schermo (orologio, NFC, Dance trigger)
        ├── dance                  // Schermata Dance Mission con preview CameraX e ring progress
        ├── settings               // Gestione Tag NFC, simulatore test mode, Billing/Premium
        └── permissions            // Wizard interattivo per la concessione di tutti i permessi
```

---

## 3. Permessi in `AndroidManifest.xml`
- `android.permission.CAMERA`: Obbligatorio per CameraX e Dance Mission on-device.
- `android.permission.NFC`: Per la lettura dei tag NFC hardware.
- `android.permission.SCHEDULE_EXACT_ALARM` & `USE_EXACT_ALARM`: Per la precisione della sveglia.
- `android.permission.USE_FULL_SCREEN_INTENT`: Per risvegliare lo schermo sopra il lockscreen.
- `android.permission.FOREGROUND_SERVICE`: Inclusi i tipi specifici per Android 14/15:
  - `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
  - `FOREGROUND_SERVICE_SPECIAL_USE`
  - `FOREGROUND_SERVICE_CAMERA`
- `android.permission.WAKE_LOCK`: Per impedire alla CPU di andare in sleep mode.
- `android.permission.RECEIVE_BOOT_COMPLETED`: Per il ripristino delle sveglie attive al boot.
- `android.permission.VIBRATE`: Vibrazione sincronizzata con l'allarme.
- `android.permission.POST_NOTIFICATIONS`: Per notifiche push su Android 13+.
- `android.permission.SYSTEM_ALERT_WINDOW`: Per overlay di blocco immediato.
- `android.permission.QUERY_ALL_PACKAGES`: Per mostrare all'utente l'elenco delle app installate da bloccare.
- `android.permission.BIND_ACCESSIBILITY_SERVICE`: Per il servizio di blocco app.
