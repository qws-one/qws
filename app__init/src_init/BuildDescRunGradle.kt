//

object BuildDescRunGradle {
    fun echo(dir: LocalFile, processId: Long?) = processId?.let {
        println(dir)
        println(ProcessHandle.of(processId))
        ProcessHandle.of(processId).ifPresent { println(it.info()) }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(" BuildDescRunGradle.main ${args.toList()}")
        val init = BuildDesc.onAppInit(java.io.File(BuildDescInit.place).absolutePath)
        with(BuildDescBase) {
            with(BuildDescBase.FsAtRuntime.writable(emptyFile)) {
                val mapConfigured = BuildDescBase.lib.refOfObjLib.mapConfigured
                listOf(app_init_by_gradle).plus(mapConfigured.keys).forEach {
                    val dir = file(init.placeDir, it)
                    file(dir, _gradle_dir).run {
                        if (!exists() || isFile) {
                            deleteIfExist
                            val process = Runtime.getRuntime().exec("$opt_local_gradle/bin/gradle", emptyArray(), dir)
                            echo(dir, process?.pid())
                        }
                    }
                }
            }
        }
    }
}