package typealias4ide


class FileEditor

class FileEditorManagerEx : FileEditorManager() {
    //    abstract fun getCurrentFile(): com.intellij.openapi.vfs.VirtualFile?
    val currentFile: VirtualFile? = null

    companion object {
        fun getInstance(project: Project): FileEditorManager = TODO()
    }
}

open class FileEditorManager {
    fun openFile(file: VirtualFile, var2: Boolean): Array<FileEditor?> = emptyArray()
    fun openFile(file: VirtualFile, focusEditor: Boolean, searchForOpen: Boolean): Array<FileEditor?> = emptyArray()

    companion object {
        fun getInstance(project: Project): FileEditorManager = TODO()
    }
}
