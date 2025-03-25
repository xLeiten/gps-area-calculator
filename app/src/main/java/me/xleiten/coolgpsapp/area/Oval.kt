package me.xleiten.coolgpsapp.area

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import me.xleiten.coolgpsapp.R
import kotlin.math.PI
import kotlin.math.min

class Oval(name: String, val radius1: Float, val radius2: Float) : Figure(name) {
    constructor(name: String, radius: Float) : this(name, radius, radius)

    override fun calcArea(): Float {
        return (this.radius1 * this.radius2 * PI).toFloat()
    }

    override fun draw(context: Context, canvas: Canvas, paint: Paint, path: Path) {
        paint.color = context.getColor(R.color.primary)
        paint.style = Paint.Style.FILL

        val centerX = (canvas.width / 2).toFloat()
        val centerY = (canvas.height / 2).toFloat()

        val millimetersX = this.radius1 * 1000 * 2
        val millimetersY = this.radius2 * 1000 * 2

        val halfWidth = millimetersX / 2
        val halfHeight = millimetersY / 2

        val scaleFactorX = canvas.width / millimetersX
        val scaleFactorY = canvas.height / millimetersY
        val scale = min(scaleFactorX, scaleFactorY)

        canvas.save()
        canvas.translate(centerX, centerY);
        canvas.scale(scale, scale)
        canvas.translate(-centerX, -centerY);

        canvas.drawOval(
            centerX - halfWidth,
            centerY - halfHeight,
            centerX + halfWidth,
            centerY + halfHeight,
            paint
        )

        canvas.restore()
    }
}