object M {

    fun script(args: Array<String>, bindings: Map<String, Any?>): String = SimpleScript(args) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        println("${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        result put "${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)
}