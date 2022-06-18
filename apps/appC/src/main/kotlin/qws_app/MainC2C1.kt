import qws_local_host.LocalHost
import qws_local_host.LocalHost.SocketConfig.Companion.send
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
        val res = LocalHost.uds(MainL2.udsSuffix).send(script)

        println("<top>.runKtFile '$res'")
    }
    runKtFile(File("apps/appC/src/main/kotlin/MainKtsC2C1.kt"))
}
