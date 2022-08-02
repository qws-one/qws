interface BuildDescGen {
    @Suppress("PropertyName")
    class DescUnit {
        val LogSimple = Companion.LogSimple
        val LocalHostSocket = Companion.LocalHostSocket
        val BaseTypeAlias = Companion.BaseTypeAlias
        val ScriptStrRunEnv = Companion.ScriptStrRunEnv
        val RunScriptStr = Companion.RunScriptStr
        val TypeAlias = Companion.TypeAlias
    }

    companion object : BuildDescGen {
        object LogSimple {
            const val name = "LogSimple"
            const val srcDir = "libs/logs/LogSimple/src_main"
        }

        object LocalHostSocket {
            const val name = "LocalHostSocket"
            const val srcDir = "libs/LocalHostSocket/src_main"
        }

        object BaseTypeAlias {
            const val name = "BaseTypeAlias"
            const val srcDir = "libs/BaseTypeAlias/src_main"
        }

        object ScriptStrRunEnv {
            const val name = "ScriptStrRunEnv"
            const val srcDir = "libs/ScriptStrRunEnv/src_main"
        }

        object RunScriptStr {
            const val name = "RunScriptStr"
            const val srcDir = "tools/RunScriptStr/src_main"
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