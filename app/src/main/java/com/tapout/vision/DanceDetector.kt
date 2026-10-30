package com.tapout.vision

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.hypot
import kotlin.math.max

data class DanceState(
    val isUserDetected: Boolean,
    val isDancing: Boolean,
    val currentEnergy: Float,
    val secondsDanced: Float,
    val targetSeconds: Float = DanceDetector.DANCE_REQUIRED_SECONDS,
    val message: String
)

class DanceDetector(
    private val targetSeconds: Float = DANCE_REQUIRED_SECONDS,
    private val onMissionComplete: () -> Unit
) {

    companion object {
        const val DANCE_REQUIRED_SECONDS = 30f

        // Soglia di rumore: variazioni relative inferiori al 10% della larghezza spalle sono considerate micro-jitter/respiro
        private const val NOISE_DEADBAND = 0.10f

        // Soglia minima di movimento corporeo reale per considerare l'utente "in ballo"
        private const val DANCE_ENERGY_THRESHOLD = 0.25f

        // Velocità di adattamento della posa di riferimento (filtro passa-basso)
        private const val REFERENCE_ADAPT_RATE = 0.06f
    }

    // Posa di riferimento normalizzata (centrata sul tronco e divisa per shoulderSpan)
    private val referenceRelativePose = mutableMapOf<Int, Pair<Float, Float>>()
    private var lastFrameTimestamp: Long = 0L

    private var accumulatedDanceSeconds: Float = 0f
    private var isCompleted = false
    private var smoothedEnergy: Float = 0f

    fun processPose(pose: Pose?, onStateUpdate: (DanceState) -> Unit) {
        if (isCompleted) return

        val now = System.currentTimeMillis()
        if (lastFrameTimestamp == 0L) {
            lastFrameTimestamp = now
        }
        val deltaTimeSec = ((now - lastFrameTimestamp) / 1000f).coerceIn(0.01f, 0.2f)
        lastFrameTimestamp = now

        if (pose == null || pose.allPoseLandmarks.isEmpty()) {
            smoothedEnergy = 0f
            onStateUpdate(
                DanceState(
                    isUserDetected = false,
                    isDancing = false,
                    currentEnergy = 0f,
                    secondsDanced = accumulatedDanceSeconds,
                    targetSeconds = targetSeconds,
                    message = "Inquadrati a figura intera per iniziare"
                )
            )
            return
        }

        // Estrazione landmark anatomici critici
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

        val hasShoulders = (leftShoulder != null && leftShoulder.inFrameLikelihood > 0.4f) &&
                (rightShoulder != null && rightShoulder.inFrameLikelihood > 0.4f)

        if (!hasShoulders) {
            smoothedEnergy = 0f
            onStateUpdate(
                DanceState(
                    isUserDetected = false,
                    isDancing = false,
                    currentEnergy = 0f,
                    secondsDanced = accumulatedDanceSeconds,
                    targetSeconds = targetSeconds,
                    message = "Inquadrati a figura intera per iniziare"
                )
            )
            referenceRelativePose.clear()
            return
        }

        // 1. Centro del tronco (Torso Center)
        val torsoCenterX = (leftShoulder!!.position.x + rightShoulder!!.position.x) / 2f
        val torsoCenterY = (leftShoulder.position.y + rightShoulder.position.y) / 2f

        // 2. Scala anatomica: ampiezza bi-acromiale delle spalle
        val shoulderSpan = max(
            hypot(
                leftShoulder.position.x - rightShoulder.position.x,
                leftShoulder.position.y - rightShoulder.position.y
            ),
            50f
        )

        // Landmark tracciati per il ballo (braccia, testa, fianchi)
        val targetJoints = listOf(
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.NOSE,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_HIP
        )

        // 3. Calcolo delle coordinate RELATIVE al centro del tronco e normalizzate per shoulderSpan.
        // Se il telefono trema o viene scosso, sia il landmark che il torsoCenter traslano della stessa quantità:
        // la loro differenza (relativeX, relativeY) rimane RIGIDAMENTE COSTANTE. Zero falsi positivi da scuotimento!
        val currentRelativePose = mutableMapOf<Int, Pair<Float, Float>>()

        for (joint in targetJoints) {
            val lm = pose.getPoseLandmark(joint)
            if (lm != null && lm.inFrameLikelihood > 0.35f) {
                val relX = (lm.position.x - torsoCenterX) / shoulderSpan
                val relY = (lm.position.y - torsoCenterY) / shoulderSpan
                currentRelativePose[joint] = Pair(relX, relY)
            }
        }

        // Se non abbiamo ancora una posa di riferimento, inizializzala e attendi il frame successivo
        if (referenceRelativePose.isEmpty()) {
            referenceRelativePose.putAll(currentRelativePose)
            onStateUpdate(
                DanceState(
                    isUserDetected = true,
                    isDancing = false,
                    currentEnergy = 0f,
                    secondsDanced = accumulatedDanceSeconds,
                    targetSeconds = targetSeconds,
                    message = "🕺 INIZIA A BALLARE O MUOVERE LE BRACCIA!"
                )
            )
            return
        }

        // 4. Misurazione dello spostamento reale degli arti rispetto alla posa di riferimento
        var totalRealDisplacement = 0f
        var validJointsCount = 0

        for ((joint, currentRel) in currentRelativePose) {
            val refRel = referenceRelativePose[joint]
            if (refRel != null) {
                val delta = hypot(currentRel.first - refRel.first, currentRel.second - refRel.second)

                // Deadband anti-rumore: qualsiasi variazione inferiore al 10% della larghezza spalle viene azzerata
                val netDisplacement = if (delta > NOISE_DEADBAND) {
                    delta - NOISE_DEADBAND
                } else {
                    0f
                }

                // Diamo un peso maggiore al movimento dei polsi (braccia che ballano)
                val weight = when (joint) {
                    PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST -> 1.8f
                    PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW -> 1.2f
                    else -> 1.0f
                }

                totalRealDisplacement += netDisplacement * weight
                validJointsCount++

                // Aggiorna gradualmente la posa di riferimento (EMA passa-basso)
                val newRefX = refRel.first + (currentRel.first - refRel.first) * REFERENCE_ADAPT_RATE
                val newRefY = refRel.second + (currentRel.second - refRel.second) * REFERENCE_ADAPT_RATE
                referenceRelativePose[joint] = Pair(newRefX, newRefY)
            } else {
                referenceRelativePose[joint] = currentRel
            }
        }

        val rawEnergy = if (validJointsCount > 0) {
            totalRealDisplacement / validJointsCount
        } else {
            0f
        }

        // Smussamento dell'energia per evitare scatti
        smoothedEnergy = smoothedEnergy * 0.7f + rawEnergy * 0.3f

        val isDancing = smoothedEnergy >= DANCE_ENERGY_THRESHOLD

        if (isDancing) {
            accumulatedDanceSeconds += deltaTimeSec
            if (accumulatedDanceSeconds >= targetSeconds) {
                accumulatedDanceSeconds = targetSeconds
                isCompleted = true
                onMissionComplete()
            }
        }

        val feedbackMessage = when {
            isCompleted -> "🎉 MISSIONE COMPLETATA!"
            isDancing -> "🔥 OTTIMO RITMO! CONTINUA COSÌ!"
            accumulatedDanceSeconds > 0 -> "⏸️ TI SEI FERMATO! MUOVITI!"
            else -> "🕺 MUOVI LE BRACCIA A RITMO!"
        }

        onStateUpdate(
            DanceState(
                isUserDetected = true,
                isDancing = isDancing,
                currentEnergy = (smoothedEnergy / 0.6f).coerceIn(0f, 1.5f),
                secondsDanced = accumulatedDanceSeconds,
                targetSeconds = targetSeconds,
                message = feedbackMessage
            )
        )
    }

    fun reset() {
        accumulatedDanceSeconds = 0f
        isCompleted = false
        referenceRelativePose.clear()
        smoothedEnergy = 0f
        lastFrameTimestamp = 0L
    }
}
