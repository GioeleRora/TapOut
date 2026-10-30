package com.tapout.vision

import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.hypot
import kotlin.math.max

data class SmileKissState(
    val isFaceDetected: Boolean = false,
    val smilesCount: Int = 0,
    val targetSmiles: Int = SmileKissDetector.TARGET_COUNT,
    val kissesCount: Int = 0,
    val targetKisses: Int = SmileKissDetector.TARGET_COUNT,
    val isSmilingNow: Boolean = false,
    val isKissingNow: Boolean = false,
    val isCompleted: Boolean = false,
    val message: String = "Inquadra il viso per iniziare"
)

/**
 * Motore di computer vision on-device per la missione Sorrisi & Baci.
 * Calibrato accuratamente per:
 * 1. Sorrisi autentici: richiede probabilità elevata (>= 0.80) e allargamento labbra (>= 0.70).
 * 2. Baci (Pucker) precisi: richiede che la bocca sia significativamente stretta (<= 0.58) E
 *    che le labbra siano raccolte e pronunciate verticalmente (aspect ratio W/H <= 2.4).
 *    Questo esclude categoricamente volti neutri o a riposo, richiedendo un vero bacio alla fotocamera.
 * 3. Macchina a stati a isteresi: obbliga a rilassare l'espressione prima di contare il punto successivo.
 * 100% on-device in RAM volatile, privacy-first, zero disk, zero cloud.
 */
