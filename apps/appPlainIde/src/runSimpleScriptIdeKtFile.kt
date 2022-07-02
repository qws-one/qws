@Suppress("ClassName")
object runSimpleScriptIdeKtFile {
    private const val lineFunMainArgs = "fun main(args: Array<String>) = runSimpleScriptIdeKtFile(args)"

    fun String.content() = java.io.File(this).readText()

    operator fun invoke(args: Array<String>, chanelId: Int = ToolSharedConfig.chanelIdSimpleScriptListener) {
        val relativePathToKtFile: String = args[0]
        val ktFile = java.io.File(relativePathToKtFile)
        val scriptFromFile = ktFile.readText().split(lineFunMainArgs)[0]

        val script = """
${"libs/libSimpleScript/src/SimpleScript.kt".content()}
$scriptFromFile

val res = ${ktFile.nameWithoutExtension}.script(arrayOf("${ktFile.absolutePath}"), bindings)
//println("runSimpleScriptIdeKtFile[on script console]: script run result '${'$'}res'")
res
    """
        //println("runSimpleScriptIdeKtFile[on app console]:  '${File("").absoluteFile}'")
        java.io.File("tmp/abc.kts").apply {
            parentFile.mkdirs()
            writeText(script)
        }
        val res = LocalHostSocket.uds(chanelId).send(script)

        println("runSimpleScriptIdeKtFile[on app console]: run result  '$res'")
    }
}