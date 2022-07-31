@file:Suppress("UNUSED_PARAMETER", "unused")

package typealias4ide

import javax.swing.JComponent


open class HyperlinkInfo(block: (Project) -> Unit) {
    fun navigate(project: Project) {}
    fun includeInOccurenceNavigation(): Boolean {
        return true
    }
}

object ConsoleViewContentType {
    val NORMAL_OUTPUT: ConsoleViewContentType = TODO()
    val ERROR_OUTPUT: ConsoleViewContentType = TODO()
    val SYSTEM_OUTPUT: ConsoleViewContentType = TODO()
    val USER_INPUT: ConsoleViewContentType = TODO()
    val OUTPUT_TYPES: Array<ConsoleViewContentType> = emptyArray()
}

abstract class RunContentDescriptor(executionConsole: ExecutionConsole?, processHandler: ProcessHandler?, component: JComponent, displayName: String?) {
    val executionConsole: ExecutionConsole = TODO()

    val displayName = ""

    open fun isContentReuseProhibited() = true
}


class RunContentManager {
    fun showRunContent(executor: Executor, descriptor: RunContentDescriptor) {}

    //    fun getAllDescriptors(): List<RunContentDescriptor?>?
    val allDescriptors: List<RunContentDescriptor?> = emptyList()

    fun toFrontRunContent(var1: Executor, var2: RunContentDescriptor) {}
}

class ExecutionManager {

    @Deprecated("")
    fun getContentManager(): RunContentManager = TODO()

    companion object {
        fun getInstance(project: Project): ExecutionManager = TODO()
    }
}

class Executor
class DefaultRunExecutor {
    companion object {
        fun getRunExecutorInstance(): Executor = TODO()
    }
}

class ProcessHandler {}
open class ExecutionConsole {}
class ConsoleView : ExecutionConsole() {
    //    fun getComponent(): JComponent = TODO()
    val component: JComponent = TODO()
    fun createConsoleActions(): Array<AnAction> = TODO()

    fun print(var1: String, var2: ConsoleViewContentType) {}
    fun printHyperlink(var1: String, var2: HyperlinkInfo?) {}
}

class TextConsoleBuilder {
    //    fun getConsole(): com.intellij.execution.ui.ConsoleView
    val console: ConsoleView = TODO()
}

class TextConsoleBuilderFactory {
    fun createBuilder(project: Project): TextConsoleBuilder = TODO()

    companion object {
        fun getInstance(): TextConsoleBuilderFactory = TODO()
    }
}

class DefaultDebugExecutor {
    companion object {
        fun getDebugExecutorInstance(): Executor = TODO()
    }
}

class CloseAction(
    executor: Executor?,
    contentDescriptor: RunContentDescriptor?,
    project: Project?
) : AnAction()


class ApplicationManager {
    companion object {
        fun getApplication(): Application = TODO()
    }
}

class ProjectManager {
    val openProjects = emptyList<Project>()

    companion object {
        fun getInstance(): ProjectManager = TODO()
    }
}

class FileDocumentManager {
    fun saveAllDocuments() {}

    companion object {
        fun getInstance(): FileDocumentManager = TODO()
    }
}

@Suppress("UNUSED_PARAMETER")
class Application {
    fun runWriteAction(runnable: Runnable) {}
    fun invokeLater(runnable: Runnable) {}
    //fun invokeLater(function: () -> Unit) {}
}

class Project {
    val name = ""
    val basePath: String? = null
}

class IDE {
    val project: Project? = null
    val application: Application = TODO()

    fun print(o: Any) {}
    fun error(o: Any) {}
}
