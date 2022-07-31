class FsPath(val path: String) {
    val valid by lazy { path.isNotEmpty() }

    companion object {
        val empty = FsPath("")
        fun from(path: String) = FsPath(path.trim())
    }
}