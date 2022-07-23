//
object RunScriptStr {
    class HashedContent(val name: String, val content: String) {
        val sha1sum = Util.sha1sum(content) ?: TODO()
    }

    @Suppress("ArrayInDataClass")
    data class Conf(
        val chanelId: Int,
        val needBindings: Boolean,
        val needScriptPath: Boolean,
        val saveToPlace: Array<ModuleUtil.Place>,
        val needSaveToFileDebug: Boolean,
        val debugCase_fromFile: Boolean,
        val forRuntime: ForRuntime = ForRuntime()
    ) {
        companion object {
            const val ide_scripting = "ide-scripting"
            const val dummyChanelId = ToolSharedConfig.chanelIdDummy
            fun placeInModule(path: String) = arrayOf(ModuleUtil.placeInModule(path))
            fun placeInIdeScripting(path: String) = arrayOf(ModuleUtil.placeInIdeScripting(path))
            fun placeInModuleAndIdeScripting(path: String) = arrayOf(ModuleUtil.placeInModule(path), ModuleUtil.placeInIdeScripting(path))
            val conf get() = conf()
            fun conf(
                chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener,
                needBindings: Boolean = true,
                needScriptPath: Boolean = false,
                needSaveToFileDebug: Boolean = false,
                saveToPlace: Array<ModuleUtil.Place> = emptyArray(),
                debugCase_fromFile: Boolean = false,

                args: Array<String> = emptyArray(),
            ) = Conf(
                chanelId = chanelId,
                needBindings = needBindings,
                needScriptPath = needScriptPath,
                saveToPlace = saveToPlace,
                needSaveToFileDebug = needSaveToFileDebug,
                debugCase_fromFile = debugCase_fromFile,
                forRuntime = ForRuntime(args = args)
            )
        }

        data class ForRuntime(
            val args: Array<String> = emptyArray(),
            val scriptPath: String = ""
        )

        val scriptStrEnvText
            get() = """object $scriptStrEnv {
    val scriptPath ${if (needScriptPath) "by lazy { \"${forRuntime.scriptPath}\" }" else "= \"\""}
    val args by lazy { arrayOf<String>(${forRuntime.args.joinToString { "\"$it\"" }}) }
}"""
    }

    private val scriptStrEnv = ScriptStrRunEnv::class.simpleName ?: TODO()
    private val ignore = setOf(
        "TypeAlias.kt",
        this::class.simpleName?.plus(Fs.kt) ?: TODO(),
        scriptStrEnv + Fs.kt,
    )

    private const val funMainArgs = "@JvmStatic\n    fun main(args: Array<String>) = RunScriptStr(ModuleInfo)"

    operator fun invoke(moduleInfo: ModuleUtil.Info) =
        runScriptStr(moduleInfo, Conf.conf())

    operator fun invoke(moduleInfo: ModuleUtil.Info, block: Conf.Companion.() -> Conf) =
        runScriptStr(moduleInfo, block(Conf.Companion))

    private fun runScriptStr(moduleInfo: ModuleUtil.Info, config: Conf) {
        val caller = Thread.currentThread().stackTrace[3]
        val className: String = caller.className
        val methodName: String = caller.methodName
        val fileName: String? = caller.fileName
        assert("main" == methodName)
        val ktFile = with(Fs) {
            val appsPlace = currentDir.lookupToParentByName(BuildDescConst.app__init)?.up ?: TODO()
            val appsSetPlace = appsPlace file moduleInfo.appsSetName
            val modulePlace = appsSetPlace file moduleInfo.relativePath
            val ktFileName = fileName ?: (className + kt)
            config.forRuntime.args.firstOrNull()?.let { _file(it) }
                ?.takeIf { it.exists() && ktFileName == it.name }
                ?: moduleInfo.srcDirs.firstNotNullOf { modulePlace.file(it).listFiles.firstOrNull { f -> ktFileName == f.name } }
        }

        val fullConf = config.copy(forRuntime = config.forRuntime.copy(scriptPath = ktFile.absolutePath))
        val scriptStrByModuleInfo = fromModuleInfo(className, moduleInfo, fullConf)
        if (config.debugCase_fromFile) {
            val scriptStrByFile = fromFile(fullConf)
            assert(scriptStrByFile == scriptStrByModuleInfo)
        }
        if (config.chanelId != Conf.dummyChanelId) {
            val res = LocalHostSocket.uds(config.chanelId).send(scriptStrByModuleInfo)
            println("RunScriptStr[on app console]: run result  '$res'")
        }
    }

