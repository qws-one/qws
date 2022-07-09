package qws_app

import javax.script.ScriptEngineManager
import javax.script.ScriptException

object KtsEngineForRun : KtsEngineUtil()

object KtsEngine : KtsEngineUtil()

open class KtsEngineUtil {

    private val engine by lazy { ScriptEngineManager().getEngineByExtension("kts") ?: TODO() }

    @Throws(ScriptException::class)
    fun eval(str: String): Any? = engine.eval(str)

    companion object {
        fun ktsEngine() = object : KtsEngineUtil() {}
    }
}