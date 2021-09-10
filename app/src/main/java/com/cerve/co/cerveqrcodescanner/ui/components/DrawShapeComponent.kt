package com.cerve.co.cerveqrcodescanner.ui.components

import android.graphics.Point
import android.graphics.PointF
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cerve.co.cerveqrcodescanner.ui.theme.boxCornerRadius
import com.cerve.co.cerveqrcodescanner.ui.theme.strokeWidth
import com.google.android.material.color.MaterialColors
import kotlin.math.abs

fun DrawScope.drawReticuleShape(
    reticuleColor: Color = Color.Unspecified,
    reticuleSize: Size = Size(0F, 0F),
    reticuleTopLeft: Offset = Offset(0F, 0F),
    reticuleStyle: DrawStyle = Stroke(strokeWidth.toPx()),
    reticuleBlendMode: BlendMode = BlendMode.Clear,
    reticuleCornerRadius: CornerRadius = CornerRadius(boxCornerRadius.toPx()),
    reticuleAlpha: Float = 1f
) {

    drawRoundRect(
        color = reticuleColor,
        topLeft = reticuleTopLeft,
        size = reticuleSize,

        style = reticuleStyle,
        blendMode = reticuleBlendMode,
        cornerRadius = reticuleCornerRadius,
        alpha = reticuleAlpha
    )

}


fun DrawScope.loadingPath(
    animProgress: Animatable<Float, AnimationVector1D>,
    animColor: Color,
    canvasSize: Size,
    boxSize: Size
) {

    val (canvasWidth, canvasHeight) = canvasSize
    val (boxWidth, boxHeight) = boxSize

    val cx = (canvasWidth - boxWidth) / 2
    val cy = (canvasHeight - boxHeight) / 2

    val cPlusX = (canvasWidth + boxWidth) / 2
    val cPlusY = (canvasHeight + boxHeight) / 2

    val boxClockwiseCoordinates: Array<PointF> = arrayOf(
        PointF(cx, cy),
        PointF(cPlusX, cy),
        PointF(cPlusX, cPlusY),
        PointF(cx, cPlusY),
    )

    val coordinateOffsetBits: Array<Point> = arrayOf(
        Point(1, 0),
        Point(0, 1),
        Point(-1, 0),
        Point(0, -1)
    )

    val boxPerimeter = (boxWidth + boxHeight) * 2
    val path = Path()
    val lastPathPoint = PointF()
    // The distance between the box's left-top corner and the starting point of white colored path.
    var offsetLen = boxPerimeter * animProgress.value % boxPerimeter
    var i = 0
    while (i < 4) {
        val edgeLen = if (i % 2 == 0) boxWidth else boxHeight
        if (offsetLen <= edgeLen) {
            lastPathPoint.x = boxClockwiseCoordinates[i].x + coordinateOffsetBits[i].x * offsetLen
            lastPathPoint.y = boxClockwiseCoordinates[i].y + coordinateOffsetBits[i].y * offsetLen
            path.moveTo(lastPathPoint.x, lastPathPoint.y)
            break
        }

        offsetLen -= edgeLen
        i++
    }

    // Computes the path based on the determined starting point and path length.
    var pathLen = boxPerimeter * 0.3f
    for (j in 0..3) {
        val index = (i + j) % 4
        val nextIndex = (i + j + 1) % 4
        // The length between path's current end point and reticule box's next coordinate point.
        val lineLength = abs(boxClockwiseCoordinates[nextIndex].x - lastPathPoint.x) +
                abs(boxClockwiseCoordinates[nextIndex].y - lastPathPoint.y)

        Log.d("Utility", "lineLen : $lineLength")

        if (lineLength >= pathLen) {
            Log.d("Utility", "lineLen : $lineLength")
            path.lineTo(
                lastPathPoint.x + pathLen * coordinateOffsetBits[index].x,
                lastPathPoint.y + pathLen * coordinateOffsetBits[index].y
            )
            break
        }

        lastPathPoint.x = boxClockwiseCoordinates[nextIndex].x
        lastPathPoint.y = boxClockwiseCoordinates[nextIndex].y
        path.lineTo(lastPathPoint.x, lastPathPoint.y)
        pathLen -= lineLength
    }

    drawPath(
        path = path,
        color = animColor,
        style = Stroke(
            width = 4.dp.toPx(),
            pathEffect = PathEffect.cornerPathEffect(8.dp.toPx())),
        alpha = animProgress.value
    )

}