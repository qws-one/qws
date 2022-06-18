//
//
//

@Suppress("ClassName", "unused")
object qws : Qws(
        utils.run {
            val instance = ApplicationInstance()
            app("name2_${instance.hashHex}", instance)
        },
        utils.run {
            val instance = ProjectInstance()
            prj("name2_${instance.hashHex}", "tmp", instance)
        },
        jvmSystemOutLoggerSE)

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    println("Hello World!")
    Main.main()
    Main.main2()
    Main.main3()
}
