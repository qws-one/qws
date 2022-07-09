//
@Suppress("unused")
object PlainIdeMain {

    fun script(args: Array<String>, bindings: Map<String, Any?>) {
        val ideOutPrintln: (o: Any) -> Unit by bindings
        val ideErrPrintln: (o: Any) -> Unit by bindings
        val newScriptEngine: () -> javax.script.ScriptEngine? by bindings

        val scriptEngine = newScriptEngine()

        val res = scriptEngine?.eval("1+3")
        ideOutPrintln("PlainIdeMain.script $newScriptEngine")
        ideOutPrintln("PlainIdeMain.script $res")
        ideOutPrintln("PlainIdeMain.script ${args.size}")
        ideOutPrintln("PlainIdeMain.script ${args[0]}")
//        ideOutPrintln("PlainIdeMain.script ${args[1]}")
    }
}

fun main(args: Array<String>) = runPlainIdeKtFile(args)