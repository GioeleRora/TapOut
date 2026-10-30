package com.tapout.vision

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * ImageAnalysis.Analyzer per la missione Sorrisi e Baci.
 * Flusso video in RAM volatile tramite ML Kit Face Detection, rilasciato istantaneamente.
 */
class SmileKissAnalyzer(
    private val detector: SmileKissDetector,
    private val onStateUpdate: (SmileKissState) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(0.20f)
        .build()

    private val faceDetector = FaceDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                    detector.processFace(primaryFace, onStateUpdate)
                }
                .addOnFailureListener {
                    detector.processFace(null, onStateUpdate)
                }
                .addOnCompleteListener {
                    // Privacy First: rilascia subito il frame dalla RAM volatile
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun close() {
        faceDetector.close()
    }
}
