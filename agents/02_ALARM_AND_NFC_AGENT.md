# 02_ALARM_AND_NFC_AGENT — Motore Sveglia Inarrestabile & Lettore NFC

## Ruolo
Sviluppa e garantisce l'affidabilità assoluta del motore di allarme ad alta priorità e il sottosistema di lettura universale per tag NFC fisici.

---

## 1. Motore Sveglia Inarrestabile
- **Precisione Assoluta:**
  Utilizza `AlarmManager.setAlarmClock()` con un `PendingIntent` speciale per la visualizzazione dell'orario esatto nella schermata di blocco di Android.
- **Resistenza al Reboot:**
  `BootReceiver` intercetta `Intent.ACTION_BOOT_COMPLETED`, `Intent.ACTION_MY_PACKAGE_REPLACED` e `Intent.ACTION_TIME_CHANGED`, interrogando Room Database per riprogrammare tutte le sveglie attive.
- **Foreground Service & WakeLock:**
  `AlarmReceiver` ottiene un `PowerManager.WakeLock` parziale e avvia `AlarmService`. Il servizio pubblica una notifica ad alta priorità sul canale `tapout_alarm_channel_v1` con flag `PRIORITY_MAX` e `fullScreenIntent` verso `AlarmActivity`.

---

## 2. AudioGuard & Anti-Silenziamento
- **Canale `STREAM_ALARM` al 100%:**
  L'allarme viene emesso sul canale dedicato `AudioManager.STREAM_ALARM` al massimo volume supportato dal dispositivo (`getStreamMaxVolume`).
- **Loop di Vigilanza Attiva:**
  Un thread/coroutine dedicato e un `ContentObserver` monitorano le variazioni del volume. Se l'utente preme i bilancieri del volume hardware per abbassarlo, il sistema lo riporta istantaneamente al 100% entro 20 millisecondi.
- **Sintesi Sonora di Fallback:**
  Se il file audio personalizzato o la suoneria di sistema non è accessibile, `ToneGeneratorHelper` sintetizza direttamente tramite `AudioTrack` una sirena a doppia frequenza (880Hz / 1760Hz in modulazione alternata) in loop infinito.

---

## 3. Lettore Universale Tag NFC
- **Modalità `enableReaderMode`:**
  Utilizza l'API moderna di Android per la lettura a basso livello dei tag con flag `NFC_A`, `NFC_B`, `NFC_F`, `NFC_V`, disattivando i suoni di sistema standard di Android per un feedback immediato e privo di latenza.
- **Estrazione Hex UID:**
  Converte l'array di byte dell'UID hardware in una stringa esadecimale universale in lettere maiuscole (es. `04:A2:3B:1F:7C:89:D0`). Compatibile con NTAG213, NTAG215, NTAG216, Mifare Classic/Ultralight e tessere magnetiche standard.
- **Modalità Test Senza Tag (Fallback di Simulazione):**
  Un toggle globale in DataStore permette di simulare un tap NFC tramite un pulsante dedicato sullo schermo, consentendo all'utente di testare tutti i flussi di disattivazione prima dell'arrivo del tag fisico.
