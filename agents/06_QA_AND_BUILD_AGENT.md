# 06_QA_AND_BUILD_AGENT — Garanzia di Qualità, Validazione & Build

## Ruolo
Verifica la correttezza formale, la coerenza delle dichiarazioni in `AndroidManifest.xml`, l'assenza di riferimenti irrisolti o dipendenze conflittuali, ed esegue la compilazione Gradle da terminale (`./gradlew assembleDebug`) fino al raggiungimento di `BUILD SUCCESSFUL`.

---

## 1. Verifiche di Integrità Preliminari
- **Validazione Manifest:**
  - Tutti i Service (`AlarmService`, `AppBlockerAccessibilityService`) e le Activity (`MainActivity`, `AlarmActivity`, `BlockerOverlayActivity`) devono essere correttamente registrati con gli intent-filter e permessi appropriati.
  - Verifica della configurazione XML per l'`AccessibilityService` in `res/xml/accessibility_service_config.xml`.
- **Verifica Risorse & Stringhe:**
  - Controllo che non vi siano ID, icone o stringhe mancanti nei file XML e Compose.
  - Controllo che non esistano package duplicati o import residui obsoleti.

---

## 2. Compilazione ed Esecuzione Build
- **Script di Compilazione:**
  Esecuzione dell'ambiente Gradle con le variabili d'ambiente corrette:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
  $env:ANDROID_HOME = "C:\Users\gioele\AppData\Local\Android\Sdk"
  .\gradlew.bat assembleDebug
  ```
- **Risoluzione Autonoma:**
  In caso di avvisi o errori di compilazione relativi a Kotlin, KSP o CameraX/ML Kit, interviene istantaneamente modificando i file sorgente fino al superamento del build con codice di uscita 0.
- **Validazione Output:**
  Conferma della generazione del file binario `app-debug.apk` all'interno di `app/build/outputs/apk/debug/`.
