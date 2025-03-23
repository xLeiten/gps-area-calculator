package me.xleiten.coolgpsapp.area

import kotlin.math.PI

class Oval(name: String, val radius1: Double, val radius2: Double): Figure(name) {
    constructor(name: String, radius: Double): this(name, radius, radius)

    override fun calcArea(): Double {
        return this.radius1 * this.radius2 * PI
    }
}