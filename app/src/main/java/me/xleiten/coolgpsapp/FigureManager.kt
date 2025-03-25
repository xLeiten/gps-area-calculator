package me.xleiten.coolgpsapp

import android.location.Location
import me.xleiten.coolgpsapp.area.AreaType
import me.xleiten.coolgpsapp.area.Figure
import me.xleiten.coolgpsapp.area.Oval
import me.xleiten.coolgpsapp.area.Position
import me.xleiten.coolgpsapp.area.Rectangle
import me.xleiten.coolgpsapp.area.Triangle

open class FigureManager {
    companion object {
        fun getFigure(areaType: AreaType, points: List<Location>): Figure {
            var figure: Figure? = null
            when (areaType) {
                AreaType.TRIANGLE -> {
                    val side1 = points[0].distanceTo(points[1])
                    val side2 = points[1].distanceTo(points[2])
                    val side3 = points[2].distanceTo(points[0])
                    figure = Triangle("test", side1, side2, side3)
                }

                AreaType.RECTANGLE -> {
                    val width = points[0].distanceTo(points[1])
                    val height = points[1].distanceTo(points[2])
                    figure = Rectangle("test", width, height)
                }

                AreaType.SQUARE -> {
                    val size = points[0].distanceTo(points[1])
                    figure = Rectangle("test", size)
                }

                AreaType.SQUARE_NORMAL -> {
                    val size = points[0].distanceTo(points[1])
                    figure = Rectangle("test", size * 2)
                }

                AreaType.CIRCLE -> {
                    val radius = points[0].distanceTo(points[1])
                    figure = Oval("test", radius)
                }

                AreaType.OVAL -> {
                    val radius1 = points[0].distanceTo(points[1])
                    val radius2 = points[1].distanceTo(points[2])
                    figure = Oval("test", radius1, radius2)
                }
            }

            return figure
        }

        fun convertCoordinates(points: List<Position>, locationProvider: String): List<Location> {
            return points.map {
                Location(locationProvider).apply {
                    this.latitude = it.latitude
                    this.longitude = it.longitude
                }
            }
        }
    }
}