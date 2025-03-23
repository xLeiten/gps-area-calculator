package me.xleiten.coolgpsapp.area

class Rectangle(name: String, val width: Double, val height: Double) : Figure(name) {
    constructor(name: String, size: Double) : this(name, size, size)

    override fun calcArea(): Double {
        return this.width * this.height
    }
}