package test.app.areacalculator.figures

enum class FigureType(val id: Int, val pointsRequired: Int) {
    RECTANGLE(0, 3),
    SQUARE(1, 2),
    CIRCLE(2, 2),
    OVAL(3, 3),
    TRIANGLE(4, 3);

    companion object {
        fun getById(id: Int, default: FigureType = RECTANGLE): FigureType {
            for (figureType in FigureType.entries) {
                if (figureType.id == id) {
                    return figureType
                }
            }
            return default
        }
    }
}