@Suppress("ClassName", "unused")
object qws : Qws(
    utils.run {
        val instance = ApplicationInstance()
        app("name3_${instance.hashHex}", instance)
    },
    utils.run {
        val instance = ProjectInstance()
        prj("name3_${instance.hashHex}", "tmp", instance)
    },
    jvmSystemOutLoggerSE
)

