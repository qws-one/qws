@file:Suppress("UNUSED_PARAMETER", "unused")

package typealias4ide

import java.io.File
import java.nio.file.Path

class VirtualFile {
    val name = ""
    val path = ""
    val nameWithoutExtension = ""
    val url = ""
    fun toNioPath(): Path = TODO()

//    fun getName(): String
//    fun getNameSequence(): CharSequence
//    fun getUrl(): String
//    fun getFileSystem(): VirtualFileSystem
//    fun getPath(): String
//    fun getPresentableUrl(): String
//    fun getExtension():String?
//    fun getNameWithoutExtension():  String
}

class LocalFileSystem {

    fun findFileByIoFile(file: File): VirtualFile? = null

    companion object {
        fun getInstance(): LocalFileSystem = TODO()
    }
}
