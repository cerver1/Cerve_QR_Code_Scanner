package com.cerve.co.cerveqrcodescanner.services

import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.cerve.co.cerveqrcodescanner.Utils.stopScanner
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    val actionScannerLoadingResults: ((ScannerState, String) -> Unit)? = null,
    val scannerBoundingBox: RectF? = null
): ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        mediaImage?.let {

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner
                .process(image)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val barcodeInCenter = task.result.firstOrNull { barcode ->

                            val barcodeBoundingBox = barcode?.boundingBox?.toRectF() ?: return@firstOrNull false
                            val box = scannerBoundingBox ?: return@firstOrNull false

                            box.intersect(barcodeBoundingBox)

                        }

                        /**
                         * When the successListener fires it means that the barcode is in view of the camera.
                         * But within the SIMPLEconnect application we only want to acknowledge barcodes
                         * within a centered 3x2 grid.
                         */

                        if (barcodeInCenter != null) {
                            /***
                             * The developer may have specific requirements for a barcode a user
                             * will be scanning.
                             *
                             * Logic for these requirements can be entered below.
                             * [is String is only being used as a possible requirement for the barcode]
                             */
                            val barcodeInCenterRaw = barcodeInCenter.rawValue

                            when(barcodeInCenterRaw) {
                                is String -> {
                                    Log.d("Utility", "valid barcodeInCenter $barcodeInCenterRaw")
                                    scanner.stopScanner()
                                    actionScannerLoadingResults?.let { action ->
                                        action(ScannerState.LOADING, barcodeInCenterRaw)
                                    }
                                }

                                else -> {

                                    /***
                                     * Ignore invalid Qr-codes
                                     */

                                    Log.d("Utility", "invalid barcodeInCenter $barcodeInCenterRaw")
                                }

                            }

                        }

                    }

                    /**
                     *  [imageProxy.close()] is required for continued processing of each image
                     */
                    imageProxy.close()
                }
        }

    }

}