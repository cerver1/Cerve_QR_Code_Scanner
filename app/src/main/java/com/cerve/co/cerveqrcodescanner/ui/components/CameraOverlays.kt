package com.cerve.co.cerveqrcodescanner.ui.components

import android.util.Log
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cerve.co.cerveqrcodescanner.Utils.getBoxSize
import com.cerve.co.cerveqrcodescanner.models.ScannerState
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleBackground
import com.cerve.co.cerveqrcodescanner.ui.theme.barcodeReticuleStroke
import com.cerve.co.cerveqrcodescanner.ui.theme.reticuleRipple

@Composable
fun SurfaceViewOverlay(
    modifier: Modifier,
    currentScannerState: ScannerState? = null,
    actionSetScannerCompletionState: ((ScannerState) -> Unit)? = null
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

    LaunchedEffect(currentScannerState) {

        if(currentScannerState == ScannerState.LOADING) {

            animatedProgress.animateTo(

                targetValue = 0f,
                repeatable(iterations = 2, animation = tween(durationMillis = 4000, easing = LinearEasing))

            ).endState.run {

                /**
                 * This lets us take action after the loading animation has been completed
                 */
                actionSetScannerCompletionState?.let {
                    actionSetScannerCompletionState(ScannerState.COMPLETE)
                }


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

        when (currentScannerState) {
            ScannerState.SCANNING, null -> {

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
            else -> Unit
        }

    }

}