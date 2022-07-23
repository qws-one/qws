object M3 : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        //val bindings: Map<String, Any?>?  try{ by runtimeMap} catch (e:Exception){}
        val bindings = "bindings".let { runtimeMap.containsKey(it); runtimeMap[it] as? Map<*, *> }
        val ide = bindings?.get("IDE")
        scriptStrResult put "$ide $bindings  ${runtimeMap["bindings"]} \n ${ScriptStrRunEnv.scriptPath} ${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(
            args = args,
            chanelId = dummyChanelId,
            debugCase_fromFile = true,
            needSaveToFileDebug = true
//            needBindings = false
        )
    }
}