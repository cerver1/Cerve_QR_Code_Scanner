package com.cerve.co.cerveqrcodescanner

import android.graphics.RectF
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanner
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Utils {

    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0

    fun BarcodeScanner?.stopScanner() {
        if (this != null) {
            try {
                this.close()
            } catch (e: Exception) {

            }
        }
    }

    fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun Density.getBoxSize(): Size {

        val boxWidth = with(this) { 300.dp.toPx() }
        val boxHeight = with(this) { 250.dp.toPx() }

        return Size(boxWidth, boxHeight)

    }

    fun PreviewView?.toIntSize() : IntSize {
        return this?.let {

            IntSize(this.width, this.height)

        } ?: IntSize.Zero

    }

    fun IntSize?.retrieveBoxRect(size: Size): RectF {

        return this?.let {

            val (canvasWidth, canvasHeight) = this

            val (boxWidth, boxHeight) = size

            val leftB = ((canvasWidth - boxWidth) / 2)
            val topB = ((canvasHeight - boxHeight) / 2)

            val rightB = ((canvasWidth + boxWidth) / 2)
            val bottomB = ((canvasHeight + boxHeight) / 2)

           RectF(leftB,topB,rightB,bottomB)

        } ?: RectF()

    }

}