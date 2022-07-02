//

@Suppress("NOTHING_TO_INLINE", "unused")
object Fs {

    inline fun file(baseFile: java.io.File, path: String) = file(java.io.File(baseFile, path))
    inline fun file(basePath: String, path: String) = file(java.io.File(basePath, path))
    inline fun file(path: String) = file(java.io.File(path))
    inline fun file(file: java.io.File): java.io.File = file.absoluteFile
    inline fun file() = file("")
    inline fun fileTODO(): java.io.File = TODO()
    inline fun fileNull(): java.io.File? = null
    inline val java.io.File.listFiles: Array<java.io.File> get() = listFiles() ?: emptyArray()

    inline fun java.io.File.update(text: String) = if (!exists() || readText() != text) {
        parentFile.mkdirs()
        writeText(text)
        println("update: $absolutePath")
    } else Unit

    inline fun java.io.File.create(getText: () -> String) = if (!exists()) {
        parentFile.mkdirs()
        writeText(getText())
        println("create: $absolutePath")
    } else Unit

    inline val String.listFiles get() = file(this).listFiles
    inline fun String.update(text: String) = file(this).update(text)
    inline fun String.create(getText: () -> String) = file(this).create(getText)
    inline fun String.content() = file(this).readText()
}