    fun fromModuleInfo(className: String, moduleInfo: ModuleUtil.Info, conf: Conf) = with(Fs) {
        val appsPlace = currentDir.lookupToParentByName(BuildDescConst.app__init)?.up ?: TODO()
        val appsSetPlace = appsPlace file moduleInfo.appsSetName
        val modulePlace = appsSetPlace file moduleInfo.relativePath
        val ktFileName = className + kt
        val ktFile = moduleInfo.srcDirs.firstNotNullOf { modulePlace.file(it).listFiles.firstOrNull { f -> ktFileName == f.name } }
        val moduleInfoFile = modulePlace.file(BuildDescConst.src_module_info).file(BuildDescConst.ModuleInfo + kt)
        val scriptFromFile = ktFile.readText().split(funMainArgs)[0].trim().plus("\n}")
        val additionalObjects = moduleInfo.dependenciesSrc.flatMap { dependencySrcRelativePath ->
            appsSetPlace.file(dependencySrcRelativePath).listFiles
                .filter { it.isFile && it.extension == extensionKt && it.name !in ignore }
        }.map { HashedContent(it.name, it.readText()) }
        assembleScript(
            appsSetPlace.file("tmp/__all_ide_TypeAlias.kt").readText(),
            additionalObjects,
            moduleInfoFile.readText(),
            scriptFromFile,
            ktFile.nameWithoutExtension,
            conf
        ).also {
            if (conf.needSaveToFileDebug) appsSetPlace.file("/tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFileName}.StrFromMInfo$kts") update it
            conf.saveToPlace.forEach { place ->
                when (place.parent) {
                    ModuleUtil.Place.Parent.Module -> modulePlace.file(place.path) update it
                    ModuleUtil.Place.Parent.AppsSetPlace -> appsSetPlace.file(place.path) update it
                    ModuleUtil.Place.Parent.AppsPlace -> appsPlace.file(place.path) update it
                    ModuleUtil.Place.Parent.IdeScripting -> {
                        appsPlace.lookupToParentByName(BuildDescConst.app__init)?.let { appInit ->
                            appInit.file(BuildDescConst.conf_place_of_ide_scripting).run {
                                if (exists()) {
                                    val ideScriptingPath = readText().trim()
                                    val ideScripting = _file(ideScriptingPath)
                                    if (ideScriptingPath.isNotEmpty() && ideScripting.exists() && ideScripting.isDirectory) {
                                        ideScripting.file(place.path) update it
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun fromFile(conf: Conf) = with(Fs) {
        val ktFile = _file(conf.forRuntime.scriptPath)
        val ktFileName = ktFile.name
        val moduleInfoSrcDir = ktFile.lookupToParentByName(BuildDescConst.src_module_info) ?: TODO()
        val modulePlace = moduleInfoSrcDir.up
        val settingsGradleKts = modulePlace.lookupToParentByName(BuildDescConst.settings_gradle_kts) ?: TODO()
        val appsSetPlace = settingsGradleKts.up
        val moduleInfoFile = moduleInfoSrcDir.file(BuildDescConst.ModuleInfo + kt)
        val scriptFromFile = ktFile.readText().split(funMainArgs)[0].trim().plus("\n}")
        val additionalObjects = modulePlace.file(BuildDescConst.dependencies_src_txt).readLines()
            .flatMap { dependencySrcRelativePath ->
                appsSetPlace.file(dependencySrcRelativePath.trim()).listFiles
                    .filter { it.isFile && it.extension == extensionKt && it.name !in ignore }
            }.map { HashedContent(it.name, it.readText()) }
        val scriptStr = assembleScript(
            appsSetPlace.file("tmp/__all_ide_TypeAlias.kt").readText(),
            additionalObjects,
            moduleInfoFile.readText(),
            scriptFromFile,
            ktFile.nameWithoutExtension,
            conf
        )
        if (conf.needSaveToFileDebug) appsSetPlace.file("/tmp/_${ktFileName}.StrFromFile$kts") update scriptStr
        return@with scriptStr
    }

    fun assembleScript(
        typeAliasText: String,
        additionalObjects: List<HashedContent>,
        moduleInfoObjectText: String,
        objectText: String,
        objectName: String,
        conf: Conf,
    ) = """
$typeAliasText
${additionalObjects.joinToString("\n") { it.content }}
$moduleInfoObjectText
${conf.scriptStrEnvText}
$objectText

//script run result string
val res = $objectName.script(${if (conf.needBindings) "mapOf<String, Any?>(\"bindings\" to bindings)" else "emptyMap()"})
res
"""
}