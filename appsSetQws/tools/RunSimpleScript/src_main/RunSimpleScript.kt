//
object RunSimpleScript {
    private const val typeAliasKt = "TypeAlias.kt"
    private val runSimpleScriptKt = this::class.simpleName?.plus(Fs.kt) ?: TODO()

    private const val lineFunMainArgs = "fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)"

    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.filter { it.exists() }.getOrElse(0) { Fs.fileTODO() }
    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.firstOrNull { it.exists() } ?: Fs.fileTODO()

    operator fun invoke(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) {
        //old(args, thisRef, moduleInfo, chanelId)

        fromModuleInfo(args, thisRef, moduleInfo)
        fromFile(args)
    }

    fun fromModuleInfo(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info) = with(Fs) {
        val appsSetPlace = currentDir file moduleInfo.appsSetName
        val modulePlace = appsSetPlace file moduleInfo.relativePath

        val ktFileName = thisRef::class.simpleName?.plus(kt) ?: TODO()
        val ktFile = args.map { _file(it) }.firstOrNull { it.exists() && ktFileName == it.name }
            ?: moduleInfo.srcDirs.map { modulePlace.file(it).listFiles.firstOrNull { f -> ktFileName == f.name } }.first() ?: TODO()
        val moduleInfoFile = modulePlace.file(BuildDescConst.src_module_info).file(BuildDescConst.ModuleInfo + kt)

        val scriptFromFile = ktFile.readText().replace(lineFunMainArgs, "").replace("@JvmStatic", "")
        val additionalObject = moduleInfo.dependenciesSrc.map { dependencySrcRelativePath ->
            appsSetPlace.file(dependencySrcRelativePath).listFiles
                .filter {
                    it.isFile && it.extension == extensionKt
                            && it.name != typeAliasKt
                            && it.name != runSimpleScriptKt
                }
                .joinToString("\n") { it.readText() }
        }

        val scriptStr = assembleScript(
            _file("${moduleInfo.appsSetName}/tmp/__all_ide_TypeAlias.kt").readText(),
            additionalObject,
            moduleInfoFile.readText(),
            scriptFromFile,
            ktFile.nameWithoutExtension,
            args.toList()
        )
        appsSetPlace.file("/tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFileName}.StrFromMInfo$kts") update scriptStr
    }

    fun fromFile(args: Array<String>) = with(Fs) {
        val ktFile = args.map { _file(it) }.firstOrNull { it.exists() } ?: TODO()
        val ktFileName = ktFile.name
        val moduleInfoSrcDir = ktFile.lookupToParentByName(BuildDescConst.src_module_info) ?: TODO()
        val modulePlace = moduleInfoSrcDir.up
        val settingsGradleKts = modulePlace.lookupToParentByName(BuildDescConst.settings_gradle_kts) ?: TODO()
        val appsSetPlace = settingsGradleKts.up
        val moduleInfoFile = moduleInfoSrcDir.file(BuildDescConst.ModuleInfo + kt)
        val scriptFromFile = ktFile.readText().replace(lineFunMainArgs, "").replace("@JvmStatic", "")

        val additionalObject = modulePlace.file(BuildDescConst.dependencies_src_txt).readLines().map { dependencySrcRelativePath ->
            appsSetPlace.file(dependencySrcRelativePath.trim()).listFiles
                .filter {
                    it.isFile && it.extension == extensionKt
                            && it.name != typeAliasKt
                            && it.name != runSimpleScriptKt
                }
                .joinToString("\n") { it.readText() }
        }


        val scriptStr = assembleScript(
            appsSetPlace.file("tmp/__all_ide_TypeAlias.kt").readText(),
            additionalObject,
            moduleInfoFile.readText(),
            scriptFromFile,
            ktFile.nameWithoutExtension,
            args.toList()
        )
        appsSetPlace.file("/tmp/_${ktFileName}.StrFromFile$kts") update scriptStr
    }

    fun old(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) = with(Fs) {
        //        val name = thisRef::class.simpleName ?: TODO()
        val appsSetPlace = currentDir file moduleInfo.appsSetName
        val modulePlace = appsSetPlace file moduleInfo.relativePath

        fun ModuleUtil.Info.ktFileByName(name: String) =
            srcDirs.map { with(Fs) { modulePlace.file(it).listFiles.firstOrNull { it.nameWithoutExtension == name } } }
                .first { it?.nameWithoutExtension == name }
        //val ktFile = if (args.isNotEmpty()) Fs.file(args[0]) else moduleInfo.ktFile
        val ktFile = args.map { _file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName(thisRef::class.simpleName ?: TODO()) ?: TODO()
        val moduleInfoFile = args.map { _file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName("ModuleInfo")

        val scriptFromFile = ktFile.readText().replace(lineFunMainArgs, "").replace("@JvmStatic", "")
        val additionalObject = with(Fs) {
            moduleInfo.dependenciesSrc.map { dependencySrcPathRelativeToProjectRoot ->
                dependencySrcPathRelativeToProjectRoot._file.listFiles.filter { it.isFile && it.extension == "kt" && it.name != "TypeAlias.kt" }
                    .joinToString("\n") { it.readText() }
            }
        }//
        val script = """
${additionalObject.joinToString("\n")}
${moduleInfoFile?.readText() ?: ""}
$scriptFromFile

val res = ${ktFile.nameWithoutExtension}.script(arrayOf("${ktFile.absolutePath}"), bindings)
//println("runSimpleScriptIdeKtFile[on script console]: script run result '${'$'}res'")
res
    """
        //println("RunSimpleScript[on app console]:  '${File("").absoluteFile}'")
//        with(Fs) { "${moduleInfo.appsSetName}/tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFile.nameWithoutExtension}.kts".update(script) }

//        val scriptStr = assembleScript(
//            Fs.file("${moduleInfo.appsSetName}/tmp/__all_ide_imports.txt").readText(),
//            Fs.file("${moduleInfo.appsSetName}/tmp/__all_ide_TypeAlias.kt").readText(),
//            additionalObject,
//            moduleInfoFile?.readText() ?: "",
//            scriptFromFile,
//            ktFile.nameWithoutExtension,
//            args.toList()
//        )
//        with(Fs) { "${moduleInfo.appsSetName}/tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFile.nameWithoutExtension}Str.kts".update(scriptStr) }

        val res = LocalHostSocket.uds(chanelId).send(script)

        println("RunSimpleScript[on app console]: run result  '$res'")
    }

    fun assembleScript(
        typeAliasText: String,
        additionalObject: List<String>,
        moduleInfoObjectText: String,
        objectText: String,
        objectName: String,
        args: List<String>
    ) = """
$typeAliasText
${additionalObject.joinToString("\n")}
$moduleInfoObjectText
$objectText

//script run result string
val res = $objectName.script(arrayOf(${args.joinToString { "\"$it\"" }}), bindings)
res
"""
}