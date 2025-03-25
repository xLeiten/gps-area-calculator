package test.app.areacalculator.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import test.app.areacalculator.figures.Figure

class FigureView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()

    private var figure: Figure? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.paint.isAntiAlias = true
        this.figure?.draw(this.context, canvas, this.paint, this.path)
    }

    fun setFigure(figure: Figure) {
        this.figure = figure
        this.invalidate()
    }
}