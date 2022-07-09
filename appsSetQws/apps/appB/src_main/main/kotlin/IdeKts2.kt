import kotlin.system.measureTimeMillis

@Suppress("unused")
object IdeKts2 {
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

        Ide.project?.let { project ->
            Ide.application.invokeLaterOnWriteThread {
                val consolePanel = Ide.consolePanel(project.name, "QWS check")
                consolePanel.toFrontRunContent()

                consolePanel.print("from script")
                consolePanel.printHyperlinkDummy("HyperlinkDummy") {
                    Ide.application.invokeLaterOnWriteThread {
                        consolePanel.print("from HyperlinkDummy " + Ide.scriptEngine()?.eval("2+5"))
                    }
                }


                Ide.registerAction("qws_one") {
                    consolePanel.toFrontRunContent().print("from action qws_one " + Ide.scriptEngine()?.eval("1+2+5"))
                }
            }
        }
        result put "check result " + Ide.scriptEngine()?.eval("1+2")
    }
}

fun main(args: Array<String>) = runKtFile(args)