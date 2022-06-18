//
//
//

@Suppress("ClassName", "unused")
object qws : Qws(
        utils.run {
            val instance = ApplicationInstance()
            app("name1_${instance.hashHex}", instance)
        },
        utils.run {
            val instance = ProjectInstance()
            prj("name1_${instance.hashHex}", "tmp", instance)
        },
        jvmSystemOutLogger)

fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    println("Hello World!")
    Main.main()
}
