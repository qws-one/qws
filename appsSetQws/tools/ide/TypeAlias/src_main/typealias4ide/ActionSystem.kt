@file:Suppress("UNUSED_PARAMETER")

package typealias4ide

import javax.swing.JComponent
import javax.swing.KeyStroke

class KeyboardShortcut(firstKeyStroke: KeyStroke, secondKeyStroke: KeyStroke?)

@Suppress("ClassName", "UNUSED_PARAMETER")
class actionSystem_Presentation {
    fun setText(s: String, b: Boolean) {}
}

class AnActionEvent {
    val project: Project? = null
    val place: String = TODO()

    //    fun getProject(): Project?
//fun getDataContext(): DataContext
// fun getPlace(): String
}


open class AnAction {
    val templatePresentation: actionSystem_Presentation = TODO()
}

class DumbAwareAction {

    companion object {
        //fun create(block: (AnActionEvent?) -> Unit): AnAction = TODO()
        fun create(block: (AnActionEvent) -> Unit): AnAction = TODO()
    }
}

class ActionToolbar {
    val component: JComponent = TODO()
    var targetComponent: JComponent = TODO()
}

abstract class ActionGroup {
    fun addAll(vararg actions: AnAction) {}
    fun add(action: AnAction) {}
}

class DefaultActionGroup : ActionGroup()

@Suppress("UNUSED_PARAMETER")
class ActionManager {
    fun createActionToolbar(var1: String, var2: ActionGroup, var3: Boolean): ActionToolbar = TODO()

    fun getAction(id: String): AnAction? = null
    fun unregisterAction(id: String) {}
    fun registerAction(id: String, a: AnAction) {}

    companion object {
        fun getInstance(): ActionManager = TODO()
    }
}
