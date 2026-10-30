# 03_VISION_AND_DANCE_AGENT — Computer Vision & Dance Recognition

## Ruolo
Progetta e sviluppa il modulo di Computer Vision basato su CameraX e Google ML Kit Pose Detection, implementando la "Dance Mission" da 30 secondi con analisi 100% on-device in memoria volatile.

---

## 1. Principi di Privacy Assoluta (Zero Cloud, Zero Recording)
- **Elaborazione Esclusiva in RAM:**
  I frame catturati da `ImageAnalysis` fluiscono come `ImageProxy` direttamente al modulo di inferenza ML Kit Pose Detection in memoria volatile.
- **Rilascio Immediato dei Frame:**
  Ogni frame viene consumato e rilasciato (`imageProxy.close()`) in meno di 40ms. Nessun buffer viene scritto su disco, convertito in file JPEG/PNG, salvato nella galleria o trasmesso tramite API di rete.
- **Assenza di Tracciamento Biometrico Persistente:**
  Le coordinate dei landmark scheletrici vengono utilizzate esclusivamente per calcolare un valore scalare di energia cinetica e vengono distrutte frame dopo frame.

---

## 2. Pipeline CameraX & Google ML Kit
- **Configurazione CameraX:**
  Utilizzo della fotocamera frontale (`CameraSelector.DEFAULT_FRONT_CAMERA`) con risoluzione ottimizzata per Computer Vision (640x480 o 480x360, aspect ratio 4:3) per minimizzare il consumo di CPU e batteria.
- **ML Kit Pose Detector:**
  Utilizzo di `AccuratePoseDetectorOptions` o `PoseDetectorOptions.STREAM_MODE` con esecuzione su CPU ottimizzata per stream continuo in tempo reale.
- **Integrazione `ImageAnalysis`:**
  Analizzatore con `STRATEGY_KEEP_ONLY_LATEST` per evitare accumulo di frame in coda e garantire latenza zero.

---

## 3. Algoritmo di Riconoscimento del Ballo (Kinetic Dance Meter)
- **Landmark Tracciati:**
  Monitoraggio delle coordinate 3D dei punti chiave del corpo:
  - Polso sinistro & Polso destro (`LEFT_WRIST`, `RIGHT_WRIST`)
  - Gomito sinistro & Gomito destro (`LEFT_ELBOW`, `RIGHT_ELBOW`)
  - Spalla sinistra & Spalla destra (`LEFT_SHOULDER`, `RIGHT_SHOULDER`)
  - Fianco sinistro & Fianco destro (`LEFT_HIP`, `RIGHT_HIP`)
  - Caviglia sinistra & Caviglia destra (`LEFT_ANKLE`, `RIGHT_ANKLE`)
- **Calcolo dell'Energia Cinetica:**
  Per ciascun frame, viene calcolata la velocità euclidea normalizzata dei landmark rispetto al frame precedente. Se la somma pesata delle distanze coperte supera la soglia minima (`MIN_DANCE_ENERGY_THRESHOLD`), lo stato utente passa a `DANCING`.
- **Meccanica del Timer a 30 Secondi:**
  - Un indicatore circolare ad anello avanza da 0 a 30 secondi solo quando l'energia cinetica supera la soglia.
  - Se l'utente si ferma, rallenta eccessivamente o esce dal campo visivo, il timer si mette in pausa e lo schermo mostra un feedback visivo immediato: *"Continua a muoverti! Rilevamento in pausa"*.
  - Al raggiungimento dei 30 secondi continui di movimento effettivo accumulato, la missione è dichiarata completata e la sveglia o il blocco app viene disattivato con successo.
