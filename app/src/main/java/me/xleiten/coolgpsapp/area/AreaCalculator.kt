package me.xleiten.coolgpsapp.area

import android.location.Location
import android.util.Log

open class AreaCalculator {
    companion object {
        fun calculateArea(areaType: AreaType, points: List<Location>): Double {
            var figure: Figure? = null
            when (areaType) {
                AreaType.TRIANGLE -> {
                    val side1 = points[0].distanceTo(points[1]).toDouble()
                    val side2 = points[1].distanceTo(points[2]).toDouble()
                    val side3 = points[2].distanceTo(points[0]).toDouble()
                    figure = Triangle("test", side1, side2, side3)
                }

                AreaType.RECTANGLE -> {
                    val width = points[0].distanceTo(points[1]).toDouble()
                    val height = points[1].distanceTo(points[2]).toDouble()
                    figure = Rectangle("test", width, height)
                }

                AreaType.SQUARE -> {
                    val size = points[0].distanceTo(points[1]).toDouble()
                    figure = Rectangle("test", size)
                }

                AreaType.SQUARE_NORMAL -> {
                    val size = points[0].distanceTo(points[1]).toDouble()
                    figure = Rectangle("test", size * 2)
                }

                AreaType.CIRCLE -> {
                    val radius = points[0].distanceTo(points[1]).toDouble()
                    figure = Oval("test", radius)
                }

                AreaType.OVAL -> {
                    val radius1 = points[0].distanceTo(points[1]).toDouble()
                    val radius2 = points[1].distanceTo(points[2]).toDouble()
                    figure = Oval("test", radius1, radius2)
                }
            }

            val result = figure.calcArea()
            if (result.isNaN()) {
                throw NumberFormatException()
            }

            return result
        }
    }
}