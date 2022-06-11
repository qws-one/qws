//

open class IdeApi(private val ideApiObj: Any) {

    class Application(val ideAppObj: Any)

    class Project(val idePrjObj: Any)

    private val methodPrint by lazy { ideApiObj.javaClass.getDeclaredMethod("print", Any::class.java) }
    private val methodError by lazy { ideApiObj.javaClass.getDeclaredMethod("error", Any::class.java) }

    private val fieldApplication by lazy { ideApiObj.javaClass.getDeclaredField("application") }
    private val fieldProject by lazy { ideApiObj.javaClass.getDeclaredField("project") }

    private var _application: Application? = null

    val application: Application
        get() {
            if (null == _application) {
                val app = fieldApplication.get(ideApiObj)
                if (null != app) {
                    _application = Application(app)
                }
            }
            return _application ?: TODO()
        }

    val project: Project?
        get() {
            var _project: Project? = null
            val _p = fieldProject.get(ideApiObj)
            if (null != _p) {
                _project = Project(_p)
            }
            return _project
        }

    fun print(any: Any?) {
        methodPrint.invoke(ideApiObj, any)
    }

    fun error(any: Any?) {
        methodError.invoke(ideApiObj, any)
    }
}

@Suppress("ClassName")
object ideApiHolder : IdeApiHolder()

@Suppress("PropertyName")
val IDE: Any by bindings
ideApiHolder._ide = IDE

open class IdeApiHolder {
    @Suppress("PropertyName")
    lateinit var _ide: Any
}

object Ide : IdeApi(ideApiHolder._ide)

Ide.print("kotlinVersion=${KotlinVersion.CURRENT}")
Ide.print("jvmVersion=${Runtime.version()}")
Ide.print("jvmVersion=${Runtime.version().feature()}")

val bindingsMap = bindings
if (bindingsMap is MutableMap<String, Any?>) {
    val key = "qws.tmp.item"
    bindingsMap[key] = "(qws tmp item value)"
    Ide.print(bindingsMap[key])
}

class QwsUtils16 {
    interface Interface {
        val Any.hashHex get() = hashCode().toString(16)
    }

    companion object : Interface
}

class QwsUtils10 {
    interface Interface {
        val Any.hashDec get() = hashCode().toString(10)
    }

    companion object : Interface
}

class QwsUtils8 {
    interface Interface {
        val Any.hashOct get() = hashCode().toString(8)
    }

    companion object : Interface
}

class QwsUtils2 {
    interface Interface {
        val Any.hashBin get() = hashCode().toString(2)
    }

    companion object : Interface
}

class QwsUtilsAll : QwsUtils16.Interface, QwsUtils10.Interface, QwsUtils8.Interface, QwsUtils2.Interface {
    inline fun <T1, T2, R> multiWith(t1: T1, t2: T2, block: T1.() -> (T2.() -> R)): R {
        return block.invoke(t1).invoke(t2)
    }

    inline fun <T1, T2, T3, R> multiWith(t1: T1, t2: T2, t3: T3, block: T1.() -> (T2.() -> (T3.() -> R))): R {
        return block.invoke(t1).invoke(t2).invoke(t3)
    }

    inline fun <T1, T2, T3, T4, R> multiWith(t1: T1, t2: T2, t3: T3, t4: T4, block: T1.() -> (T2.() -> (T3.() -> (T4.() -> R)))): R {
        return block.invoke(t1).invoke(t2).invoke(t3).invoke(t4)
    }
}

class QwsUtilsTmp {
    inline fun <T1, T2, R> multiWith(t1: T1, t2: T2, block: T1.() -> (T2.() -> R)): R {
        return block.invoke(t1).invoke(t2)
    }

    inline fun <T1, T2, T3, R> multiWith(t1: T1, t2: T2, t3: T3, block: T1.() -> (T2.() -> (T3.() -> R))): R {
        return block.invoke(t1).invoke(t2).invoke(t3)
    }

    inline fun <T1, T2, T3, T4, R> multiWith(t1: T1, t2: T2, t3: T3, t4: T4, block: T1.() -> (T2.() -> (T3.() -> (T4.() -> R)))): R {
        return block.invoke(t1).invoke(t2).invoke(t3).invoke(t4)
    }
}

typealias ApplicationInstance = Any
typealias ProjectInstance = Any

class QwsLocal {

    interface Logger {
        infix fun out(any: Any?)
        infix fun err(any: Any?)
        fun err(msg: String, throwable: Throwable)
    }

    open class Application(
        val name: String,
        val instance: ApplicationInstance
    ) {
        override fun toString(): String {
            return QwsUtils16.run { "name=$name appId=${instance.hashHex}" }
        }
    }

    open class Project(
        val name: String,
        val path: String,
        val instance: ProjectInstance
    ) {
        override fun toString(): String {
            return Qws.utils.run { "name=$name appId=${instance.hashHex} path=$path" }
        }
    }
}

open class Qws(val app: QwsLocal.Application, val prj: QwsLocal.Project, val logger: QwsLocal.Logger) {

    @Suppress("NOTHING_TO_INLINE")
    companion object {
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
    object : QwsLocal.Logger {
        override infix fun out(any: Any?) {
            Ide.print(any)
        }

        override infix fun err(any: Any?) {
            Ide.error(any)
        }

        override fun err(msg: String, throwable: Throwable) {
            Ide.error("Err: $msg\n$throwable\n${throwable.stackTrace.joinToString("\n")}")
        }
    })


object Main {
    fun main() {
        qws out qws.app
        qws out qws.prj

        qws out qws.hashCode().toString(16)
        qws out qws.utils.run { qws.hashHex }

        with(QwsUtils16.Companion) {
            with(QwsUtils8.Companion) {
                with(QwsUtils2.Companion) {
                    qws out "whith " + qws.hashHex
                    qws out "whith " + qws.hashOct
                    qws out "whith " + qws.hashBin
                }
            }
        }

        QwsUtilsTmp().run {

            multiWith(QwsUtils8.Companion, QwsUtils2.Companion) {
                {
                    qws out "multiWith2 " + qws.hashOct
                    qws out "multiWith2 " + qws.hashBin
                }
            }

            multiWith(QwsUtils16.Companion, QwsUtils8.Companion, QwsUtils2.Companion) {
                {
                    {
                        qws out "multiWith3 " + qws.hashHex
                        qws out "multiWith3 " + qws.hashOct
                        qws out "multiWith3 " + qws.hashBin
                    }
                }
            }

            multiWith(QwsUtils16.Companion, QwsUtils10.Companion, QwsUtils8.Companion, QwsUtils2.Companion) {
                {
                    {
                        {
                            qws out "multiWith4 " + qws.hashHex
                            qws out "multiWith4 " + qws.hashDec
                            qws out "multiWith4 " + qws.hashOct
                            qws out "multiWith4 " + qws.hashBin
                        }
                    }
                }
            }
        }

        qws.utils.run {
            qws out "qws.utils.run " + qws.hashHex
            qws out "qws.utils.run " + qws.hashDec
            qws out "qws.utils.run " + qws.hashOct
            qws out "qws.utils.run " + qws.hashBin
        }
    }

    fun main2() {
        qws err qws.app
        qws err qws.prj
    }

    fun main3() = qws.utils.run {
        qws out "main3 qws.utils.run " + qws.hashHex
        qws out "main3 qws.utils.run " + qws.hashDec
        qws out "main3 qws.utils.run " + qws.hashOct
        qws out "main3 qws.utils.run " + qws.hashBin
    }

}

Main.main()
Main.main2()
Main.main3()
