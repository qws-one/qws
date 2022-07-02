object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
    }
}