//

@Suppress("unused")
object ModuleUtil {
    class Place(val parent: Parent, val path: String) {
        enum class Parent { Module, AppsSetPlace, AppsPlace, IdeScripting }

        inline val isEmpty get() = this == emptyPlace
        inline val isNotEmpty get() = !isEmpty
    }

    interface Info {
        val appsSetName: String
        val name: String
        val srcDirsCsv: String
        val relativePath: String
        val dependenciesSrcCsv: String
        val srcDirs get() = fromCsvLine(srcDirsCsv)
        val dependenciesSrc get() = fromCsvLine(dependenciesSrcCsv)
    }

    fun fromCsvLine(csvLine: String) = csvLine.split(',').map { it.trim() }
    fun placeInModule(path: String) = Place(Place.Parent.Module, path)
    fun placeInAppsSetPlace(path: String) = Place(Place.Parent.AppsSetPlace, path)
    fun placeInAppsPlace(path: String) = Place(Place.Parent.AppsPlace, path)
    fun placeInIdeScripting(path: String) = Place(Place.Parent.IdeScripting, path)
    val emptyPlace = Place(Place.Parent.Module, "")
}