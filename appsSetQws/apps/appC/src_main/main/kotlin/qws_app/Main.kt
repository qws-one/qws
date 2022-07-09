package qws_app

import LocalHostSocket
import kotlin.system.measureTimeMillis

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val engine = KtsEngineUtil.ktsEngine()

    val t = measureTimeMillis {
        engine.eval(
            """
    Main.main()
    Main.main2()
    Main.main3()
"""
        )
    }
    println("<top>.main $t")

    LocalHostSocket.uds.params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
        println("<top>.main msg='$msg' $connectionIndex, $param")
        val res = engine.eval(msg)
        result("OK $connectionIndex\n$res")
    }
}
