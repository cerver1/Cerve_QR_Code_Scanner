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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.LifecycleOwner
import com.cerve.co.cerveqrcodescanner.Utils
import com.cerve.co.cerveqrcodescanner.Utils.getBoxSize
import com.cerve.co.cerveqrcodescanner.Utils.retrieveBoxRect
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.cerve.co.cerveqrcodescanner.services.BarcodeAnalyzer
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleBackground
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleStroke
import com.cerve.co.cerveqrcodescanner.ui.theme.reticuleRipple
import java.util.concurrent.Executor

@Composable
fun DefaultCameraPreview(
    modifier: Modifier = Modifier,
    actionScannerLoadingResults: ((ScannerState, String?) -> Unit)? = null,
    ) {

    //TODO camera provider could be null if the users device doesn't have a camera
    // ?: throw IllegalStateException("Camera initialization failed.")

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val boxSize = LocalDensity.current.getBoxSize()

    AndroidView(
        modifier = modifier,
        factory = { innerContext ->


            val previewView = PreviewView(innerContext)
            val cameraExecutor = ContextCompat.getMainExecutor(innerContext)
            Log.d("Utility", "previewView : ${previewView.width}")

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                bindPreview(
                    cameraLifecycleOwner = lifecycleOwner,
                    cameraPreviewView = previewView,
                    cameraProvider = cameraProvider,
                    cameraExecutor = cameraExecutor,
                    boxSize = boxSize,
                    actionScannerLoadingResults = actionScannerLoadingResults

                )
            }, cameraExecutor)

            previewView
        }

    )

    SurfaceViewOverlay(
        modifier = modifier,
    )

}





private fun bindPreview(
    cameraLifecycleOwner: LifecycleOwner,
    cameraPreviewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraExecutor: Executor,
    boxSize: Size,
    actionScannerLoadingResults: ((ScannerState, String?) -> Unit)? = null,
) {

    val previewWidth = cameraPreviewView.width
    val previewHeight = cameraPreviewView.height

    val previewAspectRatio = Utils.aspectRatio(previewWidth, previewHeight)
    // TODO could be used for setTargetResolution() instead of setAspectRatio()
    //  val previewSize = Size(previewView.width, previewView.height)

    val cameraPreview = androidx.camera.core.Preview.Builder()
        .setTargetAspectRatio(previewAspectRatio)
        .build()


    Log.d("Utility", "previewView : $previewAspectRatio")

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
                    actionSetBarcodeValueAndState = actionScannerLoadingResults,
                    scannerBoundingBox = IntSize(previewWidth, previewHeight).retrieveBoxRect(boxSize)
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

@Composable
fun SurfaceViewOverlay(
    modifier: Modifier,
//    analyzer: ImageAnalysis.Analyzer? = null,
    scannerState: ScannerState = ScannerState.SCANNING
){

    val animatedProgress = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition()
    val infinitelyAnimatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    val infinitelyAnimatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing

            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(scannerState) {

        if(scannerState == ScannerState.LOADING) {

            animatedProgress.animateTo(

                targetValue = 0f,
                animationSpec = tween(durationMillis = 5000, easing = LinearEasing)

            ).endState.run {

                Log.d("Utility", "animatedProgress : Ended")
                //TODO take action on complete

            }

        }

    }

    val density = LocalDensity.current

    Canvas(modifier = modifier
        .fillMaxSize()
        .background(barcodeReticuleBackground)
    ) {

        val (canvasWidth, canvasHeight) = size
        val (boxWidth, boxHeight) = density.getBoxSize()

        with(density) {300.dp.toPx()}

        val cx = (canvasWidth - boxWidth) / 2
        val cy = (canvasHeight - boxHeight) / 2

        /* offset x moves horizontally to the right and y moves vertically down */

        drawReticuleShape(
            reticuleSize = Size(boxWidth, boxHeight),
            reticuleTopLeft = Offset(cx, cy),
            reticuleStyle = Fill
        )

        drawReticuleShape(
            reticuleSize = Size(boxWidth, boxHeight),
            reticuleTopLeft = Offset(cx, cy)
        )

        drawReticuleShape(
            reticuleColor = barcodeReticuleStroke,
            reticuleSize = Size(boxWidth, boxHeight),
            reticuleTopLeft = Offset(cx, cy),
            reticuleBlendMode = BlendMode.SrcOver
        )

        when(scannerState) {
            ScannerState.SCANNING -> {
                scale(
                    scale = infinitelyAnimatedScale
                ){


                    drawReticuleShape(
                        reticuleColor = reticuleRipple,
                        reticuleSize = Size(boxWidth, boxHeight),
                        reticuleTopLeft = Offset(cx, cy),
                        reticuleBlendMode = BlendMode.SrcOver,
                        reticuleAlpha = infinitelyAnimatedAlpha
                    )

                }
            }
            ScannerState.LOADING -> {

                loadingPath(animProgress = animatedProgress, size, density.getBoxSize())

            }
        }

    }

}