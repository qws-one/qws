import qws_app.KtsEngine
import qws_app.KtsEngineForRun
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

object MainL2 {
    val udsSuffix = 8082

    @JvmStatic
    fun main(args: Array<String>) {
        println("L2")

        val engine = ScriptEngineManager().getEngineByExtension("kts")!!

        val t1 = measureTimeMillis {
            engine.eval("1+1")
        }

        val t2 = measureTimeMillis {
            KtsEngine.eval("1+1")
        }

        val t3 = measureTimeMillis {
            KtsEngineForRun.eval("1+1")
        }

        println("L2 $t1 $t2 $t3 ${t1 + t2 + t3}")

        val socketConfig = LocalHostSocket.uds(udsSuffix).params(
            byteBufferInputSize = 1024,
            acceptClientConnectionCount = 3,
        )

        LocalHostSocket.listen(socketConfig) {
            println("L2 msg.length='${msg.length}' index=$connectionIndex, $param")
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


