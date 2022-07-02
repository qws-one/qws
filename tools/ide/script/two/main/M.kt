object M {
    fun script(args: Array<String>, bindings: Map<String, Any?>) = SimpleScript(args) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
    }

    @JvmStatic
    fun main(args: Array<String>) = RunSimpleScript(args, this, ModuleInfo)
}
