interface BuildDescGen {
    @Suppress("PropertyName")
    class DescUnit {
        val BaseTypeAlias = Companion.BaseTypeAlias
        val TypeAlias = Companion.TypeAlias
    }

    companion object : BuildDescGen {
        object BaseTypeAlias {
            const val name = "BaseTypeAlias"
            const val srcDir = "libs/BaseTypeAlias/src_main"
        }

        object TypeAlias {
            const val name = "TypeAlias"
            const val srcDir = "tools/ide/TypeAlias/src_main"
            const val ignoreSrcFileName = "TypeAliasTransient.kt"
        }

        override val descUnit = DescUnit()
    }

    val descUnit: DescUnit
}