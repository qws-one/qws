import qws_local.ApplicationInstance
import qws_local.ProjectInstance
import javax.script.ScriptEngineManager
import kotlin.system.measureTimeMillis

object MainKts2 {
    @Suppress("unused")
    fun go(): String {
        val t = measureTimeMillis {
            val scriptEngine = ScriptEngineManager().getEngineByExtension("kts") ?: TODO()
            qws out "internal script run result ${scriptEngine.eval("11+10")}"
            qws out "check output message"
            qws err "check error message"
            qws out "project name = ${qws.prj.name}"
        }
        println("t=${t}")
        return this::class.simpleName ?: "MainKts"
    }
}
