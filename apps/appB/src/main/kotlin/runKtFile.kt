import java.io.File


fun runKtFile(args: Array<String>, chanelId: Int = 8098) {
    val relativePathToKtFile: String = args[0]
    val ktFile = File(relativePathToKtFile)
    val scriptFromFile = ktFile.readText()
        .split("fun main(args: Array<String>) = runKtFile(")[0]

    val script = """
$scriptFromFile


val res = ${ktFile.nameWithoutExtension}.main(arrayOf("${ktFile.absolutePath}"))
//println("script run result '${'$'}res'")
 qws out "script run result '${'$'}res'"
  println("bindings=${'$'}bindings")
  println("bindings=${'$'}{bindings["IDE"]}")
  println("bindings=${'$'}{bindings.keys}")
res
    """
    val res = LocalHostSocket.uds(chanelId).send(script)

    println("run result  '$res'")
}