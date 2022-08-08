object PlaceUtil {
    const val ide_scripting = "ide-scripting"

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
            private val fs = LocalFs.fs
            private fun forIde(path: String) = "$ide_scripting/$ide_scripting-${path}${fs.kts}"

            fun inModule(path: String) = listOf(place.inModule(path))
            fun inIdeScriptingRaw(path: String) = listOf(place.inIdeScripting(path))
            fun inIdeScripting(path: String) = listOf(place.inIdeScripting(forIde(path)))
            fun ktsInIdeScripting(path: String) = listOf(place.inIdeScripting(forIde(path)))
            fun inModuleAndIdeScripting(path: String) = listOf(place.inModule(path), place.inIdeScripting(path))
            fun ktsInModuleAndIdeScripting(path: String) = forIde(path).let { listOf(place.inIdeScripting(it), place.inModule(it)) }
        }

        companion object : Places {
            override val places = Fun
        }

        val places: Fun
    }
}