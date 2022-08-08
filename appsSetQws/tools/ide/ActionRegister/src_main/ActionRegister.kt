object ActionRegister : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
//        with(RunScriptStrFull) {
//            val (
//                appsPlace,
//                appInitDir,
//                appsSetPlace,
//                modulePlace,
//                moduleInfoSrcDir,
//            ) = appsPlacesFromFile(runtimeMap.scriptPath)
//        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            dummyChanelId,
            uniqueString = "Ide Action Register",
            //needBindings = true,
            withoutRunScriptStr = false,
            runEnv = runEnv.copy(needScriptPath = true),
            saveToPlace = places.inModuleAndIdeScripting("$ide_scripting/$ide_scripting-ActionRegister.kts")
        )
    }
}