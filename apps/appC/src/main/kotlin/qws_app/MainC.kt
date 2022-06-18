import qws_local_host.LocalHost
import qws_local_host.LocalHost.SocketConfig.Companion.send

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    println("<top>.main")
    val res = LocalHost.uds.send("2+3")

    println("<top>.runKtFile '$res'")
}