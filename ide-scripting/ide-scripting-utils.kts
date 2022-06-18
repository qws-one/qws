
import java.awt.BorderLayout
import javax.swing.JPanel


open class IdeApi(val ideApiObj: com.intellij.ide.script.IDE) {

    val application = ideApiObj.application

    val project get() = ideApiObj.project

    @Suppress("NOTHING_TO_INLINE")
    inline fun print(any: Any?) {
        ideApiObj.print(any)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun error(any: Any?) {
        ideApiObj.error(any)
    }
}

@Suppress("ClassName")
object ideApiHolder : IdeApiHolder()

@Suppress("PropertyName")
val IDE: com.intellij.ide.script.IDE by bindings
//val IDE: Any by bindings
ideApiHolder._ide = IDE

open class IdeApiHolder {
    @Suppress("PropertyName")
    lateinit var _ide: com.intellij.ide.script.IDE
}

object Ide : IdeApi(ideApiHolder._ide)


class ConsolePanel(val projectName: String, val descriptor: RunContentDescriptor, val descriptorDisplayName: String, val type: Type) {

    enum class Type(val executor: () -> Executor) {
        Run({ DefaultRunExecutor.getRunExecutorInstance() }),
        Dbg({ DefaultDebugExecutor.getDebugExecutorInstance() })
    }

    private val consoleView get() = descriptor.executionConsole as? ConsoleView

    private fun Any?.string() = (this?.toString() ?: "null") + "\n"

    fun print(msg: Any?) = consoleView?.print(msg.string(), ConsoleViewContentType.NORMAL_OUTPUT)

    fun error(msg: Any?) = consoleView?.print(msg.string(), ConsoleViewContentType.ERROR_OUTPUT)

    fun printHyperlink(msg: Any?, block: (Project) -> Unit) = consoleView?.printHyperlink(msg.string(), HyperlinkInfo { block(it) })

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

fun consolePanel(project: Project, displayName: String, type: ConsolePanel.Type = ConsolePanel.Type.Run): ConsolePanel {
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


Ide.project?.let { project ->
    val consolePanel = consolePanel(project, "QWS check")
    consolePanel.toFrontRunContent()

    consolePanel.print("from script")
    consolePanel.printHyperlink("Hyperlink") { consolePanel.print("from Hyperlink project=$it") }
    consolePanel.printHyperlinkDummy("HyperlinkDummy") { consolePanel.print("from HyperlinkDummy") }


    registerAction("qws_one") {
        consolePanel.toFrontRunContent().print("from action qws_one")
    }
}

