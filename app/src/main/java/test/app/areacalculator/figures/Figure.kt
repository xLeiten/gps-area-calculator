package test.app.areacalculator.figures

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

abstract class Figure {
    abstract fun calcArea(): Float

    abstract fun draw(context: Context, canvas: Canvas, paint: Paint, path: Path)
}