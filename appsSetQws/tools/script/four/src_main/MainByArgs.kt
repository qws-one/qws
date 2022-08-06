object MainByArgs : ScriptStr {
    private val className = this::class.simpleName

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("... $className")
        println("MainArgsCheck.script bindings   =})>${runtimeMap.bindings}<({")
        println("MainArgsCheck.script scriptPath =})>${runtimeMap.scriptPath}<({")
        println("MainArgsCheck.script args       =})>${runtimeMap.args}<({")
        println("MainArgsCheck.script tmpDirBig  =})>${runtimeMap.tmpDirBig}<({")
        println("MainArgsCheck.script tmpDirQuick=})>${runtimeMap.tmpDirQuick}<({")

        scriptStrResult put """
bindings   =})>${runtimeMap.bindings}<({
scriptPath =})>${runtimeMap.scriptPath}<({
args       =})>${runtimeMap.args}<({
tmpDirBig  =})>${runtimeMap.tmpDirBig}<({
tmpDirQuick=})>${runtimeMap.tmpDirQuick}<({
$className ..."""
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            chanelId = chanelIdIdeScriptListener,
            uniqueString = "four",
            needBindings = true,
            needArgs = true,
            runEnv = runEnv.copy(needScriptPath = true, needTmpDirQuick = true),
        )
    }
}