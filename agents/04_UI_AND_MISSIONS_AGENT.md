# 04_UI_AND_MISSIONS_AGENT — UI Compose, Missioni di Risveglio e Onboarding

## Ruolo
Realizza un'interfaccia utente moderna, fluida e ad alto contrasto in Jetpack Compose (Material 3 Dark Theme), le missioni matematiche cognitive, il protocollo di sblocco d'emergenza anti-lockout e il wizard di onboarding dei permessi di sistema.

---

## 1. Design System & Stile Visivo
- **Tema:** Dark OLED puro (`#05070B` / `#0E121A`) con elementi in rilievo (`#171D28`), testi ad alta leggibilità (`#F0F4F8`, `#94A3B8`), e colori di accento:
  - *Electric Cyan* (`#00E5FF`) per azioni primarie, sveglia e NFC.
  - *Amber Warning* (`#FFB300`) per le missioni e avvisi.
  - *Crimson Danger* (`#FF3B30`) per il blocco app e il protocollo di emergenza.
- **Tipografia:** Moderna e chiara, orologi a cifre grandi per le sveglie.
- **Nessuna animazione superflua:** Focus su reattività istantanea e robustezza.

---

## 2. Missioni Matematiche Graduali
- **Livelli di Difficoltà:**
  1. *Facile:* Addizioni e sottrazioni a due cifre (es. `18 + 27`, `85 - 39`).
  2. *Medio:* Moltiplicazioni miste ad addizioni/sottrazioni (es. `(14 * 7) - 25`, `45 + (12 * 8)`).
  3. *Difficile:* Calcoli a tre operatori con moltiplicazioni impegnative (es. `(24 * 13) + 87 - 45`, `(36 * 19) - 124`).
- **Configurabilità:**
  - L'utente sceglie nelle opzioni della sveglia da 1 a 10 quesiti consecutivi.
  - L'avanzamento mostra una barra di progresso (es. "Quesito 3 di 5").
- **Funzionalità Audio Ducking Intelligente (Attenuazione per Concentrazione):**
  - Mentre l'utente digita la risposta sul tastierino numerico a schermo, il volume della sveglia si abbassa temporaneamente del 70% per consentirgli di concentrarsi sul calcolo.
  - Un timer visivo di 10 secondi si avvia ad ogni tocco del tastierino. Se l'utente smette di digitare o impiega più di 10 secondi senza confermare una risposta, il volume torna istantaneamente al 100% per forzare il risveglio.
  - Se la risposta è errata: feedback sonoro/vibrazione, il calcolo corrente viene sostituito con uno nuovo e il volume torna al 100%.

---

## 3. Protocollo di Emergenza Anti-Lockout
- Pensato per quando l'utente è fuori casa o ha smarrito temporaneamente il Tag NFC:
  - **Fase 1:** Risolvere obbligatoriamente **15 calcoli matematici di livello Difficile** consecutivi senza poter saltare alcun quesito.
  - **Fase 2:** Tenere premuto un pulsante a schermo per **60 secondi continui** senza staccare il dito.
    - Un cerchio di progresso radiale e un conto alla rovescia indicano il tempo rimanente.
    - Se il dito si stacca anche solo per 100 millisecondi, il timer si resetta a 60 secondi.
  - Solo al superamento di entrambe le fasi l'allarme o il blocco app si disattivano.

---

## 4. Wizard Onboarding Permessi Interattivo
- Schermata di verifica iniziale all'avvio dell'app che controlla e guida l'utente nella concessione dei permessi critici con indicatori di stato dinamici (Spunta verde / Punto esclamativo rosso):
  1. **Notifiche & Schermo Intero** (`POST_NOTIFICATIONS` + `USE_FULL_SCREEN_INTENT`).
  2. **Sveglie Esatte** (`SCHEDULE_EXACT_ALARM` / `canScheduleExactAlarms()`).
  3. **Ottimizzazione Batteria** (Richiesta `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
  4. **Accessibilità per Blocco App** (Apre `Settings.ACTION_ACCESSIBILITY_SETTINGS` evidenziando il servizio `Sveglia Speciale Blocco`).
  5. **NFC Attivo** (Verifica `NfcAdapter.isEnabled()`, se disattivato apre `Settings.ACTION_NFC_SETTINGS`).
- Ogni voce dispone di un pulsante diretto "Abilita" che reindirizza la configurazione di sistema appropriata.
