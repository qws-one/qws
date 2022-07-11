//
object RunSimpleScript {
    private const val lineFunMainArgs = "fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)"

    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.filter { it.exists() }.getOrElse(0) { Fs.fileTODO() }
    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.firstOrNull { it.exists() } ?: Fs.fileTODO()

    operator fun invoke(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) {
        old(args, thisRef, moduleInfo, chanelId)
    }

    fun old(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) {
        //        val name = thisRef::class.simpleName ?: TODO()
        fun ModuleUtil.Info.ktFileByName(name: String) =
            srcDirs.map { with(Fs) { file(relativePlace, it).listFiles.firstOrNull { it.nameWithoutExtension == name } } }
                .first { it?.nameWithoutExtension == name }
        //val ktFile = if (args.isNotEmpty()) Fs.file(args[0]) else moduleInfo.ktFile
        val ktFile = args.map { Fs.file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName(thisRef::class.simpleName ?: TODO()) ?: TODO()
        val moduleInfoFile = args.map { Fs.file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName("ModuleInfo")

        val scriptFromFile = ktFile.readText().replace(lineFunMainArgs, "").replace("@JvmStatic", "")
        val additionalObject = with(Fs) {
            moduleInfo.dependenciesSrc.map { dependencySrcPathRelativeToProjectRoot ->
                dependencySrcPathRelativeToProjectRoot.listFiles.filter { it.isFile && it.extension == "kt" && it.name != "TypeAlias.kt" }
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
        //with(Fs) { "${moduleInfo.appsSetName}/tmp/${moduleInfo.relativePath.replace('/', '_')}_${ktFile.nameWithoutExtension}.kts".update(script) }

//        val scriptStr = assembleScript(
//            Fs.file("${moduleInfo.appsSetName}/tmp/imports.txt").readText(),
//            Fs.file("${moduleInfo.appsSetName}/tmp/TypeAlias.kt").readText(),
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
        importsText: String,
        typeAliasText: String,
        additionalObject: List<String>,
        moduleInfoObjectText: String,
        objectText: String,
        objectName: String,
        args: List<String>
    ) = """
$importsText
$typeAliasText
${additionalObject.joinToString("\n")}
$moduleInfoObjectText
$objectText

//script run result string
val res = $objectName.script(arrayOf(${args.joinToString { "\"$it\"" }}), bindings)
res
"""
}