# SPECIFICATIONS.md — Specifiche Tecniche & Architetturali di TapOut

## 1. Visione del Prodotto
**TapOut** è una sveglia inarrestabile combinata con un App Blocker fisico basato su Tag NFC standard e una missione di risveglio cinetica basata su Computer Vision: la **Dance Mission** (impostata a 5 secondi in test mode) elaborata al 100% on-device tramite Google ML Kit Pose Detection con riconoscimento del movimento intra-corporeo (anti-shaking).

---

## 2. Design System & Separazione Cromatica
- **Modulo Sveglie (Alarms):** Tonalità dominante **Neon Pink / Rosa Elettrico** (`#FF2A85` / `#FF529A`). Caratterizza l'orologio, i pulsanti della sveglia, i selettori orari e il tab "Sveglie".
- **Modulo Blocco App (App Blocker):** Tonalità dominante **Electric Purple / Viola Vibrante** (`#7C4DFF` / `#9E77FF`). Caratterizza lo scudo, le fasce orarie, le app bloccate e il pill button "Sblocca".
- **Sfondo & Superfici:** Dark OLED profondo (`#08090C`, `#12141A`, `#1B1E28`).
- **Icone Applicazioni Reali:** Modulo `AppIconView` con cache in memoria `LruCache` e caricamento asincrono su `Dispatchers.IO` per estrarre le vere icone dal `PackageManager`.

---

## 3. Modello Dati e Persistenza (Room & DataStore)

### 3.1 Entità Room

#### `AlarmEntity`
Tabella per la memorizzazione delle sveglie configurate dall'utente:
- `id: Long` (Primary Key, autoGenerate)
- `hour: Int` (0-23)
- `minute: Int` (0-59)
- `label: String` (es. "Sveglia Mattina")
- `daysOfWeek: String` (es. "1,2,3,4,5" per lun-ven, o stringa vuota per sveglia singola)
- `isEnabled: Boolean` (true se attiva)
- `vibrate: Boolean` (true per abilitare vibrazione martellante)
- `soundUri: String` (URI del file audio scelto o stringa vuota per suoneria di fallback)
- `requireNfc: Boolean` (true se richiede scansione NFC)
- `requireDance: Boolean` (true se richiede Dance Mission)

#### `BlockedAppEntity`
Tabella per le applicazioni incluse nella blacklist dell'App Blocker:
- `packageName: String` (Primary Key)
- `appName: String` (Nome visualizzato)
- `isBlocked: Boolean` (true se attualmente soggetta a blocco)
- `category: String` (Distrazione)

#### `BlockScheduleEntity`
Tabella per le fasce orarie programmate del Blocco App:
- `id: Long` (Primary Key, autoGenerate)
- `startHour: Int`
- `startMinute: Int`
- `endHour: Int`
- `endMinute: Int`
- `daysOfWeek: String` (Giorni attivi)
- `isEnabled: Boolean`
- `label: String`

### 3.2 DataStore Preferences (`SettingsRepository`)
- `REGISTERED_NFC_UID: String` — UID esadecimale del tag NFC registrato.
- `TAG_NAME: String` — Nome amichevole (es. "Tag Bagno").
- `TEST_MODE_ENABLED: Boolean` — Se true, consente la simulazione del tap NFC a schermo.
- `ONBOARDING_COMPLETED: Boolean` — Se true, il wizard permessi è già stato superato.
- `PREMIUM_SIMULATION_ACTIVE: Boolean` — Se true, simula lo sblocco del piano Premium per test.
- `IMMEDIATE_BLOCK_UNTIL_TIMESTAMP: Long` — Timestamp di fine del blocco immediato (0 se non attivo).

---

## 4. Modulo Vision & Dance Recognition (Anti-Shaking, 100% On-Device)

### 4.1 Pipeline CameraX & ML Kit
- `DancePoseAnalyzer` implementa `ImageAnalysis.Analyzer`.
- Cattura i frame dalla fotocamera frontale con risoluzione VGA (640x480).
- Converte l'immagine in `InputImage.fromMediaImage(mediaImage, rotationDegrees)`.
- Invia l'immagine a `PoseDetector` (configurato con `PoseDetectorOptions.STREAM_MODE`).
- **Isolamento RAM:** Il buffer `imageProxy` viene chiuso immediatamente nel blocco `finally` dopo il passaggio all'analizzatore. Zero scrittura su disco o cloud.

### 4.2 Algoritmo di Movimento Intra-Corporeo Normalizzato (`DanceDetector`)
1. **Rilevamento Umano Robusto:** Verifica la presenza con confidenza $> 0.45$ dei landmark di tronco e spalle. Se assenti: `"Inquadrati a figura intera per iniziare"`.
2. **Normalizzazione Anatomica (`bodyScale`):** Calcolo della scala fisica di riferimento (distanza bi-acromiale o lunghezza del busto). Ogni distanza intra-corporea viene divisa per `bodyScale`, rendendola indipendente dalla distanza dalla fotocamera.
3. **Vettore Geometrico Intra-Corporeo:** Calcolo delle distanze relative articolari interne:
   - Polso-Spalla Sinistro ($d_{LW-LS}$) e Destro ($d_{RW-RS}$)
   - Polso-Gomito Sinistro ($d_{LW-LE}$) e Destro ($d_{RW-RE}$)
   - Polso-Fianco Sinistro ($d_{LW-LH}$) e Destro ($d_{RW-RH}$)
   - Distanza tra i due polsi ($d_{LW-RW}$, incrocio ed estensione braccia).
4. **Immunità allo Scuotimento Telefono (Anti-Shaking):** Se il telefono trema o viene mosso nello spazio, le coordinate assolute traslano insieme, ma le distanze relative intra-corporee restano immutate ($\Delta \text{intra} \approx 0$). Il timer NON avanza.
5. **Countdown a 5 Secondi (Test Mode):** Parametrizzato con `DANCE_REQUIRED_SECONDS = 5f`. Solo quando la persona muove braccia e busto attivamente, l'energia cinetica supera la soglia e il timer avanza da 0 a 5 secondi.

---

## 5. Modulo App Blocker Fisico

### 5.1 `AppBlockerAccessibilityService`
- Ascolta `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
- Verifica se il package rilevato appartiene a `BlockedAppEntity` e se l'orario corrente rientra in una fascia attiva in `BlockScheduleEntity` o se è attivo un blocco immediato.
- In caso positivo, apre istantaneamente `BlockerOverlayActivity`.

### 5.2 Sblocco Fisico & Emergenza
Dalla schermata `BlockerOverlayActivity`, l'utente può sbloccare l'accesso anticipato tramite:
- Scansione del Tag NFC registrato.
- Superamento della Dance Mission di 5 secondi.
