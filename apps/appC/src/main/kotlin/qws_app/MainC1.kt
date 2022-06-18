
import java.io.File

fun main() {
    println("<top>.main")
    fun runKtFile(ktFile: File) {
        val name = ktFile.nameWithoutExtension
        val script = """
${ktFile.readText()}

val res = ${name}.go()
println("script run result '${'$'}res'")
    """
        val res = LocalHost.uds(MainL1.udsSuffix).send(script)

        println("<top>.runKtFile '$res'")
    }

    runKtFile(File("apps/appC/src/main/kotlin/MainKts1.kt"))

}
