package me.xleiten.coolgpsapp.area

// Связывает айди фигуры в выпадающем списке с типом этой фигуры
enum class AreaType(val id: Int, val pointsRequired: Int) {
    TRIANGLE(0, 3),
    RECTANGLE(1, 3),
    CIRCLE(2, 2),
    OVAL(3, 3),
    SQUARE(4, 2),
    SQUARE_NORMAL(5, 2);

    companion object {
        fun getById(id: Int, default: AreaType = AreaType.RECTANGLE): AreaType {
            for (figureType in AreaType.entries) {
                if (figureType.id == id) {
                    return figureType
                }
            }
            return default
        }
    }
}