//

@Suppress("NOTHING_TO_INLINE", "unused")
class LocalFs {
    interface Is {
        interface Fs {
            val extensionKt: String
            val kt: String
            val kts: String
        }

        val fs: Fs
        val String.fsPath: FsPath

        val java.io.File.fsPath: FsPath get() = FsPath(absolutePath)
        val FsPath.file get() = java.io.File(path)
        fun FsPath.file(path: String) = file(java.io.File(file, path))
        infix fun FsPath.fsPath(path: String): FsPath

        val java.io.File.fsItem: FsItem get() = FsItem(absolutePath, name)
        val FsItem.file get() = java.io.File(path)
        fun FsItem.file(path: String) = file(java.io.File(file, path))
        val FsItem.fsContent get() = file.let { FsContent(it.path, it.name, it.readText()) }
        val FsItem.fileContent get() = file.fsContent
        val java.io.File.fsContent get() = FsContent(path, name, readText())

        val java.io.File.fromFile get() = if (exists()) FsPath.from(readText()) else FsPath.empty
        val FsPath.fromFile get() = file.fromFile

        infix fun java.io.File.file(path: String): java.io.File
        infix fun java.io.File.update(text: String)
        infix fun java.io.File.lookupToParentByName(lookupName: String): java.io.File?
        val java.io.File.listFiles: Array<java.io.File>
        val java.io.File.up: java.io.File?
        val currentDir: java.io.File
    }

    @Suppress("OVERRIDE_BY_INLINE")
    companion object : Is {
        const val extensionKt = "kt"
        const val kt = ".$extensionKt"
        const val kts = ".kts"
        const val txt = ".txt"

        override val fs = object : Is.Fs {
            override val extensionKt = LocalFs.extensionKt
            override val kt = LocalFs.kt
            override val kts = LocalFs.kts
        }

        override inline infix fun FsPath.fsPath(path: String) = file(path).fsPath

        //inline fun file(baseFile: java.io.File, path: String) = file(java.io.File(baseFile, path))
        override inline infix fun java.io.File.file(path: String) = file(java.io.File(this, path))
        inline fun file(basePath: String, path: String) = file(java.io.File(basePath, path))
        inline fun file(file: java.io.File): java.io.File = file.absoluteFile

        inline fun fileTODO(): java.io.File = TODO()
        inline fun fileNull(): java.io.File? = null

        override inline val java.io.File.up get() = absoluteFile.parentFile
        override inline val currentDir get() = file(java.io.File(""))
        override inline val java.io.File.listFiles: Array<java.io.File> get() = listFiles() ?: emptyArray()
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

        override inline infix fun java.io.File.update(text: String) = if (!exists()) create { text } else if (readText() != text) {
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

        override fun java.io.File.lookupToParentByName(lookupName: String): java.io.File? =
            (if (isDirectory) this else up)?.run {
                listFiles.firstOrNull { it.name == lookupName }
                    ?: up?.lookupToParentByName(lookupName)
            }

        override inline val String.fsPath get() = FsPath.from(this)

        fun createTempFile(prefix: String, suffix: String) = java.io.File.createTempFile(prefix, suffix)
    }
}