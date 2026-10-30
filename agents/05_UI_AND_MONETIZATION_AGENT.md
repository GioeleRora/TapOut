# 05_UI_AND_MONETIZATION_AGENT — Interfaccia Utente Compose & Modulo Freemium

## Ruolo
Progetta e sviluppa l'interfaccia utente moderna ad alto contrasto in Jetpack Compose (Material 3 Dark Theme con accenti neon Electric Orange `#FF6D00` ed Electric Purple `#7C4DFF`), le animazioni di interazione e il modulo di predisposizione commerciale Freemium (`BillingManager`).

---

## 1. Stile Visivo & Identità del Brand
- **Palette Colori:**
  - *OLED Cyber Black:* `#08090C` / `#101217`
  - *Card Surface:* `#181B22` / `#222733`
  - *Electric Orange (Accent 1):* `#FF6D00` (per allarmi, energia cinetica, fuoco del risveglio)
  - *Electric Purple (Accent 2):* `#7C4DFF` (per NFC, protezione fisica e blocco app)
  - *Neon Mint (Success):* `#00E676` (per sblocchi completati e indicatori positivi)
  - *Pure White & Slate:* `#F5F7FA` / `#8E9AA8` per testi ad altissima leggibilità.

---

## 2. Schermate dell'Applicazione
1. **Dashboard Sveglie (`AlarmDashboardScreen`):**
   - Elenco sveglie attive con toggle, orario gigante e badge delle missioni abilitate (NFC, Dance Mission).
   - Dialog modale per creare e modificare sveglie: orario, giorni della settimana, scelta suoneria/musica, selezione della missione di risveglio.
2. **App Blocker (`AppBlockerScreen`):**
   - Selettore delle app installate (recuperate via `PackageManager`) con icone di sistema e switch.
   - Configurazione fasce orarie settimanali e pulsante per l'attivazione del "Blocco Immediato" (es. 30 min, 1h, 2h).
3. **Schermata Sveglia Attiva (`AlarmActivity` & `AlarmActiveScreen`):**
   - Schermo intero a contrasto estremo con orologio digitale in tempo reale ad alta precisione.
   - Zona sensibile per scansione NFC e pulsante prominente per avviare la "Dance Mission".
4. **Schermata Dance Mission (`DanceMissionScreen`):**
   - Anteprima della fotocamera frontale in una card circolare o quadrata con bordi arrotondati.
   - Indicatore radiale circolare (Ring Progress) che si riempie in arancio neon da 0% a 100% (30 secondi).
   - Barra di stato cinetica con indicazione visiva "Muoviti!" o "Ottimo ritmo!".
5. **Wizard Onboarding Permessi (`PermissionsWizardScreen`):**
   - Checklist interattiva che verifica lo stato in tempo reale di: Fotocamera, Notifiche, Sveglie Esatte, Accessibilità, NFC, Ottimizzazione Batteria.
   - Pulsanti one-click per aprire le relative impostazioni di sistema Android.
6. **Impostazioni & Abbonamento Premium (`SettingsScreen`):**
   - Registrazione e test del Tag NFC con radar di scansione.
   - Toggle per la modalità di test simulata (Senza Tag).
   - Sezione Freemium / Simulazione Abbonamento Premium: visualizzazione dei vantaggi (temi esclusivi, blocco app illimitato, multi-tag) con toggle per testare lo sblocco delle funzionalità avanzate.

---

## 3. Architettura Freemium (`BillingManager`)
- **Stato Abbonamento:** `StateFlow<SubscriptionPlan>` (FREE vs. PREMIUM).
- **Feature Flags:**
  - `maxBlockedApps`: 3 per il piano Free, Illimitate per Premium.
  - `allowMultipleNfcTags`: 1 per il piano Free, Multipli per Premium.
  - `customTonesUnlimited`: Abilitato con Premium.
- **Simulatore Locale:** Permette il test completo delle feature premium senza necessità di connessione al Play Store in fase di sviluppo.
