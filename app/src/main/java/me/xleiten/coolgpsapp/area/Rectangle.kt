package me.xleiten.coolgpsapp.area

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import me.xleiten.coolgpsapp.R
import kotlin.math.min

class Rectangle(name: String, val width: Float, val height: Float) : Figure(name) {
    constructor(name: String, size: Float) : this(name, size, size)

    override fun calcArea(): Float {
        return this.width * this.height
    }

    override fun draw(context: Context, canvas: Canvas, paint: Paint, path: Path) {
        paint.color = context.getColor(R.color.primary)
        paint.style = Paint.Style.FILL

        val centerX = (canvas.width / 2).toFloat()
        val centerY = (canvas.height / 2).toFloat()

        val millimetersX = this.width * 1000
        val millimetersY = this.height * 1000

        val halfWidth = millimetersX / 2
        val halfHeight = millimetersY / 2

        val scaleFactorX = canvas.width / millimetersX
        val scaleFactorY = canvas.height / millimetersY
        val scale = min(scaleFactorX, scaleFactorY)

        canvas.save()
        canvas.translate(centerX, centerY);
        canvas.scale(scale, scale)
        canvas.translate(-centerX, -centerY);

        canvas.drawRect(
            centerX - halfWidth,
            centerY - halfHeight,
            centerX + halfWidth,
            centerY + halfHeight,
            paint
        )

        canvas.restore()
    }
}