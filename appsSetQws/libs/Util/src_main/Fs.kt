//

@Suppress("NOTHING_TO_INLINE", "unused")
object Fs {
    const val extensionKt = "kt"
    const val kt = ".$extensionKt"
    const val kts = ".kts"
    const val txt = ".txt"

    //inline fun file(baseFile: java.io.File, path: String) = file(java.io.File(baseFile, path))
    inline infix fun java.io.File.file(path: String) = file(java.io.File(this, path))
    inline fun file(basePath: String, path: String) = file(java.io.File(basePath, path))
    inline fun _file(path: String) = file(java.io.File(path))
    inline fun file(file: java.io.File): java.io.File = file.absoluteFile

    //inline fun file() = file("")
    inline fun fileTODO(): java.io.File = TODO()
    inline fun fileNull(): java.io.File? = null
    inline val java.io.File.up get() = absoluteFile.parentFile
    inline val currentDir get() = file(java.io.File(""))
    inline val java.io.File.listFiles: Array<java.io.File> get() = listFiles() ?: emptyArray()
    inline fun java.io.File.forEachDir(action: (java.io.File) -> Unit) = listFiles()?.forEach { if (it.isDirectory) action(it) }

    inline val java.io.File.deleteIfExist: Boolean
        get() {
            if (exists()) {
                if (isDirectory) deleteRecursively()
                else delete()
                println("remove: $absolutePath")
                return true
            }
            return false
        }

    inline infix fun java.io.File.update(text: String) = if (!exists()) create { text } else if (readText() != text) {
        parentFile.mkdirs()
        writeText(text)
        println("update: $absolutePath")
    } else Unit

    inline infix fun java.io.File.create(getText: () -> String) = if (!exists()) {
        val str = getText()
        if (str.isNotEmpty()) {
            parentFile.mkdirs()
            writeText(str)
            println("create: $absolutePath")
        } else Unit
    } else Unit

    fun java.io.File.lookupToParentByName(lookupName: String): java.io.File? =
        (if (isDirectory) this else up)?.run {
            listFiles.firstOrNull { it.name == lookupName }
                ?: up?.lookupToParentByName(lookupName)
        }

    inline val String._file get() = _file(this)

    fun createTempFile(prefix: String, suffix: String) = java.io.File.createTempFile(prefix, suffix)
}