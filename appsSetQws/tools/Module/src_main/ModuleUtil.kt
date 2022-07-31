//

@Suppress("unused")
object ModuleUtil {
    class Place(val parent: Parent, val path: String) {
        enum class Parent { Module, AppsSetPlace, AppsPlace, IdeScripting }

        inline val isEmpty get() = this == PlaceTool.emptyPlace
        inline val isNotEmpty get() = !isEmpty
    }

    interface PlaceTool {
        companion object {
            val emptyPlace = Place(Place.Parent.Module, "")
            fun inModule(path: String) = Place(Place.Parent.Module, path)
            fun inAppsSetPlace(path: String) = Place(Place.Parent.AppsSetPlace, path)
            fun inAppsPlace(path: String) = Place(Place.Parent.AppsPlace, path)
            fun inIdeScripting(path: String) = Place(Place.Parent.IdeScripting, path)
        }

        val place: Companion
    }

    object PlaceToolHolder : PlaceTool {
        override val place = PlaceTool.Companion
    }

    interface Places {
        object Fun : PlaceTool by PlaceToolHolder {
            fun inModule(path: String) = listOf(place.inModule(path))
            fun inIdeScripting(path: String) = listOf(place.inIdeScripting(path))
            fun inModuleAndIdeScripting(path: String) = listOf(place.inModule(path), place.inIdeScripting(path))
        }

        companion object : Places {
            override val places = Fun
        }

        val places: Fun
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

}