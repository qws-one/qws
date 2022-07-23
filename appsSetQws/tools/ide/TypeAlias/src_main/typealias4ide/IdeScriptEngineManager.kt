package typealias4ide

class IdeScriptEngineManager {
    class EngineInfo {
        val fileExtensions = ""
    }

    fun getEngine(engineInfo: EngineInfo, classLoader: ClassLoader?): IdeScriptEngine? = null

    @Suppress("SpellCheckingInspection")
    val engineInfos = emptyList<EngineInfo>()

    companion object {
        fun getInstance(): IdeScriptEngineManager = TODO()
    }
}


