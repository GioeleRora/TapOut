# GUIDA UTENTE TAPOUT — Installazione, Permessi e Utilizzo

Benvenuto in **TapOut**, la sveglia inarrestabile progettata per sconfiggere il sonno profondo e i tentativi di procrastinazione grazie alla combinazione di sblocco fisico tramite **Tag NFC** e la rivoluzionaria **Dance Mission da 30 secondi con intelligenza visiva on-device**.

---

## 1. Installazione e Avvio Rapido

### Metodo 1: Tramite Android Studio
1. Collega il tuo smartphone Android al PC tramite cavo USB.
2. Assicurati che le **Opzioni Sviluppatore** e il **Debug USB** siano attivi sul telefono.
3. Apri il progetto in Android Studio e premi il pulsante verde **Run 'app' (▶)** nella barra superiore.

### Metodo 2: Tramite Riga di Comando (ADB)
Se il file APK è già compilato, esegui da terminale PowerShell:
```powershell
& "C:\Users\gioele\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

## 2. Configurazione dei Permessi (Wizard Interattivo)
Al primo avvio, TapOut ti guiderà attraverso un **Wizard di Autorizzazione**:
1. **Fotocamera:** Consenti per permettere alla "Dance Mission" di rilevare il tuo movimento sul posto. *(Privacy garantita: l'immagine rimane in RAM e non viene mai registrata!)*
2. **Notifiche:** Consenti per ricevere l'avviso di sveglia e il controllo permanente.
3. **Sveglie Esatte (Alarms & Reminders):** Attiva per assicurare che la sveglia scatti al millisecondo stabilito.
4. **Mostra sopra altre app (Display over other apps):** Necessario per visualizzare la sveglia e la schermata di blocco sopra ogni altra applicazione.
5. **Accessibilità (Accessibility):** Attiva il servizio *TapOut App Blocker* per impedire l'uso delle app bloccate durante le sessioni di concentrazione.
6. **Ottimizzazione Batteria (Unrestricted Battery):** Imposta su *Non ottimizzata / Senza restrizioni* per impedire al sistema operativo di arrestare l'allarme in background durante la notte.

---

## 3. Funzionalità Chiave

### ⏰ Sveglia Inarrestabile (Anti-Cheat)
- Quando scatta la sveglia, il volume sale istantaneamente al 100% sul canale di allarme.
- **Nessun pulsante Snooze:** Non puoi rimandare la sveglia.
- **Anti-Silenziamento:** Se premi i tasti del volume del telefono, il volume torna al 100% in millisecondi.

### 🕺 La Dance Mission (30 Secondi di Movimento)
- Selezionando la Dance Mission nella sveglia o come metodo di sblocco d'emergenza, si aprirà la fotocamera frontale con un anello circolare di progresso.
- **Come funziona:** Inquadrati a figura intera o mezzo busto e comincia a ballare o muovere braccia e gambe a ritmo.
- **L'anello avanza solo mentre ti muovi:** Se ti fermi o esci dall'inquadratura, il timer si congela e lo schermo ti incoraggia a continuare. Al completamento dei 30 secondi effettivi di movimento, la sveglia si arresta!

### 📱 Sblocco Fisico con Tag NFC (Singolo e Multi-Tag PRO)
- Vai nella scheda **Impostazioni** dell'app e tocca **"Registra Tag NFC"**.
- Avvicina qualsiasi tag NFC standard (NTAG213, NTAG215, NTAG216, Mifare, tessere dei trasporti o badge aziendali) al retro dello smartphone. Sentirai una vibrazione di conferma e il tag verrà salvato con il nome da te scelto (es. *"Tag Bagno"*).
- **Multi-Tag Fisici (TapOut PRO):** Nel piano PRO puoi registrare più tag contemporaneamente (es. uno in bagno, uno in cucina e il badge nel portafoglio) e spegnere la sveglia o sbloccare le app con uno qualsiasi di essi!
- **Consiglio per Schermata di Blocco su Android:** Alcuni produttori (come OnePlus, Samsung, Xiaomi) hanno un'impostazione di sicurezza chiamata *"Richiedi sblocco del dispositivo per NFC"*. Assicurati che nelle *Impostazioni di Sistema -> Connessioni -> NFC* questa voce sia disattivata, in modo che l'antenna NFC possa leggere il tag istantaneamente anche sopra la lockscreen.
- **Non hai ancora un tag fisico?** Nessun problema: attiva il toggle **"Modalità Test Senza Tag"** nelle Impostazioni per simulare il tap direttamente sullo schermo!

### 🛑 App Blocker (Concentrazione & Detox Digitale)
- Dalla scheda **Blocco App**, seleziona le applicazioni che ti distraggono (social media, giochi, browser).
- Imposta le fasce orarie in cui desideri che siano inaccessibili o tocca **"Avvia Blocco Immediato"**.
- Durante la sessione attiva, l'apertura delle app selezionate sarà bloccata istantaneamente: per sbloccarle prima dell'orario stabilito, dovrai scansionare il Tag NFC o superare la Dance Mission.
