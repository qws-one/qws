object Action {
    fun script(args: Array<String>, bindings: Map<String, Any?>) = SimpleScript(args) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        ProjectMgr.getInstance().openProjects.forEach { project ->
            if ("qws" == project.name) Lib.outputPanel(project.name, "QWS check")?.let { consolePanel ->
                consolePanel.toFrontRunContent()

                consolePanel.out println ("from script")
                consolePanel.printHyperlink("Hyperlink") { consolePanel.out println ("from Hyperlink project=$it") }
                consolePanel.printHyperlinkDummy("HyperlinkDummy") { consolePanel.out println ("from HyperlinkDummy") }

                Lib.action.register("qws_one") {
                    consolePanel.toFrontRunContent().out println ("from action qws_one " + Lib.newScriptEngine()?.eval("1+12"))
                }
            }
        }

    }

    @JvmStatic
    fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)
}
