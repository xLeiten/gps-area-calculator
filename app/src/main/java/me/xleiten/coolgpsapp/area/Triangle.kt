package me.xleiten.coolgpsapp.area

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import me.xleiten.coolgpsapp.R
import kotlin.math.sqrt

class Triangle(name: String, val side1: Float, val side2: Float, val side3: Float) :
    Figure(name) {
    override fun calcArea(): Float {
        val s = (this.side1 + this.side2 + this.side3) / 2
        return sqrt(s * (s - this.side1) * (s - this.side2) * (s - this.side3))
    }

    override fun draw(context: Context, canvas: Canvas, paint: Paint, path: Path) {
        paint.color = context.getColor(R.color.primary)
        paint.style = Paint.Style.FILL

        if (!isValidTriangle(this.side1, this.side2, this.side3)) {
            return
        }

        val (triangleWidth, triangleHeight) = calculateTriangleDimensions(
            this.side1,
            this.side2,
            this.side3
        )

        val scaleFactor =
            calculateScalingFactor(triangleWidth, triangleHeight, canvas.width, canvas.height)

        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)

        val centerX = canvas.width / (2 * scaleFactor)
        val centerY = canvas.height / (2 * scaleFactor)
        val vertices =
            calculateTriangleVertices(this.side1, this.side2, this.side3, centerX, centerY)

        val path = Path().apply {
            moveTo(vertices[0].x, vertices[0].y)
            lineTo(vertices[1].x, vertices[1].y)
            lineTo(vertices[2].x, vertices[2].y)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    private fun isValidTriangle(a: Float, b: Float, c: Float): Boolean {
        return a + b > c && a + c > b && b + c > a
    }

    private fun calculateTriangleDimensions(a: Float, b: Float, c: Float): Pair<Float, Float> {
        val s = (a + b + c) / 2
        val area = sqrt(s * (s - a) * (s - b) * (s - c))
        val height = (2 * area) / a
        return Pair(a, height)
    }

    private fun calculateScalingFactor(
        triangleWidth: Float,
        triangleHeight: Float,
        canvasWidth: Int,
        canvasHeight: Int
    ): Float {
        val widthScale = canvasWidth / triangleWidth
        val heightScale = canvasHeight / triangleHeight
        return minOf(widthScale, heightScale)
    }

    private fun calculateTriangleVertices(
        a: Float,
        b: Float,
        c: Float,
        centerX: Float,
        centerY: Float
    ): Array<PointF> {
        val halfA = a / 2
        val height = (2 * this.calcArea()) / a

        val point1 = PointF(centerX - halfA, centerY + height / 2)
        val point2 = PointF(centerX + halfA, centerY + height / 2)
        val point3 = PointF(centerX, centerY - height / 2)

        return arrayOf(point1, point2, point3)
    }

    data class PointF(val x: Float, val y: Float)
}