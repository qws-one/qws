import java.io.File


@Suppress("ClassName")
object runPlainIdeKtFile {
    operator fun invoke(args: Array<String>, chanelId: Int = PlainIdeListener.chanelId) {
        val relativePathToKtFile: String = args[0]
        val ktFile = File(relativePathToKtFile)
        val scriptFromFile = ktFile.readText()
            .split("fun main(args: Array<String>) = runPlainIdeKtFile(args)")[0]

        val script = """
$scriptFromFile

val res = ${ktFile.nameWithoutExtension}.script(arrayOf("${ktFile.absolutePath}"), bindings)
println("runPlainIdeKtFile[on script console]: script run result '${'$'}res'")
    """
        val res = LocalHostSocket.uds(chanelId).send(script)

        println("runPlainIdeKtFile[on app console]: run result  '$res'")
    }

}