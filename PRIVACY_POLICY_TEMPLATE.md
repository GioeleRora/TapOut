# Informativa sulla Privacy di TapOut (Privacy Policy)

*Ultimo aggiornamento: 30 Ottobre 2026*

La presente Informativa sulla Privacy descrive le modalità con cui l'applicazione **TapOut** raccoglie, utilizza ed elabora le informazioni e garantisce la riservatezza e la sicurezza dei dati degli utenti, conformemente al Regolamento Generale sulla Protezione dei Dati (GDPR - Regolamento UE 2016/679) e alle norme vigenti del Google Play Store.

---

## 1. Principio Fondamentale: Privacy-First e Visione Esclusivamente On-Device

**Nessuna immagine, fotogramma, foto o video viene mai registrato, salvato sul dispositivo o trasmesso a server esterni.**

- **Elaborazione Locale in RAM (Google ML Kit Pose Detection):**
  La funzionalità "Dance Mission" fa uso della fotocamera frontale del dispositivo esclusivamente per analizzare in tempo reale la presenza e il movimento di landmark scheletrici (es. movimento delle braccia e delle gambe).
- **Flusso Immediato e Volatile:**
  Ogni singolo fotogramma viene acquisito tramite Android CameraX in memoria volatile (RAM), elaborato istantaneamente sul chip locale tramite Google ML Kit Pose Detection e immediatamente distrutto (`imageProxy.close()`).
- **Nessun Tracciamento o Riconoscimento Facciale:**
  TapOut non raccoglie dati biometrici identificativi, impronte vocali o tratti somatici dell'utente. Il sistema calcola unicamente un indice numerico di varianza cinetica (energia di movimento).

---

## 2. Dati Raccolti e Memorizzati Esclusivamente sul Dispositivo Locale

Tutti i dati generati durante l'utilizzo dell'applicazione sono conservati esclusivamente all'interno della memoria protetta dell'applicazione sul dispositivo dell'utente (Room Database e DataStore crittografato di sistema):
- **Impostazioni Sveglie:** Orari, giorni della settimana selezionati, etichette delle sveglie, suonerie scelte e preferenze di risveglio (NFC, Dance Mission).
- **Regole del Blocco App:** Elenco dei package delle applicazioni selezionate per il blocco e le relative fasce orarie.
- **Identificatore Hardware del Tag NFC:** Codice seriale univoco esadecimale (UID) del tag NFC associato dall'utente per lo sblocco fisico. Tale codice rimane memorizzato in locale e non è collegato all'identità dell'utente.

---

## 3. Autorizzazioni di Sistema Richieste e relative Motivazioni

- **Fotocamera (`CAMERA`):**
  Utilizzata unicamente ed esclusivamente per consentire alla Dance Mission di verificare il risveglio tramite movimento.
- **Comunicazione NFC (`NFC`):**
  Necessaria per consentire al chip NFC del telefono di scansionare i tag fisici e verificare la corrispondenza dell'UID per lo sblocco della sveglia e delle app bloccate.
- **Sveglie Esatte (`SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`):**
  Indispensabile per fare scattare l'allarme al secondo esatto stabilito, senza ritardi causati dal risparmio energetico di Android.
- **Schermo Intero sopra il Lockscreen (`USE_FULL_SCREEN_INTENT`, `SYSTEM_ALERT_WINDOW`):**
  Necessario per mostrare l'interfaccia di disattivazione allarme a tutto schermo anche a telefono bloccato o a schermo spento.
- **Servizi in Primo Piano (`FOREGROUND_SERVICE`):**
  Garantisce la riproduzione ininterrotta dell'audio della sveglia e l'esecuzione affidabile del protocollo di sicurezza anti-cheat.
- **Servizio di Accessibilità (`BIND_ACCESSIBILITY_SERVICE`):**
  Utilizzato unicamente per rilevare il passaggio in primo piano di applicazioni incluse nella blacklist e presentare la schermata di blocco di TapOut.

---

## 4. Servizi di Terze Parti e Assenza di Tracciamento Cloud

TapOut non integra script di tracciamento invasivi, tracker pubblicitari di terze parti o pixel di monitoraggio comportamentale. Non vendiamo, scambiamo o condividiamo alcun dato con intermediari pubblicitari o broker di dati.

---

## 5. Controllo e Cancellazione dei Dati

L'utente ha il pieno e totale controllo sui propri dati:
- È possibile eliminare o modificare qualsiasi sveglia, orario di blocco o tag NFC registrato direttamente dalle schermate dell'app in qualsiasi momento.
- La disinstallazione dell'applicazione comporta l'eliminazione definitiva e irreversibile di tutti i dati memorizzati in locale.

---

## 6. Contatti
Per qualunque domanda, chiarimento o richiesta relativa a questa informativa sulla privacy, è possibile contattare il supporto ufficiale TapOut tramite i canali di rilascio dell'applicazione.
