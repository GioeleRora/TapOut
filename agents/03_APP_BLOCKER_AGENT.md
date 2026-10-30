# 03_APP_BLOCKER_AGENT — Hardcore Alarm Shield (Blocco Totale Durante la Sveglia)

## Ruolo
Garantisce che l'applicazione sia una **sveglia inarrestabile pura e dura ("cazzuta")**: zero funzioni in stile digital-detox o wealthcare a orari programmati. Quando la sveglia suona, lo smartphone entra in modalità **blocco totale (lockdown assoluto)**: nessuna applicazione può essere aperta o utilizzata finché l'allarme non viene disattivato fisicamente con il Tag NFC o tramite la missione stabilita.

---

## 1. Intercettazione Real-Time via AccessibilityService
- **`AppBlockerAccessibilityService`:**
  - Ascolta gli eventi `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
  - Verifica all'istante lo stato di allarme attivo tramite `AlarmService.isAlarmRinging`.
  - Se la sveglia sta suonando:
    - Qualsiasi finestra che tenti di apparire (Home launcher di sistema, App Recenti, Instagram, TikTok, Browser, Giochi, Impostazioni) viene intercettata in meno di 20ms.
    - Se il pacchetto aperto non è `com.svegliaspeciale` e non è il compositore telefonico d'emergenza (`telephony`/`dialer`), il servizio rimette istantaneamente `AlarmActivity` in primo piano con i flag:
      `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_SINGLE_TOP`.

---

## 2. Inviolabilità dell'Esperienza di Sveglia
- L'utente non può:
  - Minimizzare la sveglia per navigare sui social a letto.
  - Aprire notifiche di altre app.
  - Abbassare il volume.
  - Usare scorciatoie gesture o barra di navigazione per fuggire dalla sveglia.
- L'unico modo per tornare a usare il proprio telefono è completare il risveglio:
  1. Scansionare il Tag NFC fisico registrato;
  2. Risolvere la missione matematica (se abilitata);
  3. Oppure completare il protocollo punitivo di emergenza (15 calcoli difficili + 60s touch continuo).
