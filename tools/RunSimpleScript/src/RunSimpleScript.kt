//
object RunSimpleScript {
    private const val lineFunMainArgs = "fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)"

    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.filter { it.exists() }.getOrElse(0) { Fs.fileTODO() }
    //private val ModuleUtil.Info.ktFile get() = srcDirs.map { Fs.file(relativePath, it) }.firstOrNull { it.exists() } ?: Fs.fileTODO()

    operator fun invoke(args: Array<String>, thisRef: Any, moduleInfo: ModuleUtil.Info, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) {
        //        val name = thisRef::class.simpleName ?: TODO()
        fun ModuleUtil.Info.ktFileByName(name: String) =
            srcDirs.map { with(Fs) { file(relativePath, it).listFiles.firstOrNull { it.nameWithoutExtension == name } } }
                .first { it?.nameWithoutExtension == name }
        //val ktFile = if (args.isNotEmpty()) Fs.file(args[0]) else moduleInfo.ktFile
        val ktFile = args.map { Fs.file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName(thisRef::class.simpleName ?: TODO()) ?: TODO()
        val moduleInfoFile = args.map { Fs.file(it) }.firstOrNull { it.exists() } ?: moduleInfo.ktFileByName("ModuleInfo")

        val scriptFromFile = ktFile.readText().replace(lineFunMainArgs, "").replace("@JvmStatic", "")

        val script = """
${
            with(Fs) {
                moduleInfo.dependenciesSrc.map {
                    it.listFiles.filter { it.isFile && it.extension == "kt" && it.name != "TypeAlias.kt" }
                        .map { it.readText() }.joinToString("\n")
                }
            }.joinToString("\n")
        }
${moduleInfoFile?.readText() ?: ""}
$scriptFromFile

val res = ${ktFile.nameWithoutExtension}.script(arrayOf("${ktFile.absolutePath}"), bindings)
//println("runSimpleScriptIdeKtFile[on script console]: script run result '${'$'}res'")
res
    """
        //println("RunSimpleScript[on app console]:  '${File("").absoluteFile}'")
        with(Fs) { "${moduleInfo.relativePath}/tmp/${ktFile.nameWithoutExtension}.kts".update(script) }
        val res = LocalHostSocket.uds(chanelId).send(script)

        println("RunSimpleScript[on app console]: run result  '$res'")
    }
}