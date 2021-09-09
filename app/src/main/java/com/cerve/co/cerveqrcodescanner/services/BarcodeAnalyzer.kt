package com.cerve.co.cerveqrcodescanner.services

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.cerve.co.cerveqrcodescanner.Utils.logIt
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.sql.Struct

class BarcodeAnalyzer(
    private val actionSetBarcodeValueAndState: ((ScannerState, String?) -> Unit)? = null,
    var scannerBoundingBox: RectF? = null
): ImageAnalysis.Analyzer {


    private val scanner = BarcodeScanning.getClient()
    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        mediaImage?.let {

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        RectF()
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
                             */
                            when(barcodeInCenter.rawValue) {

                                is String -> {
                                    "valid ${barcodeInCenter.rawValue}".logIt("BarcodeAnalyzer")
                                    scanner.stop()
                                    actionSetBarcodeValueAndState?.let { action ->
                                        action(ScannerState.LOADING, barcodeInCenter.rawValue)
                                    }
                                }

                                else -> {

                                    /***
                                     * Ignore invalid Qr-codes
                                     */

                                    "ignore ${barcodeInCenter.rawValue}".logIt("BarcodeAnalyzer")

                                }


                            }
                        }

                    }

                    /**
                     *  [imageProxy.close()] is required for iteration per ImageProxy.image
                     */
                    imageProxy.close()

                }

        }

    }


    private fun BarcodeScanner?.stop() {
        if (this != null) {
            try {
                this.close()
            } catch (e: Exception) {
                "scanner $this | stop error: ${e.message}".logIt("MLKitBarcodeAnalyzer")
            }
        }

    }
}