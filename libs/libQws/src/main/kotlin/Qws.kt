//
//
//

import qws_local.ApplicationInstance
import qws_local.ProjectInstance
import qws_local.QwsLocal
import qws_local.QwsUtilsAll

open class Qws(val app: QwsLocal.Application, val prj: QwsLocal.Project, val logger: QwsLocal.Logger) {
    @Suppress("ClassName")
    object jvmSystemOutLogger : QwsLocal.Logger by QwsLocal.jvmSystemOutLogger

    @Suppress("NOTHING_TO_INLINE")
    companion object {
        val jvmSystemOutLoggerSE = QwsLocal.jvmSystemOutLogger

        val utils = QwsUtilsAll()
        inline fun app(name: String, instance: ApplicationInstance) = QwsLocal.Application(name, instance)
        inline fun prj(name: String, path: String, instance: ProjectInstance) = QwsLocal.Project(name, path, instance)
    }

    val utils = Companion.utils

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun out(any: Any?) = logger.out(any)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun err(any: Any?) = logger.err(any)
}
