import qws_app.KtsEngineUtil
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

object MainL1 {
    const val udsSuffix = 8081

    @JvmStatic
    fun main(args: Array<String>) {
        println("L1")

        val engine = KtsEngineUtil.ktsEngine()

        val t1 = measureTimeMillis {
            engine.eval("1+1")
        }

        println("L1 $t1")
        LocalHost.uds(udsSuffix).params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
            println("L1 msg.length='${msg.length}' index=$connectionIndex, $param")
            val res = try {
                engine.eval(msg)
            } catch (e: ScriptException) {
                e.printStackTrace()
                e.toString()
            }
            result("connectionIndex=$connectionIndex\nresult:\n$res")
        }
    }
}
