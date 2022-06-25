

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    println("<top>.main")
//    println(12)
//    val res = LocalHost.uds.send("2+3")
    val res = LocalHostSocket.uds(8091).send("println(1)\n//12+3")
    println("<top>.runKtFile '$res'")
}