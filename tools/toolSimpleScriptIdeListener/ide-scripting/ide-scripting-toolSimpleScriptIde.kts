
typealias IdeScriptEngineMgr = IdeScriptEngineManager
typealias ApplicationMgr = ApplicationManager
typealias ProjectMgr = ProjectManager
typealias IDE_typealias = IDE

@Suppress("MemberVisibilityCanBePrivate")
object PlainReadScriptAndStartListen {
    fun newScriptEngine(): javax.script.ScriptEngine? {
        for (engineInfo in IdeScriptEngineMgr.getInstance().engineInfos)
            if (engineInfo.fileExtensions.contains("kts")) {
                val ideScriptEngine = IdeScriptEngineMgr.getInstance().getEngine(engineInfo, null as ClassLoader?) ?: return null
                return object : javax.script.ScriptEngine {
                    override fun get(key: String): Any? = ideScriptEngine.getBinding(key)
                    override fun put(key: String, value: Any?) = ideScriptEngine.setBinding(key, value)
                    override fun eval(any: String): Any? = ideScriptEngine.eval(any)

                    override fun eval(p0: String?, p1: javax.script.ScriptContext?): Any = Unit
                    override fun eval(p0: java.io.Reader?, p1: javax.script.ScriptContext?): Any = Unit
                    override fun eval(p0: java.io.Reader?): Any = Unit
                    override fun eval(p0: String?, p1: javax.script.Bindings?): Any = Unit
                    override fun eval(p0: java.io.Reader?, p1: javax.script.Bindings?): Any = Unit
                    override fun getBindings(p0: Int): javax.script.Bindings? = null
                    override fun setBindings(p0: javax.script.Bindings?, p1: Int) {}
                    override fun createBindings(): javax.script.Bindings? = null
                    override fun getContext(): javax.script.ScriptContext? = null
                    override fun setContext(p0: javax.script.ScriptContext?) {}
                    override fun getFactory(): javax.script.ScriptEngineFactory? = null
                }
            }
        return null
    }

    fun start(projectBasePath: String, bindings: Map<String, Any?>) {
        fun String.content() = java.io.File("$projectBasePath/$this").readText()

        val additionalScriptEngine = newScriptEngine() ?: return
        val entryPointScriptEngine = newScriptEngine() ?: return

        val ide = "IDE".let {
            additionalScriptEngine.put(it, bindings[it]);
            entryPointScriptEngine.put(it, bindings[it]);
            bindings[it] as IDE_typealias
        }

        val ideOutPrintln: (o: Any) -> Unit = ide::print
        val ideErrPrintln: (o: Any) -> Unit = ide::error

        additionalScriptEngine.put("newScriptEngine", this::newScriptEngine)

        additionalScriptEngine.put("ideOutPrintln", ideOutPrintln)
        additionalScriptEngine.put("ideErrPrintln", ideErrPrintln)

        entryPointScriptEngine.put("ideOutPrintln", ideOutPrintln)
        entryPointScriptEngine.put("ideErrPrintln", ideErrPrintln)
        try {

            entryPointScriptEngine.put("additionalScriptEngine", additionalScriptEngine)
            val entryPointScript = """
${"libs/libLocalHostSocket/src/LocalHostSocket.kt".content()}
${"tools/tool4config/src/ToolSharedConfig.kt".content()}
${"tools/toolSimpleScriptIdeListener/src/SimpleScriptListener.kt".content()}

SimpleScriptListener.start(bindings)
            """
//            java.io.File("$projectBasePath/tools/toolSimpleScriptIdeListener/tmp/entryPointSimpleScriptIdeScript.kts").apply {
//                parentFile.mkdirs()
//                writeText(entryPointScript)
//            }
            val res = entryPointScriptEngine.eval(entryPointScript)
            ide.print(res)
        } catch (e: Exception) {
            e.printStackTrace()
            ide.error(e)
        }
    }
}

ProjectMgr.getInstance().openProjects.forEach { project ->
    if ("qws" == project.name) project.basePath?.let { basePath ->
        ApplicationMgr.getApplication().invokeLater { PlainReadScriptAndStartListen.start(basePath, bindings) }
    }
}
