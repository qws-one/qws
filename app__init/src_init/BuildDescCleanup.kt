//

object BuildDescCleanup {
    @JvmStatic
    fun main(args: Array<String>) {
        println(" BuildDescCleanup.main ${args.toList()}")
        val init = BuildDesc.onAppInit(java.io.File(BuildDescInit.place).absolutePath, false)
        with(BuildDesc) {
            runtime = BuildDesc.Runtime.writable
            init.placeDir.forEachDir { dir ->
                file(dir, _gradle_dir).deleteIfExist;
                file(dir, all_build_place_txt).deleteIfExist
            }
            init.placeDir.walk().onEnter {when (it.name) {//@formatter:off
                    "build" -> {           it.deleteIfExist; false}
                    ".idea", ".git" -> { println(it); false}
                    else -> true                 }
                }.forEach {
                    //if (it.isFile && dependencies_src_txt == it.name) it.deleteIfExist
                }
            //@formatter:on
            file(init.placeDir, app_init_by_gradle).run {
                file(absoluteFile, _gradle_dir).deleteIfExist
                file(parentFile, "out").deleteIfExist
            }
        }
    }
}
