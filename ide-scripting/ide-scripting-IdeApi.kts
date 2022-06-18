open class IdeApi(private val ideApiObj: Any) {

    @Suppress("MemberVisibilityCanBePrivate")
    class Application(val ideAppObj: Any) {
        private val methodInvokeLater by lazy { ideAppObj.javaClass.getDeclaredMethod("invokeLater", Runnable::class.java) }

        fun invokeLater(runnable: Runnable) {
            methodInvokeLater.invoke(ideAppObj, runnable)
        }
    }

    class Project(val idePrjObj: Any) {
        private val methodGetName by lazy { idePrjObj.javaClass.getDeclaredMethod("getName") }
        private val methodGetBasePath by lazy { idePrjObj.javaClass.getDeclaredMethod("getBasePath") }

        val name get() = methodGetName.invoke(idePrjObj) as String
        val basePath get() = methodGetBasePath.invoke(idePrjObj) as String?
    }

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

    @Suppress("LocalVariableName")
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
        try {
            methodPrint.invoke(ideApiObj, any)
        } catch (e: Exception) {
            println(any)
        }
    }

    fun error(any: Any?) {
        try {
            methodError.invoke(ideApiObj, any)
        } catch (e: Exception) {
            System.err.println(any)
        }
    }
}

object IdeApiHolder {
    const val keyNameIDE = "IDE"

    @Suppress("ObjectPropertyName")
    lateinit var _ide: Any
}
IdeApiHolder._ide = bindings[IdeApiHolder.keyNameIDE] as Any

object Ide : IdeApi(IdeApiHolder._ide)