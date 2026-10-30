package com.tapout.vision

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class DancePoseAnalyzer(
    private val danceDetector: DanceDetector,
    private val onStateUpdate: (DanceState) -> Unit
) : ImageAnalysis.Analyzer {

    // Configurazione ML Kit Pose Detector ottimizzata per streaming CPU in tempo reale
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector = PoseDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            poseDetector.process(inputImage)
                .addOnSuccessListener { pose ->
                    danceDetector.processPose(pose, onStateUpdate)
                }
                .addOnFailureListener {
                    // Fallback silenzioso: aggiorna lo stato indicando riprova
                    danceDetector.processPose(null, onStateUpdate)
                }
                .addOnCompleteListener {
                    // Privacy First & Memory Safety:
                    // Il frame viene rilasciato immediatamente dalla memoria volatile (RAM).
                    // Nessun dato viene conservato su disco o trasmesso in rete.
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun close() {
        poseDetector.close()
    }
}
