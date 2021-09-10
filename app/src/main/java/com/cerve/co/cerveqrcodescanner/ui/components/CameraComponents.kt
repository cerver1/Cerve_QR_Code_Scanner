package com.cerve.co.cerveqrcodescanner.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.cerve.co.cerveqrcodescanner.Utils
import com.cerve.co.cerveqrcodescanner.Utils.getBoxSize
import com.cerve.co.cerveqrcodescanner.Utils.retrieveBoxRect
import com.cerve.co.cerveqrcodescanner.Utils.toIntSize
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.cerve.co.cerveqrcodescanner.services.BarcodeAnalyzer
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleBackground
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleStroke
import com.cerve.co.cerveqrcodescanner.ui.theme.reticuleRipple
import java.util.concurrent.Executor

@Composable
fun DefaultCameraPreview(
    modifier: Modifier = Modifier,
    actionScannerLoadingResults: ((ScannerState, String) -> Unit)? = null,
) {

    //TODO camera provider could be null if the users device doesn't have a camera
    // ?: throw IllegalStateException("Camera initialization failed.")

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scanningBoundingBoxSizeFromDensity = LocalDensity.current.getBoxSize()
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    AndroidView(
        modifier = modifier,
        factory = { innerContext ->

            val previewView = PreviewView(innerContext)
            val cameraExecutor = ContextCompat.getMainExecutor(innerContext)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                bindPreview(
                    cameraLifecycleOwner = lifecycleOwner,
                    cameraPreviewView = previewView,
                    cameraProvider = cameraProvider,
                    cameraExecutor = cameraExecutor,
                    scanningBoundingBoxSize = scanningBoundingBoxSizeFromDensity,
                    actionScannerLoadingResults = actionScannerLoadingResults

                )
            }, cameraExecutor)

            previewView
        }

    )
//
//    SurfaceViewOverlay(
//        modifier = modifier
//    )

}





private fun bindPreview(
    cameraLifecycleOwner: LifecycleOwner,
    cameraPreviewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraExecutor: Executor,
    scanningBoundingBoxSize: Size,
    actionScannerLoadingResults: ((ScannerState, String) -> Unit)? = null,
) {

    val previewViewSize = cameraPreviewView.toIntSize()
    val previewWidth = cameraPreviewView.width
    val previewHeight = cameraPreviewView.height

    val previewAspectRatio = Utils.aspectRatio(previewWidth, previewHeight)
    // TODO could be used for setTargetResolution() instead of setAspectRatio()

    val cameraPreview = androidx.camera.core.Preview.Builder()
        .setTargetAspectRatio(previewAspectRatio)
        .build()

    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetAspectRatio(previewAspectRatio)
        .build()

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    val imageAnalyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(previewAspectRatio)
        .build()
        .also { analyzer ->

            analyzer.setAnalyzer(
                cameraExecutor,
                BarcodeAnalyzer(
                    actionScannerLoadingResults = actionScannerLoadingResults,
                    scannerBoundingBox = previewViewSize.retrieveBoxRect(scanningBoundingBoxSize)
                )
            )

        }

    try {
        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(
            cameraLifecycleOwner,
            cameraSelector,
            cameraPreview,
            imageCapture,
            imageAnalyzer
        )

        cameraPreview.setSurfaceProvider(cameraPreviewView.surfaceProvider)

    } catch (e: Exception) {

    }

}