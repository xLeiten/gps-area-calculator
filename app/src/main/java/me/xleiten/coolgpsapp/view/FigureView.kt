package me.xleiten.coolgpsapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import me.xleiten.coolgpsapp.area.Figure

class FigureView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()

    private var figure: Figure? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.configurePaint()
        this.figure?.draw(this.context, canvas, this.paint, this.path)
    }

    private fun configurePaint() {
        this.paint.color = android.graphics.Color.WHITE
        this.paint.isAntiAlias = true
    }

    fun setFigure(figure: Figure) {
        this.figure = figure
        this.invalidate()
    }
}