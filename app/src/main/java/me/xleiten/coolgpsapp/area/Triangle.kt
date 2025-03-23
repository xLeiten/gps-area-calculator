package me.xleiten.coolgpsapp.area

import kotlin.math.sqrt

class Triangle(name: String, val side1: Double, val side2: Double, val side3: Double) :
    Figure(name) {
    override fun calcArea(): Double {
        val s = (this.side1 + this.side2 + this.side3) / 2
        return sqrt(s * (s - this.side1) * (s - this.side2) * (s - this.side3))
    }
}