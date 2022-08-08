@Suppress("ObjectPropertyName")
object RunScriptStrFull : UtilBase(), LocalFs.Is by LocalFs, LocalFs.Is.Fs by LocalFs.fs, BuildDescConstPlus by BuildDescConstPlus, BuildDescGen by BuildDescGen {
    private val RunScriptStr.ConfBase.scriptStrEnvText
        get() = """object $scriptStrEnv {
    val scriptPath ${if (runEnv.needScriptPath) "by lazy { \"${forRuntime.scriptPath}\" }" else "= \"\""}
    val appsPlacePath ${if (runEnv.needAppsPlacePath) "by lazy { \"${forRuntime.appsPlacePath}\" }" else "= \"\""}
    val tmpDirBig ${if (runEnv.needTmpDirBig) "by lazy { \"${forRuntime.tmpDirBig}\" }" else "= \"\""}
    val tmpDirQuick ${if (runEnv.needTmpDirQuick) "by lazy { \"${forRuntime.tmpDirQuick}\" }" else "= \"\""}
    val args by lazy { listOf<String>(${forRuntime.args.joinToString { "\"$it\"" }}) }
}"""
    private val RunScriptStr.Conf.scriptArgs
        get() : String {
            val bindings by stringOfName { if (needBindings) "\"$it\" to $it" else "" }
            val tmpDirBig by stringOfName { if (forRuntime.tmpDirBig.isNotEmpty()) """"$it" to "${forRuntime.tmpDirBig}"""" else "" }
            val tmpDirQuick by stringOfName { if (forRuntime.tmpDirQuick.isNotEmpty()) """"$it" to "${forRuntime.tmpDirQuick}"""" else "" }
            val scriptPath by stringOfName { if (runEnv.needScriptPath) """"$it" to "${forRuntime.scriptPath}"""" else "" }
            val appsPlacePath by stringOfName { if (runEnv.needAppsPlacePath) """"$it" to "${forRuntime.appsPlacePath}"""" else "" }
            val scriptName by stringOfName { forRuntime.scriptName.let { value -> if (value.isNotEmpty()) """"$it" to "$value"""" else "" } }
            val args by stringOfName { name ->
                when {
                    needArgs && forRuntime.args.isNotEmpty() -> "\"$name\" to listOf(${forRuntime.args.joinToString { "\"$it\"" }})"
                    needArgs && forRuntime.args.isEmpty() -> "\"$name\" to emptyList<String>()"
                    else -> ""
                }
            }
            val list = listOfValid(
                bindings,
                tmpDirBig,
                tmpDirQuick,
                scriptPath,
                appsPlacePath,
                scriptName,
                args,
            ) { isNotEmpty() }
            return if (list.isNotEmpty()) "mapOf(${list.joinToString()})" else "emptyMap()"
        }

    private const val _main = "main"
    private const val _args = "args"
    private fun funMainArgs(arg: String) = "@JvmStatic\n    fun $_main($_args: Array<String>) = ${RunScriptStr::class.java.name}($arg)"
    private val funMainModuleInfo by lazy { funMainArgs(BuildDescConst.ModuleInfo) }
    private val funMainArgs by lazy { funMainArgs(_args) }
    private const val allIdeTypeAlias = "tmp/__all_ide_TypeAlias.kt"
    private val tempDir by lazy { kotlin.io.path.Path(System.getProperty("user.home"), ".cache/").toAbsolutePath().toString() }

    private val defaultTmpDirBig by lazy { kotlin.io.path.Path(tempDir, "qws_tmpBig").toAbsolutePath().toString() }
    private val defaultTmpDirQuick by lazy { kotlin.io.path.Path(tempDir, "qws_tmpQuick").toAbsolutePath().toString() }

    private val scriptStrEnv = ScriptStrRunEnv::class.simpleName ?: TODO()
    private val ignore = setOf(
        descUnit.TypeAlias.ignoreSrcFileName,
        descUnit.BaseTypeAlias.name + kt,
        //this::class.simpleName?.plus(kt) ?: TODO(),
        scriptStrEnv + kt,
    )

    val appInitDir get() = currentDir.lookupToParentByName(BuildDescConst.app__init) ?: TODO()
    val ModuleUtil.Info.appsPlaces: List<FsPath>
        get() {
            val appInit = appInitDir
            val appInitPlace = appInit.fsPath
            val appsPlace = appInit.up?.fsPath ?: TODO()
            val appsSetPlace = appsPlace fsPath this.appsSetName
            val modulePlace = appsSetPlace fsPath this.relativePath
            return listOf(appsPlace, appInitPlace, appsSetPlace, modulePlace)
        }

    fun appsPlacesFromFile(scriptPath: String): List<FsPath> {
        val ktFsPath = FsPath.from(scriptPath)
        val moduleInfoSrcDir = ktFsPath.file.lookupToParentByName(BuildDescConst.src_module_info) ?: TODO()
        val modulePlace = moduleInfoSrcDir.up ?: TODO()
        val appsSetPlace = modulePlace.lookupToParentByName(BuildDescConst.settings_gradle_kts)?.up ?: TODO()
        val appInitDir = appsSetPlace.lookupToParentByName(app__init) ?: TODO()
        val appsPlace = appInitDir.up ?: TODO()
        //val moduleInfoFile = moduleInfoSrcDir.file(BuildDescConst.ModuleInfo + kt)
        return listOf(
            appsPlace.fsPath,
            appInitDir.fsPath,
            appsSetPlace.fsPath,
            modulePlace.fsPath,
            moduleInfoSrcDir.fsPath
        )
    }

    private fun String.saveTo(places: List<PlaceUtil.Place>, modulePlace: FsPath) {
        val str = this
        val moduleDir = modulePlace.file
        val appsSetPlace by lazy { moduleDir.lookupToParentByName(BuildDescConst.settings_gradle_kts)?.up ?: TODO() }
        val appsPlace by lazy { moduleDir.lookupToParentByName(app__init)?.up ?: TODO() }
        val ideScriptingPath by lazy { appsPlace.file(app__init).file(conf_place.of_ide_scripting).fromFile }

        places.forEach { place ->
            when (place.parent) {
                PlaceUtil.Place.Parent.Module -> modulePlace.file(place.path) update str
                PlaceUtil.Place.Parent.AppsSetPlace -> appsSetPlace.file(place.path) update str
                PlaceUtil.Place.Parent.AppsPlace -> appsPlace.file(place.path) update str
                PlaceUtil.Place.Parent.IdeScripting -> {
                    if (ideScriptingPath.valid) {
                        val ideScripting = ideScriptingPath.file
                        if (ideScripting.exists() && ideScripting.isDirectory) ideScripting.file(place.path) update str
                    }
                }
            }
        }
    }

    fun <T : RunScriptStr.ConfBase> updateRunEnv(appInitFsPath: FsPath, conf: RunScriptStr.ConfBase): T {
        val forRuntime = conf.forRuntime.copy(
            appsPlacePath = if (conf.runEnv.needAppsPlacePath) appInitFsPath.file.up?.absolutePath ?: TODO() else "",
            tmpDirBig = if (conf.runEnv.needTmpDirBig) appInitFsPath.file(conf_place.of__tmp_dir_big).fromFile
                .path.ifEmpty { defaultTmpDirBig } else "",
            tmpDirQuick = if (conf.runEnv.needTmpDirQuick) appInitFsPath.file(conf_place.of__tmp_dir_quick).fromFile
                .path.ifEmpty { defaultTmpDirQuick } else "",
        )
        @Suppress("UNCHECKED_CAST") return when (conf) {
            is RunScriptStr.ConfLite -> conf.copy(forRuntime = forRuntime)
            is RunScriptStr.Conf -> conf.copy(forRuntime = forRuntime)
            else -> TODO()
        } as T
    }

    private fun runScriptStr(scriptStr: String, config: RunScriptStr.Conf, tmpDir: () -> String) {
        println("Full.runScriptStr scriptPath=${config.forRuntime.scriptPath}")
        if (config.chanelId != RunScriptStr.Conf.dummyChanelId) {
            LocalHostSocket.configureToSystemOut()
            val socket = if (config.runEnv.needTmpDirQuick) {
                LocalHostSocket.uds(tmpDir(), config.chanelId)
            } else LocalHostSocket.uds(config.chanelId)
            val res = socket.send(scriptStr)
            println("RunScriptStr[on app console]: run result  '$res'")
        }
    }

    fun runScriptStr(moduleInfo: ModuleUtil.Info, config: RunScriptStr.Conf) {
        val caller = Thread.currentThread().stackTrace[3]
        val className: String = caller.className
        assert(_main == caller.methodName)
        val ktFile = (caller.fileName ?: (className + kt)).let { ktFileName ->
            config.forRuntime.args.firstOrNull()?.run { fsPath.file }?.takeIf { it.exists() && ktFileName == it.name } ?: moduleInfo.run {
                val (_, _, _, modulePlace) = appsPlaces
                srcDirs.firstNotNullOf { modulePlace.file(it).listFiles.firstOrNull { f -> ktFileName == f.name } }
            }
        }
        val fullConf = config.copy(forRuntime = config.forRuntime.copy(scriptPath = ktFile.absolutePath))
        val scriptStrByModuleInfo = fromModuleInfo(className, moduleInfo, fullConf)
        if (config.debugCase_fromFile) {
            val (_, scriptStrByFile) = fromFile(fullConf, funMainModuleInfo)
            assert(scriptStrByFile == scriptStrByModuleInfo)
        }
        runScriptStr(scriptStrByModuleInfo, fullConf) {
            val (_, appInitPlace, _, _) = moduleInfo.appsPlaces
            appInitPlace.file(conf_place.of__tmp_dir_quick).fromFile.path
        }
    }

    fun runScriptStr(config: RunScriptStr.Conf) {
        val caller = Thread.currentThread().stackTrace[3]
        val className: String = caller.className
        assert(_main == caller.methodName)
        val ktFileFsPath: FsPath = (caller.fileName ?: (className + kt)).let { fileName ->
            config.forRuntime.args.firstOrNull()?.run { fsPath.file }?.takeIf { it.exists() && fileName == it.name }?.fsPath
                ?: findFirstFileBy(fileName, config.uniqueString) ?: TODO()
        }
        val fullConf = config.copy(forRuntime = config.forRuntime.copy(scriptPath = ktFileFsPath.path))
        val (appInitPlace, scriptStrByFile) = fromFile(fullConf, funMainArgs)
        runScriptStr(scriptStrByFile, fullConf) { appInitPlace.file(conf_place.of__tmp_dir_quick).fromFile.path }
    }

    private fun FsPath.walk() = file.walk().onEnter {when (it.name) {//@formatter:off
                        "build", ".idea", ".git" -> { println("skip walk: $it"); false}
                        else -> true }
                    }//@formatter:on

    private fun findFirstFileBy(fileName: String, uniqueString: String): FsPath? {
        fun findFirstFileBy(walk: FileTreeWalk, fileName: String, uniqueString: String): FsPath? {
            walk.forEach {
                if (it.isFile && fileName == it.name) {
                    val content = it.readText()
                    if (content.contains(funMainArgs) && (uniqueString.isEmpty() || content.split(funMainArgs)[1].contains(uniqueString))) return it.fsPath
                }
            }
            return null
        }
        return findFirstFileBy(currentDir.fsPath.walk(), fileName, uniqueString)
            ?: currentDir.lookupToParentByName(BuildDescConst.app__init).let { appInitDir ->
                val appsPlace = appInitDir?.up?.fsPath ?: TODO()
                findFirstFileBy(appsPlace.walk(), fileName, uniqueString)
            }
    }

    fun List<String>.additionalObjects(conf: RunScriptStr.Conf, appsSetPlace: FsPath) =
        filterNot { conf.withoutRunScriptStr && it == descUnit.RunScriptStr.srcDir }.flatMap { dependencySrcRelativePath ->
            if (descUnit.TypeAlias.srcDir == dependencySrcRelativePath) conf.hasTypeAliasModule = true
            if (descUnit.ScriptStrRunEnv.srcDir == dependencySrcRelativePath) conf.hasUnitScriptStrRunEnv = true
            appsSetPlace.file(dependencySrcRelativePath.trim()).listFiles.let {
                if (descUnit.LocalHostSocket.srcDir == dependencySrcRelativePath) it + appsSetPlace.file(descUnit.LogSimple.srcDir).listFiles
                else it
            }.filter { it.isFile && it.extension == extensionKt && it.name !in ignore }
        }.map { it.fsContent }

    fun fromModuleInfo(className: String, moduleInfo: ModuleUtil.Info, conf: RunScriptStr.Conf): String {
        val (_, appInitPlace, appsSetPlace, modulePlace) = moduleInfo.appsPlaces
        val ktFileName = className + kt
        val ktFile = moduleInfo.srcDirs.firstNotNullOf { modulePlace.file(it).listFiles.firstOrNull { f -> ktFileName == f.name } }
        val moduleInfoFile = modulePlace.file(BuildDescConst.src_module_info).file(BuildDescConst.ModuleInfo + kt)
        val scriptFromFile = ktFile.readText().split(funMainModuleInfo)[0].trim().plus("\n}")
        val additionalObjects = moduleInfo.dependenciesSrc.additionalObjects(conf, appsSetPlace)
        return assembleScript(
            if (conf.hasTypeAliasModule) appsSetPlace.file(allIdeTypeAlias).readText() else "",
            descUnit.BaseTypeAlias.run { appsSetPlace.file(srcDir).file(name + kt).fsContent },
            additionalObjects,
            moduleInfoFile.readText(),
            scriptFromFile,
            ktFile.nameWithoutExtension,
            updateRunEnv(appInitPlace, conf)
        ).also {
            if (conf.needSaveToFileDebug) appsSetPlace.file("tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFileName}.StrFromMInfo$kts") update it
            it.saveTo(conf.saveToPlace, modulePlace)
        }
    }

    fun fromFile(conf: RunScriptStr.Conf, vararg funMainStr: String): Pair<FsPath, String> = with(BuildDescConst) {
        val ktFile = FsPath(conf.forRuntime.scriptPath).file
        val ktFileName = ktFile.name
        val moduleInfoSrcDir = ktFile.lookupToParentByName(src_module_info) ?: ktFile.lookupToParentByName(dependencies_src_txt) ?: TODO()
        val modulePlace = moduleInfoSrcDir.up ?: TODO()
        val appsSetPlace = modulePlace.lookupToParentByName(settings_gradle_kts)?.up ?: TODO()
        val appInitDir = appsSetPlace.lookupToParentByName(app__init) ?: TODO()
        val appInitPlace = appInitDir.fsPath
        val moduleInfoFile = moduleInfoSrcDir.file(ModuleInfo + kt)
        val scriptFromFile = ktFile.readText().let { text ->
            fun containsString(): String {
                for (str in funMainStr) if (text.contains(str)) return str
                return ""
            }

            val contains = containsString()
            if (contains.isEmpty()) text else text.split(contains)[0].trim().plus("\n}")
        }
        val additionalObjects = modulePlace.file(dependencies_src_txt).readLines().additionalObjects(conf, appsSetPlace.fsPath)
        val scriptStr = assembleScript(
            if (conf.hasTypeAliasModule) appsSetPlace.file(allIdeTypeAlias).readText() else "",
            descUnit.BaseTypeAlias.run { appsSetPlace.file(srcDir).file(name + kt).fsContent },
            additionalObjects,
            moduleInfoFile.run { if (exists()) readText() else "" },
            scriptFromFile,
            ktFile.nameWithoutExtension,
            updateRunEnv(appInitPlace, conf)
        )
        if (conf.needSaveToFileDebug) appsSetPlace.file("tmp/_${ktFileName}.StrFromFile$kts") update scriptStr
        scriptStr.saveTo(conf.saveToPlace, modulePlace.fsPath)
        return appInitPlace to scriptStr
    }

    fun assembleScript(
        typeAliasText: String,
        typeAliasBase: FsContent,
        additionalObjects: List<FsContent>,
        moduleInfoObjectText: String,
        objectText: String,
        objectName: String,
        conf: RunScriptStr.Conf,
    ) = """
$typeAliasText
//${typeAliasBase.fsName}
${typeAliasBase.content}
${additionalObjects.joinToString("\n") { "// begin ${it.fsName}\n${it.content}\n//  end  ${it.fsName}" }}
$moduleInfoObjectText
${if (conf.hasUnitScriptStrRunEnv) conf.scriptStrEnvText else ""}
$objectText
//script run result string
val res = $objectName.script(${conf.scriptArgs})
res
"""
}