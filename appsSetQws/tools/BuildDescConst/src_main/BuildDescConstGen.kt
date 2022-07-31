interface BuildDescConstGen {
    companion object : BuildDescConstGen {
        override val srcDirOfBaseTypeAlias = "libs/BaseTypeAlias/src_main"
        override val srcDirOfTypeAlias = "tools/ide/TypeAlias/src_main"
    }

    val srcDirOfBaseTypeAlias: String
    val srcDirOfTypeAlias: String
}