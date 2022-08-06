class RunScriptStr {
    abstract class ConfBase {
        abstract val chanelId: Int
        abstract val runEnv: RunEnv
        abstract val forRuntime: ForRuntime

        data class RunEnv(
            val needScriptPath: Boolean = false,
            val needTmpDirBig: Boolean = false,
            val needTmpDirQuick: Boolean = false,
        )

        data class ForRuntime(
            val args: List<String> = emptyList(),
            val scriptPath: String = "",
            val tmpDirBig: String = "",
            val tmpDirQuick: String = "",
        )
    }

    abstract class Abstract<C> {
        abstract val conf: C
        val dummyChanelId = ToolSharedConfig.chanelId_Dummy
        val runEnv get() = ConfBase.RunEnv()
    }

    data class ConfLite(
        override val chanelId: Int, override val runEnv: RunEnv, override val forRuntime: ForRuntime = ForRuntime()
    ) : ConfBase() {
        companion object : Abstract<ConfLite>() {
            override val conf get() = conf()
            fun conf(
                chanelId: Int = ToolSharedConfig.chanelId_ScriptListener,
                runEnv: RunEnv = RunEnv(),
            ) = ConfLite(chanelId = chanelId, runEnv = runEnv, forRuntime = ForRuntime())
        }
    }

    data class Conf(
        override val chanelId: Int,
        val needBindings: Boolean,
        val needScriptPath: Boolean,
        val needArgs: Boolean,
        override val runEnv: RunEnv,
        val saveToPlace: List<ModuleUtil.Place>,
        val needSaveToFileDebug: Boolean,
        val withoutRunScriptStr: Boolean,
        val debugCase_fromFile: Boolean,
        val uniqueString: String = "", // for lookup 'Entry Point File' with equals names
        var hasTypeAliasModule: Boolean = false,
        var hasUnitScriptStrRunEnv: Boolean = false,
        override val forRuntime: ForRuntime = ForRuntime()
    ) : ConfBase() {
        class ByArgs(private val argsArray: Array<String>) : Abstract<Conf>() {
            override val conf get() = conf()
            fun conf(
                uniqueString: String = "",
                args: Array<String> = argsArray,

                chanelId: Int = ToolSharedConfig.chanelId_ScriptListener,
                needBindings: Boolean = false,
                needScriptPath: Boolean = false,
                needArgs: Boolean = args.isNotEmpty(),
                runEnv: RunEnv = RunEnv(),
                needSaveToFileDebug: Boolean = true,
                withoutRunScriptStr: Boolean = true,

                ) = Conf(
                chanelId = chanelId,
                needBindings = needBindings,
                needScriptPath = needScriptPath,
                needArgs = needArgs,
                runEnv = runEnv,
                saveToPlace = emptyList(),
                needSaveToFileDebug = needSaveToFileDebug,
                withoutRunScriptStr = withoutRunScriptStr,
                debugCase_fromFile = false,
                uniqueString = uniqueString,
                forRuntime = ForRuntime(args = args.toList())
            )
        }

        companion object : Abstract<Conf>(), ModuleUtil.Places by ModuleUtil.Places {
            override val conf get() = conf()
            const val ide_scripting = "ide-scripting"
            fun conf(
                chanelId: Int = ToolSharedConfig.chanelId_ScriptListener,
                needBindings: Boolean = false,
                needScriptPath: Boolean = false,
                needArgs: Boolean = false,
                runEnv: RunEnv = RunEnv(),
                needSaveToFileDebug: Boolean = false,
                withoutRunScriptStr: Boolean = false,
                saveToPlace: List<ModuleUtil.Place> = emptyList(),
                debugCase_fromFile: Boolean = false,

                args: Array<String> = emptyArray(),
            ) = Conf(
                chanelId = chanelId,
                needBindings = needBindings,
                needScriptPath = needScriptPath,
                needArgs = needArgs,
                runEnv = runEnv,
                saveToPlace = saveToPlace,
                needSaveToFileDebug = needSaveToFileDebug,
                withoutRunScriptStr = withoutRunScriptStr,
                debugCase_fromFile = debugCase_fromFile,
                forRuntime = ForRuntime(args = args.toList())
            )
        }
    }

    @Suppress("unused", "RemoveRedundantQualifierName", "MemberVisibilityCanBePrivate")
    companion object {
        private val full = RunScriptStrFull
        val buildConfLite get() : ConfLite = buildConfLite()
        fun buildConfLite(): ConfLite = with(full) { updateRunEnv(appInitDir.fsPath, ConfLite.conf) }
        fun buildConfLite(block: ConfLite.Companion.() -> ConfLite): ConfLite = with(full) { updateRunEnv(appInitDir.fsPath, ConfLite.block()) }

        fun buildConf(block: Conf.Companion.() -> Conf): Conf = with(full) { updateRunEnv(appInitDir.fsPath, Conf.block()) }
        fun buildConf(scriptPath: String, block: Conf.Companion.() -> Conf): Conf = with(full) {
            val appInitFsPath = FsPath.from(scriptPath).file.lookupToParentByName(BuildDescConst.app__init)?.fsPath ?: TODO()
            updateRunEnv(appInitFsPath, Conf.block())
        }

        @Suppress("UNUSED_VARIABLE")
        fun buildConf(moduleInfo: ModuleUtil.Info, block: Conf.Companion.() -> Conf): Conf {
            val (appsPlace, appInitPlace, appsSetPlace, modulePlace) = with(full) { moduleInfo.appsPlaces }
            return full.updateRunEnv(appInitPlace, block(Conf.Companion))
        }

        operator fun invoke(moduleInfo: ModuleUtil.Info) = RunScriptStrFull.runScriptStr(moduleInfo, Conf.conf())
        operator fun invoke(moduleInfo: ModuleUtil.Info, block: Conf.Companion.() -> Conf) =
            RunScriptStrFull.runScriptStr(moduleInfo, block(Conf.Companion))

        operator fun invoke(args: Array<String>) = RunScriptStrFull.runScriptStr(Conf.ByArgs(args).conf())
        operator fun invoke(args: Array<String>, block: Conf.ByArgs.() -> Conf) = RunScriptStrFull.runScriptStr(Conf.ByArgs(args).block())
        fun invokeDebug(args: Array<String>, block: Conf.ByArgs.() -> Conf) {
            val conf = Conf.ByArgs(args).block()
            RunScriptStrFull.runScriptStr(conf)
        }
    }
}