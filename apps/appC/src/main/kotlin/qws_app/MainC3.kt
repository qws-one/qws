
import java.io.File

fun main() {
    println("<top>.main")
    fun runKtFile(ktFile: File) {
        val name = ktFile.nameWithoutExtension
        val script = """
${ktFile.readText()}

${name}.go()
    """
        val res = LocalHostSocket.uds(MainL2.udsSuffix).send(script)

        println("<top>.runKtFile '$res'")
    }
    runKtFile(File("apps/appC/src/main/kotlin/MainKts3.kt"))
}
