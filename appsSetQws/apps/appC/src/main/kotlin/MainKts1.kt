import kotlin.system.measureTimeMillis

@Suppress("unused")
object MainKts1 {
    @Suppress("unused")
    fun go(): String {
        val t = measureTimeMillis {
            qws out "check output message"
            qws err "check error message"
            qws out "project name = ${qws.prj.name}"
        }

        println("t=$t")
        return this::class.simpleName ?: "MainKts"
    }
}