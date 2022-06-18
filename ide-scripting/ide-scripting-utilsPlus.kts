
import java.awt.BorderLayout
import javax.swing.JPanel

typealias IdeProject = ide.Project

open class IdeApi(private val ideApiObj: Any) {

    @Suppress("MemberVisibilityCanBePrivate")
    class Application(val ideAppObj: Any) {
        private val methodInvokeLater by lazy { ideAppObj.javaClass.getDeclaredMethod("invokeLater", Runnable::class.java) }

        fun invokeLater(runnable: Runnable) {
            methodInvokeLater.invoke(ideAppObj, runnable)
        }
    }

    class Project(val idePrjObj: Any) {
        private val methodGetName by lazy { idePrjObj.javaClass.getDeclaredMethod("getName") }
        private val methodGetBasePath by lazy { idePrjObj.javaClass.getDeclaredMethod("getBasePath") }

        val name get() = methodGetName.invoke(idePrjObj) as String
        val basePath get() = methodGetBasePath.invoke(idePrjObj) as String?
    }

    private val methodPrint by lazy { ideApiObj.javaClass.getDeclaredMethod("print", Any::class.java) }
    private val methodError by lazy { ideApiObj.javaClass.getDeclaredMethod("error", Any::class.java) }

    private val fieldApplication by lazy { ideApiObj.javaClass.getDeclaredField("application") }
    private val fieldProject by lazy { ideApiObj.javaClass.getDeclaredField("project") }

    private var _application: Application? = null


    val application: Application
        get() {
            if (null == _application) {
                val app = fieldApplication.get(ideApiObj)
                if (null != app) {
                    _application = Application(app)
                }
            }
            return _application ?: TODO()
        }

    @Suppress("LocalVariableName")
    val project: Project?
        get() {
            var _project: Project? = null
            val _p = fieldProject.get(ideApiObj)
            if (null != _p) {
                _project = Project(_p)
            }
            return _project
        }

    fun print(any: Any?) {
        try {
            methodPrint.invoke(ideApiObj, any)
        } catch (e: Exception) {
            println(any)
        }
    }

    fun error(any: Any?) {
        try {
            methodError.invoke(ideApiObj, any)
        } catch (e: Exception) {
            System.err.println(any)
        }
    }

    class ScriptEngine(val engine: IdeScriptEngine?) {

        fun eval(str: String): Any? {
            return engine?.eval(str)
        }
    }

    fun scriptEngine(): ScriptEngine? {
        for (engineInfo in IdeScriptEngineManager.getInstance().engineInfos) {
            if (engineInfo.fileExtensions.contains("kts")) {
                return ScriptEngine(IdeScriptEngineManager.getInstance().getEngine(engineInfo, null as ClassLoader?))
            }
        }
        return null
    }

    class ConsolePanel(val projectName: String, val descriptor: RunContentDescriptor, val descriptorDisplayName: String, val type: Type) {

        enum class Type(val executor: () -> Executor) {
            Run({ DefaultRunExecutor.getRunExecutorInstance() }),
            Dbg({ DefaultDebugExecutor.getDebugExecutorInstance() })
        }

        private val consoleView get() = descriptor.executionConsole as? ConsoleView

        private fun Any?.string() = (this?.toString() ?: "null") + "\n"

        fun print(msg: Any?) = consoleView?.print(msg.string(), ConsoleViewContentType.NORMAL_OUTPUT)

        fun error(msg: Any?) = consoleView?.print(msg.string(), ConsoleViewContentType.ERROR_OUTPUT)

        fun printHyperlink(msg: Any?, block: (IdeProject) -> Unit) =
            consoleView?.printHyperlink(msg.string(), HyperlinkInfo { block(it) })

        fun printHyperlinkDummy(msg: Any?, block: () -> Unit) = consoleView?.printHyperlink(msg.string(), HyperlinkInfo { block() })

        fun toFrontRunContent(): ConsolePanel {
            ProjectManager.getInstance().openProjects.forEach {
                if (projectName == it.name) {
                    ApplicationManager.getApplication().invokeLater {
                        val executor = type.executor()
                        //RunContentManager.getInstance(it).toFrontRunContent(executor, descriptor))
                        ExecutionManager.getInstance(it).getContentManager().toFrontRunContent(executor, descriptor)
                    }
                }
            }
            return this
        }
    }

    fun fromOpenProjects(projectName: String): Project? {
        ProjectManager.getInstance().openProjects.forEach {
            if (projectName == it.name) return Project(it)
        }
        return null
    }

    fun consolePanel(projectName: String, displayName: String, type: ConsolePanel.Type = ConsolePanel.Type.Run): ConsolePanel {
        val project = fromOpenProjects(projectName)?.idePrjObj as IdeProject
        val descriptorDisplayName = type.name + displayName.replaceFirstChar { it.uppercaseChar() }
        for (runContentDescriptor in ExecutionManager.getInstance(project).getContentManager().allDescriptors) {
            if (null != runContentDescriptor && runContentDescriptor.displayName == descriptorDisplayName) {
                return ConsolePanel(project.name, runContentDescriptor, descriptorDisplayName, type)
            }
        }
        return DefaultActionGroup().let { group ->
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console

            val panel = JPanel(BorderLayout())
            panel.add(consoleView.component, "Center")
            val toolbar = ActionManager.getInstance().createActionToolbar("RunConsole", group, false)
            toolbar.targetComponent = consoleView.component
            panel.add(toolbar.component, "West")
            val descriptor = object : RunContentDescriptor(consoleView, null as ProcessHandler?, panel, descriptorDisplayName) {
                override fun isContentReuseProhibited() = true
            }
            val executor = type.executor()
            group.addAll(*consoleView.createConsoleActions())
            group.add(CloseAction(executor, descriptor, project))
            ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, descriptor)
            ConsolePanel(project.name, descriptor, descriptorDisplayName, type)
        }
    }

    fun registerAction(id: String, block: () -> Unit) = registerAction(id, DumbAwareAction.create { actionEvent ->
        actionEvent?.let { block() }
    })

    fun registerAction(id: String, action: AnAction) = ActionManager.getInstance().apply {
        if (getAction(id) != null) {
            unregisterAction(id)
        }
        registerAction(id, action)
        action.templatePresentation.setText("QWS DEBUG $id", true)
    }

}

object IdeApiHolder {
    const val keyNameIDE = "IDE"

    @Suppress("ObjectPropertyName")
    lateinit var _ide: Any
}
IdeApiHolder._ide = bindings[IdeApiHolder.keyNameIDE] as Any

object Ide : IdeApi(IdeApiHolder._ide)

Ide.project?.let { project ->
    val consolePanel = Ide.consolePanel(project.name, "QWS check")
    consolePanel.toFrontRunContent()

    consolePanel.print("from script")
    consolePanel.printHyperlink("Hyperlink") { consolePanel.print("from Hyperlink project=$it") }
    consolePanel.printHyperlinkDummy("HyperlinkDummy") { consolePanel.print("from HyperlinkDummy") }


    Ide.registerAction("qws_one") {
        consolePanel.toFrontRunContent().print("from action qws_one " + Ide.scriptEngine()?.eval("1+2"))
    }
}
