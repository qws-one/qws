//

object BuildDescInit {
    const val place = "app__init/src_init/BuildDesc.kt"

    @JvmStatic
    fun main(args: Array<String>) {
        println(" BuildDescInit.main ${args.toList()}")
        BuildDesc.onAppInit(java.io.File(place).absolutePath)
    }
}