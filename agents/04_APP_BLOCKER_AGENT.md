# 04_APP_BLOCKER_AGENT — Servizio di Blocco App Fisico & Programmato

## Ruolo
Sviluppa il motore di blocco app mediante `AccessibilityService`, consentendo di programmare fasce orarie di concentrazione o attivare sessioni di blocco immediato, sbloccabili in anticipo unicamente tramite scansione del Tag NFC registrato o completamento della Dance Mission da 30 secondi.

---

## 1. Monitoraggio delle Applicazioni (`AccessibilityService`)
- **Eventi Intercettati:**
  Ascolto leggero e tempestivo degli eventi `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
- **Filtro di Esclusione e Whitelist:**
  - Il package di TapOut (`com.tapout`) non viene mai bloccato.
  - Vengono escluse le applicazioni telefoniche essenziali (dialer d'emergenza, chiamate in arrivo, app impostazioni Bluetooth/NFC di sistema).
- **Verifica delle Regole di Blocco:**
  Verifica immediata in memoria (cache sincrona) se il package rilevato fa parte della blacklist configurata e se c'è una fascia oraria attiva oppure un blocco immediato in corso.

---

## 2. Reindirizzamento e Schermata di Blocco
- **Overlay Istantaneo:**
  Nel momento in cui viene rilevata un'app bloccata, il servizio lancia istantaneamente l'Activity `BlockerOverlayActivity` a schermo intero con flag `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP`, coprendo l'applicazione e impedendone l'uso.
- **Prevenzione del Bypassing:**
  L'utente non può tornare indietro all'app bloccata premendo il pulsante Indietro o il tasto Home: se ritenta l'apertura, la schermata di blocco riappare entro 20 millisecondi.

---

## 3. Protocollo di Sblocco Fisico o Cinetico
La schermata di blocco offre due sole vie per disattivare la restrizione prima del termine naturale dell'orario:
1. **Sblocco Fisico:** Avvicinamento del Tag NFC autorizzato al retro dello smartphone.
2. **Sblocco Cinetico:** Avvio della "Dance Mission" da 30 secondi tramite la fotocamera frontale con ML Kit.