class SmileKissDetector(
    private val targetSmiles: Int = TARGET_COUNT,
    private val targetKisses: Int = TARGET_COUNT,
    private val onMissionComplete: () -> Unit
) {

    companion object {
        const val TARGET_COUNT = 5

        // Soglie calibrate per Sorriso
        private const val SMILE_PROBABILITY_THRESHOLD = 0.80f
        private const val SMILE_RESET_THRESHOLD = 0.22f
        private const val SMILE_FRAMES_REQUIRED = 4

        // Soglie calibrate per Bacio (Pucker)
        // Labbra arricciate a bacio: richiede bocca stretta E labbra raccolte a "O"
        private const val KISS_RATIO_EYES_THRESHOLD = 0.58f
        private const val KISS_ASPECT_RATIO_MAX = 2.4f
        private const val KISS_TIGHT_RATIO = 0.52f
        private const val KISS_RESET_RATIO = 0.68f
        private const val KISS_RESET_ASPECT = 3.0f
        private const val KISS_MAX_SMILE_PROB = 0.15f
        private const val KISS_FRAMES_REQUIRED = 4
    }

    private var smilesCount = 0
    private var kissesCount = 0

    // Accumulatori morbidi con isteresi
    private var isWaitingForSmileReset = false
    private var smileScoreFrames = 0

    private var isWaitingForKissReset = false
    private var kissScoreFrames = 0

    private var missionCompleted = false

    fun reset() {
        smilesCount = 0
        kissesCount = 0
        isWaitingForSmileReset = false
        smileScoreFrames = 0
        isWaitingForKissReset = false
        kissScoreFrames = 0
        missionCompleted = false
    }

    fun processFace(face: Face?, onStateUpdate: (SmileKissState) -> Unit) {
        if (missionCompleted) {
            onStateUpdate(
                SmileKissState(
                    isFaceDetected = face != null,
                    smilesCount = smilesCount,
                    targetSmiles = targetSmiles,
                    kissesCount = kissesCount,
                    targetKisses = targetKisses,
                    isSmilingNow = false,
                    isKissingNow = false,
                    isCompleted = true,
                    message = "Missione completata con successo! 🎉"
                )
            )
            return
        }

        if (face == null) {
            smileScoreFrames = max(0, smileScoreFrames - 1)
            kissScoreFrames = max(0, kissScoreFrames - 1)
            onStateUpdate(
                SmileKissState(
                    isFaceDetected = false,
                    smilesCount = smilesCount,
                    targetSmiles = targetSmiles,
                    kissesCount = kissesCount,
                    targetKisses = targetKisses,
                    isSmilingNow = false,
                    isKissingNow = false,
                    isCompleted = false,
                    message = "Inquadra bene il viso nella fotocamera"
                )
            )
            return
        }

        val smileProb = face.smilingProbability ?: 0f
        val faceBoxWidth = face.boundingBox.width().toFloat().coerceAtLeast(10f)

        // 1. Calcolo Distanza Occhi (priorità ai contorni occhi completi, poi landmarks, poi stima viso)
        val leftEyeContour = face.getContour(FaceContour.LEFT_EYE)?.points
        val rightEyeContour = face.getContour(FaceContour.RIGHT_EYE)?.points

        val eyeDistance = if (!leftEyeContour.isNullOrEmpty() && !rightEyeContour.isNullOrEmpty()) {
            val leftX = leftEyeContour.map { it.x }.average().toFloat()
            val leftY = leftEyeContour.map { it.y }.average().toFloat()
            val rightX = rightEyeContour.map { it.x }.average().toFloat()
            val rightY = rightEyeContour.map { it.y }.average().toFloat()
            hypot(leftX - rightX, leftY - rightY).coerceAtLeast(10f)
        } else {
            val leftLandmark = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
            val rightLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
            if (leftLandmark != null && rightLandmark != null) {
                hypot(leftLandmark.x - rightLandmark.x, leftLandmark.y - rightLandmark.y).coerceAtLeast(10f)
            } else {
                faceBoxWidth * 0.44f
            }
        }

        // 2. Calcolo Geometria Labbra (ampiezza ed altezza)
        val upperLipTop = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
        val lowerLipBottom = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points
        val allLipPoints = (upperLipTop.orEmpty() + lowerLipBottom.orEmpty())

        val mouthDistance: Float
        val mouthHeight: Float

        if (allLipPoints.isNotEmpty()) {
            val minX = allLipPoints.minOf { it.x }
            val maxX = allLipPoints.maxOf { it.x }
            mouthDistance = (maxX - minX).coerceAtLeast(5f)

            val minY = allLipPoints.minOf { it.y }
            val maxY = allLipPoints.maxOf { it.y }
            mouthHeight = (maxY - minY).coerceAtLeast(2f)
        } else {
            val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
            val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
            mouthDistance = if (mouthLeft != null && mouthRight != null) {
                hypot(mouthLeft.x - mouthRight.x, mouthLeft.y - mouthRight.y)
            } else {
                eyeDistance * 0.75f
            }
            mouthHeight = 10f
        }

        val mouthEyeRatio = mouthDistance / eyeDistance
        val mouthAspect = mouthDistance / mouthHeight

        // ==========================================
        // 1. RILEVAMENTO SORRISO CALIBRATO (più rigoroso)
        // ==========================================
        // Richiede probabilità elevata e che la bocca sia effettivamente allargata
        val isSmilingInstantly = smileProb >= SMILE_PROBABILITY_THRESHOLD && mouthEyeRatio >= 0.70f

        if (isSmilingInstantly && smilesCount < targetSmiles) {
            if (!isWaitingForSmileReset) {
                smileScoreFrames++
                if (smileScoreFrames >= SMILE_FRAMES_REQUIRED) {
                    smilesCount++
                    isWaitingForSmileReset = true
                    smileScoreFrames = 0
                }
            }
        } else {
            smileScoreFrames = max(0, smileScoreFrames - 1)
            // L'utente deve rilassare il viso tornando a espressione neutra prima del prossimo sorriso
            if (smileProb <= SMILE_RESET_THRESHOLD) {
                isWaitingForSmileReset = false
            }
        }

        // ==========================================
        // 2. RILEVAMENTO BACIO CALIBRATO (Richiede vero pucker, mai a riposo)
        // ==========================================
        // Le labbra a bacio richiedono che la bocca sia stretta (<= 0.58) E
        // raccolta verticalmente in una forma a "O" (W/H <= 2.4), oppure strettissima (<= 0.52).
        val isPuckerGeometry = (mouthEyeRatio <= KISS_RATIO_EYES_THRESHOLD && mouthAspect <= KISS_ASPECT_RATIO_MAX) ||
                (mouthEyeRatio <= KISS_TIGHT_RATIO)

        val isPuckeredInstantly = isPuckerGeometry && smileProb <= KISS_MAX_SMILE_PROB

        if (isPuckeredInstantly && kissesCount < targetKisses) {
            if (!isWaitingForKissReset) {
                kissScoreFrames++
                if (kissScoreFrames >= KISS_FRAMES_REQUIRED) {
                    kissesCount++
                    isWaitingForKissReset = true
                    kissScoreFrames = 0
                }
            }
        } else {
            kissScoreFrames = max(0, kissScoreFrames - 1)
            // Reset bacio: labbra tornano distese a riposo (non più a bacio) o sorriso
            if (mouthEyeRatio >= KISS_RESET_RATIO || mouthAspect >= KISS_RESET_ASPECT || smileProb >= 0.25f) {
                isWaitingForKissReset = false
            }
        }

        // Verifica completamento della missione
        val completed = (smilesCount >= targetSmiles && kissesCount >= targetKisses)
        if (completed && !missionCompleted) {
            missionCompleted = true
            onMissionComplete()
        }

        // Messaggio dinamico guidato per l'utente
        val dynamicMessage = when {
            completed -> "Missione completata con successo! 🎉"
            isSmilingInstantly && isWaitingForSmileReset -> "Sorriso registrato! Rilassa il viso per il prossimo 😊"
            isPuckeredInstantly && isWaitingForKissReset -> "Bacio registrato! Rilassa le labbra per il prossimo 💋"
            smilesCount < targetSmiles && kissesCount < targetKisses -> "Fai un bel sorriso 😊 o manda un bacio 💋"
            smilesCount < targetSmiles -> "Mancano ${targetSmiles - smilesCount} sorrisi! Sorridi bene 😊"
            else -> "Mancano ${targetKisses - kissesCount} baci! Arriccia le labbra a bacio 💋"
        }

        onStateUpdate(
            SmileKissState(
                isFaceDetected = true,
                smilesCount = smilesCount,
                targetSmiles = targetSmiles,
                kissesCount = kissesCount,
                targetKisses = targetKisses,
                isSmilingNow = isSmilingInstantly,
                isKissingNow = isPuckeredInstantly,
                isCompleted = completed,
                message = dynamicMessage
            )
        )
    }
}
