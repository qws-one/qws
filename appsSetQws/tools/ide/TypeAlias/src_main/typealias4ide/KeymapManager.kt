@file:Suppress("UNUSED_PARAMETER")

package typealias4ide


class Keymap {
    fun removeAllActionShortcuts(actionId: String) {}
    fun addShortcut(actionId: String, shortcut: KeyboardShortcut) {}
}

class KeymapManager {

    val activeKeymap: Keymap = TODO()

    companion object {
        fun getInstance(): KeymapManager = TODO()
    }
}


