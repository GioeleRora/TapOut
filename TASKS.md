# TASKS.md — Registro Granulare delle Attività TapOut

Stato: `[ ]` Non Iniziato | `[~]` In Lavorazione | `[x]` Completato

---

## Modulo 1: Documentazione & Sistema di Agenti
- [x] Creazione `AGENTS.md` (Manifesto e protocollo del team autonomo)
- [x] Creazione `agents/01_ARCHITECT_AGENT.md` (Architettura Clean & Data Layer)
- [x] Creazione `agents/02_ALARM_AND_NFC_AGENT.md` (Motore sveglia & NFC)
- [x] Creazione `agents/03_VISION_AND_DANCE_AGENT.md` (Computer Vision & Dance Mission)
- [x] Creazione `agents/04_APP_BLOCKER_AGENT.md` (Servizio blocco app con accessibilità)
- [x] Creazione `agents/05_UI_AND_MONETIZATION_AGENT.md` (UI Compose & Billing Freemium)
- [x] Creazione `agents/06_QA_AND_BUILD_AGENT.md` (QA e Build automation)
- [x] Aggiornamento `SPECIFICATIONS.md` (Specifiche tecniche architetturali, intra-body vision & 5s timer)
- [x] Aggiornamento `TASKS.md` (Questo file)
- [x] Creazione `PRIVACY_POLICY_TEMPLATE.md` (Informativa privacy Google Play on-device vision)
- [x] Creazione `GUIDA_UTENTE.md` (Guida all'installazione, permessi e Dance Mission)

---

## Modulo 2: Configurazione Gradle & Manifest Android
- [x] Aggiornamento `settings.gradle.kts` e `build.gradle.kts` con nome progetto TapOut
- [x] Configurazione `app/build.gradle.kts` con dipendenze CameraX (1.4.0) e ML Kit Pose Detection (18.0.0-beta3)
- [x] Aggiornamento `AndroidManifest.xml` con tutti i permessi (Camera, NFC, Exact Alarm, Full Screen, Foreground Service, Accessibility, Query All Packages)
- [x] Creazione file di configurazione `accessibility_service_config.xml` per il blocco app
- [x] Configurazione risorse stringhe, colori Cyber Dark e temi XML

---

## Modulo 3: Data Layer (Room Database, DataStore & Billing)
- [x] Creazione entità Room: `AlarmEntity.kt`, `BlockedAppEntity.kt`, `BlockScheduleEntity.kt`
- [x] Creazione DAO: `AlarmDao.kt`, `BlockedAppDao.kt`, `BlockScheduleDao.kt`
- [x] Creazione Database Room singleton: `AppDatabase.kt`
- [x] Creazione `SettingsRepository.kt` (DataStore per UID NFC, test mode, timer blocco immediato)
- [x] Creazione modulo `BillingManager.kt` con feature flags Freemium e simulatore Premium

---

## Modulo 4: Motore Sveglia Inarrestabile & AudioGuard
- [x] Creazione `AlarmScheduler.kt` con `AlarmManager.setAlarmClock()`
- [x] Creazione `AlarmReceiver.kt` con gestione `WakeLock`
- [x] Creazione `BootReceiver.kt` per ripristino allarmi al riavvio
- [x] Creazione `AlarmService.kt` (Foreground Service con notifica ad altissima priorità)
- [x] Creazione `AudioGuardManager.kt` (blocco volume al 100% su `STREAM_ALARM`)
- [x] Creazione `ToneGeneratorHelper.kt` (gestione suonerie personalizzate e sirena di emergenza sintetizzata)

---

## Modulo 5: Lettore NFC Universale & Modalità Test
- [x] Creazione `NfcHelper.kt` con `enableReaderMode` (NTAG, Mifare, ISO14443)
- [x] Conversione UID in esadecimale universale formattato
- [x] Implementazione logica di validazione tag e toggle simulazione test mode

---

## Modulo 6: Computer Vision & Dance Mission (ML Kit On-Device)
- [x] Risoluzione schermo nero fotocamera con `PreviewView.ImplementationMode.COMPATIBLE` (TextureView per Compose/ColorOS)
- [x] Gestione e richiesta automatica del permesso `CAMERA` direttamente in `DanceMissionScreen`
- [x] Riprogettazione completa algoritmo `DanceDetector.kt`:
  * Centro geometrico vincolato al tronco (`torsoCenter`) e scala bi-acromiale (`shoulderSpan`)
  * Filtro zona morta (*noise deadband* al 10%) per azzerare micro-jitter e respiro quando l'utente è fermo
  * Posa di riferimento adattiva (filtro passa-basso EMA): timer fermo al 100% se il soggetto non balla
  * Immunità totale allo scuotimento del dispositivo
  * Riconoscimento del movimento reale di braccia, busto e arti
- [x] Configurazione costante timer `DANCE_REQUIRED_SECONDS = 30f` (ripristinata a 30s)
- [x] Inserimento card di test immediato "Collauda Dance Mission (30s)" nella scheda Impostazioni
- [x] Rispetto rigoroso del principio zero-disk / zero-cloud per la privacy (RAM-only)

---

## Modulo 7: Servizio di Blocco App Fisico & Bugfix UI
- [x] Creazione `AppBlockerAccessibilityService.kt` con ascolto `TYPE_WINDOW_STATE_CHANGED`
- [x] Monitoraggio continuo delle blacklist e orari attivi
- [x] Creazione `BlockerOverlayActivity.kt` per reindirizzamento forzato
- [x] Risoluzione bug layout pulsante "Sblocca" (pill button compatto su singola riga orizzontale)
- [x] Risoluzione glitch visivo rettangolo viola / overflow di vincolo
- [x] Integrazione dello sblocco tramite NFC o Dance Mission a 5 secondi

---

## Modulo 8: Interfaccia Utente Jetpack Compose & Icone Reali
- [x] Creazione `AppIconView.kt` con `LruCache` e caricamento asincrono icone reali dal `PackageManager`
- [x] Visualizzazione icone autentiche in "Seleziona App da Bloccare" e "Applicazioni Bloccate"
- [x] Separazione cromatica Design System: Neon Pink (`#FF2A85`) per Sveglie, Electric Purple (`#7C4DFF`) per Blocco App
- [x] Aggiornamento `MainActivity.kt` con tab bar dinamica in Neon Pink e Viola
- [x] Aggiornamento `AlarmDashboardScreen.kt` e `AlarmEditDialog.kt` in Neon Pink
- [x] Aggiornamento `AppBlockerScreen.kt`, selezione app e fasce orarie in Viola
- [x] Aggiornamento `AlarmActivity.kt` e `AlarmActiveScreen.kt` con orologio in tempo reale e Dance trigger
- [x] Aggiornamento `DanceMissionScreen.kt` con timer da 5 secondi e HUD cinetico
- [x] Aggiornamento `SettingsScreen.kt` e `PermissionsWizardScreen.kt`

---

## Modulo 9: Compilazione Gradle & Validazione Finale
- [x] Controllo integrità del codice, dipendenze ed esecuzione `./gradlew assembleDebug`
- [x] Risoluzione autonoma di eventuali conflitti di libreria o build
- [x] Verifica del file APK finale generato (`app/build/outputs/apk/debug/app-debug.apk`)
---

## Modulo 10: Nuova Modalità Vision "Sorrisi & Baci" (5 + 5)
- [x] Aggiunta dipendenza Google ML Kit Face Detection (`com.google.mlkit:face-detection:16.1.7`)
- [x] Creazione `SmileKissDetector.kt` con analisi geometrica labbra/occhi (pucker ratio $W_{\text{mouth}} / W_{\text{eyes}} \le 0.58$) e classificazione sorriso
- [x] Macchina a stati con isteresi anti-cheat per impedire di mantenere la stessa espressione fissa (reset facciale obbligatorio)
- [x] Creazione `SmileKissAnalyzer.kt` (CameraX ImageAnalysis in RAM volatile, privacy-first)
- [x] Creazione `SmileKissMissionScreen.kt` con anteprima fotocamera, glow reattivo e 5 slot grafici per sorrisi e baci
- [x] Migrazione Database Room v2 ed estensione `AlarmEntity` con campo `requireSmileKiss`
- [x] Propagazione del flag tramite `AlarmScheduler`, `AlarmReceiver`, `AlarmService` e `AlarmActivity`
- [x] Integrazione pulsante dedicato in `AlarmActiveScreen.kt` e toggle in `AlarmEditDialog.kt`
- [x] Inserimento collaudo istantaneo in `SettingsScreen.kt` ("Collauda Sorrisi & Baci (5 + 5)")
- [x] Compilazione ed installazione APK aggiornato sul dispositivo fisico collegato

---

## Modulo 11: Calibrazione Sorrisi/Baci & Metodi Spegnimento Sveglia Dinamici
- [x] Calibrazione Sorrisi in `SmileKissDetector.kt`: soglia minima 0.80, espansione labbra `mouthEyeRatio >= 0.70`, tenuta 4 frame e reset a <= 0.22
- [x] Calibrazione Baci in `SmileKissDetector.kt`: soglia pucker a 0.65 occhi / 0.34 viso, accumulatore morbido contro jitter frame e reset fluido
- [x] Configurazione a uno o più metodi in `AlarmEditDialog.kt` con validazione obbligatoria di almeno un metodo
- [x] Schermata sveglia attiva `AlarmActiveScreen.kt` dinamica: radar NFC contestuale o missioni visive in primo piano con spegnimento garantito con qualunque dei metodi selezionati

---

## Modulo 12: Vibrazione Intensa Permanente & Perfezionamento Baci
- [x] Rimozione selettore vibrazione da `AlarmEditDialog.kt`: vibrazione intensa permanente e sempre attiva al 100% su tutte le sveglie
- [x] Calibrazione Baci in `SmileKissDetector.kt`: rimossa condizione isolata su bounding box; vincolo combinato forma labbra a "O" ($W/H \le 2.4$ e $W_{\text{mouth}}/W_{\text{eyes}} \le 0.58$), tenuta a 4 frame ed eliminati falsi positivi su viso neutro
- [x] Compilazione verificata ed installazione APK aggiornato sul telefono via USB

---

## Modulo 13: Funzionalità Base/PRO Reali & Wizard Permessi Accorpato
- [x] Differenze e limiti reali Base vs PRO: quota 3 app bloccate su Base con dialog promozionale PRO illimitato; badge `BASE` vs `👑 PRO` in header; tabella comparativa e toggle a tutta card in `SettingsScreen.kt`
- [x] Accorpamento permessi in `PermissionsWizardScreen.kt` in 3 macro-aree logiche + stato NFC, con launcher combinato 1-tap per Fotocamera e Notifiche e pulsante "Configurazione Rapida Continua"
- [x] Compilazione Gradle ed installazione APK verificata su dispositivo fisico via USB

---

## Modulo 14: Pulizia UI Sveglie, Quota Rigorosa Base, Smart Focus Schedule & NFC Auto-Prompt
- [x] Rimozione testo ridondante "Sveglia TapOut" dalle card della dashboard (schermata minimale e leggera)
- [x] Quota rigorosa blocco app: limite di max 3 app complessive imposto in `AppSelectionDialog.kt` e `AppBlockerScreen.kt` per il piano Base
- [x] Calcolo dinamico nome fascia oraria: "Focus Mattutino", "Focus Pomeridiano", "Focus Notturno" e testo vuoto se orari mischiati in `BlockScheduleDialog.kt`
- [x] Accorpamento permessi in sole 2 macro-schede e scheda Hardware Chip NFC separata con design Cyber Radar
- [x] Auto-prompt pannello rapido NFC all'avvio della sveglia se il sensore risulta disattivato (`Settings.Panel.ACTION_NFC`)
- [x] Compilazione verificata ed installazione su dispositivo fisico via USB

---

## Modulo 15: Revoca Selettiva e Gestione Singola Permessi
- [x] Creazione `RevokePermissionsDialog.kt` per la gestione e revoca dei permessi selezionandoli uno ad uno (Fotocamera, Notifiche, Sveglie Esatte, Ottimizzazione Batteria, Overlay, Accessibilità)
- [x] Implementazione revoca autorizzazioni runtime (`revokeSelfPermissionOnKill` su API 33+) e apertura mirata della schermata di impostazioni di sistema specifica per ciascuna autorizzazione
- [x] Inserimento link discreto a caratteri piccoli e non invadente in `PermissionsWizardScreen.kt` e in `SettingsScreen.kt` ("Gestisci o revoca autorizzazioni singolarmente")
- [x] Compilazione verificata ed installazione su dispositivo fisico via USB

---

## Modulo 16: Restyling Tipografia Permessi & Limite a 2 App Bloccabili
- [x] Riprogettazione completa e ad alto contrasto di `PermissionsWizardScreen.kt`:
  * Griglia 2x2 spaziosa per i chip dei permessi di sistema (eliminato ogni accavallamento o troncamento di testo)
  * Gerarchia visiva verticale con titoli chiari in bianco puro (`TextWhite`) e descrizioni in ardesia chiara (`#CBD5E1`)
  * Badging di stato nitido con contrasto elevato
- [x] Totale riscrittura di `RevokePermissionsDialog.kt`:
  * Eliminato il sovraffollamento orizzontale a linea singola
  * Schede verticali spaziose per ciascuna autorizzazione con spiegazione per esteso, badge `🟢 ATTIVO` / `⚪ NON ATTIVO` e pulsante dedicato `🔴 Disattiva nelle Impostazioni ↗`
- [x] Aggiornamento limite app bloccabili nel piano Base da 3 a **2 app massime**:
  * Costante centralizzata `BillingManager.FREE_MAX_BLOCKED_APPS = 2`
  * Controllo e blocco in tempo reale in `AppSelectionDialog.kt`, `AppBlockerScreen.kt` e tabella in `SettingsScreen.kt`
- [x] Compilazione verificata con successo (`BUILD SUCCESSFUL in 42s`) ed installazione su dispositivo fisico USB

---

## Modulo 17: Multi-Tag NFC Fisico (Piano PRO) & Ottimizzazione Motore Scansione Sveglia
- [x] Creazione `NfcTagEntity.kt` e `NfcTagDao.kt` per memorizzare e gestire tag multipli in Room Database (`AppDatabase` v3)
- [x] Gestione Multi-Tag nel `SettingsRepository.kt` e interfaccia grafica in `SettingsScreen.kt` con dialogo promozionale PRO
- [x] Ottimizzazione a zero-latenza di `NfcHelper.kt` con `FLAG_READER_SKIP_NDEF_CHECK`, feedback aptico istantaneo (`vibrateFeedback`), normalizzazione stringhe UID e `enableForegroundDispatch`
- [x] Supporto a `onNewIntent` in `AlarmActivity.kt` e `BlockerOverlayActivity.kt` per catturare i tag anche se recapitati via Intent di sistema
- [x] Messaggio di feedback visuale e aptico in tempo reale in `AlarmActiveScreen.kt` (conferma visiva se il tag scansionato corrisponde o se è sconosciuto)
- [x] Compilazione verificata ed installazione su dispositivo fisico via USB



