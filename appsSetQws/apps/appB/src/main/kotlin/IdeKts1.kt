import kotlin.system.measureTimeMillis

@Suppress("unused")
object IdeKts1 {
    fun main(args: Array<String>) = qws.simpleScript(args) {
        Ide.print("IdeKts1.main ${parameters.scriptFile}")
        qws.out("IdeKts1.main ${parameters.scriptFile}")
        val t = measureTimeMillis {
            qws out "check output message"
            qws out "-=-=-=-=-=-==-=-=-=-=-2"
            qws err "check error message"
            qws out "project name = ${qws.prj.name}"
        }
        qws out ("t=$t")
        result put "check result"
    }
}

fun main(args: Array<String>) = runKtFile(args)