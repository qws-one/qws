import javax.script.ScriptException
import kotlin.system.measureTimeMillis

object LocalHost {

    sealed class SocketConfig(val protocolFamily: java.net.ProtocolFamily, val param: Param) {
        val byteBufferInputSize = param.byteBufferInputSize
        val timeoutSecondsOfWaitClientConnection = param.timeoutSecondsOfWaitClientConnection
        val acceptClientConnectionCount = param.acceptClientConnectionCount

        abstract val address: java.net.SocketAddress
        abstract fun description(): String
        open fun onFinally() {}

        sealed class TCP(val port: Int, param: Param) : SocketConfig(java.net.StandardProtocolFamily.INET, param) {

            class LocalHost(port: Int, param: Param = default) : TCP(port, param) {
                override val address by lazy { java.net.InetSocketAddress(java.net.Inet4Address.getLoopbackAddress(), port) }
                override fun description() = "tcp: 127.0.0.1:$port"
            }
        }

        class UDS(val path: String, param: Param = default) : SocketConfig(java.net.StandardProtocolFamily.UNIX, param) {
            override val address by lazy { java.net.UnixDomainSocketAddress.of(path) }
            override fun description() = "uds: $path"
            override fun onFinally() {
                java.nio.file.Files.deleteIfExists(address.path)
            }
        }

        data class Param(
            val byteBufferInputSize: Int = default.byteBufferInputSize,
            val timeoutSecondsOfWaitClientConnection: Int = default.timeoutSecondsOfWaitClientConnection,
            val acceptClientConnectionCount: Int = default.acceptClientConnectionCount,
        ) {
            companion object {
                val default = Param(
                    4096,
                    240,
                    1,
                )
            }
        }

        class Runtime(val msg: String, val connectionIndex: Int, val param: Param, val result: (String) -> Unit)
        companion object {
            val default = Param()

            val defaultTCP = TCP.LocalHost(8080)
            val defaultUDS = UDS(tmpUdsFilePath(defaultTCP.port)) //"File.createTempFile .../uds_tmp_8080")

            fun tcp(port: Int, param: Param = default) = TCP.LocalHost(port, param)
            fun uds(path: String, param: Param = default) = UDS(path, param)

            fun instanceWithUpdatedParams(socketConfig: SocketConfig, param: Param): SocketConfig {
                val result = when (socketConfig) {
                    is UDS -> UDS(socketConfig.path, param)
                    is TCP.LocalHost -> TCP.LocalHost(socketConfig.port, param)
                    else -> TODO()
                }
                return result
            }
        }

        fun params(
            byteBufferInputSize: Int = default.byteBufferInputSize,
            timeoutSecondsOfWaitClientConnection: Int = default.timeoutSecondsOfWaitClientConnection,
            acceptClientConnectionCount: Int = default.acceptClientConnectionCount,
        ) = instanceWithUpdatedParams(this, Param(byteBufferInputSize, timeoutSecondsOfWaitClientConnection, acceptClientConnectionCount))

        @Suppress("NOTHING_TO_INLINE")
        inline fun listen(noinline block: Runtime.() -> Unit) = listen(this, block)

        @Suppress("NOTHING_TO_INLINE")
        inline fun send(str: String) = send(this, str)
    }

    val tcp get() = SocketConfig.defaultTCP
    val uds get() = SocketConfig.defaultUDS

    fun tcp(port: Int) = SocketConfig.TCP.LocalHost(port)

    fun uds(path: String) = SocketConfig.UDS(path)

    fun uds(suffix: Int) = SocketConfig.UDS(tmpUdsFilePath(suffix))

    fun tmpUdsFilePath(suffix: Int) = tmpUdsFilePath(suffix.toString())

    fun tmpUdsFilePath(suffix: String) = "/var/run/user/1001/uds_tmp_$suffix"

    fun socketListen(block: SocketConfig.Runtime.() -> Unit) = listen(SocketConfig.defaultUDS, block)

    fun listen(socketConfig: SocketConfig, block: SocketConfig.Runtime.() -> Unit) {
        println("LocalHost.listen on ${socketConfig.description()}")
        try {
            java.nio.channels.ServerSocketChannel.open(socketConfig.protocolFamily).use { serverSocketChannel ->
                serverSocketChannel.bind(socketConfig.address)
                for (connectionCount in 0 until socketConfig.acceptClientConnectionCount) {
                    serverSocketChannel.accept().use { socketChannel ->
                        val byteBufferIn = java.nio.ByteBuffer.allocate(socketConfig.byteBufferInputSize)
                        val readByteCount = socketChannel.read(byteBufferIn)
                        if (readByteCount > 0) {
                            val byteArrayIn = ByteArray(readByteCount)
                            byteBufferIn.rewind()
                            byteBufferIn.get(byteArrayIn)
                            val strIn = byteArrayIn.decodeToString()
                            var strOut = ""
                            SocketConfig.Runtime(
                                connectionIndex = connectionCount, msg = strIn, param = socketConfig.param
                            ) { strOut = it }.block()
                            val byteBufferOut = java.nio.ByteBuffer.wrap(strOut.toByteArray())
                            socketChannel.write(byteBufferOut)
                        }
                    }
                }
            }
        } finally {
            socketConfig.onFinally()
        }
    }

    fun socketSend(str: String): String = send(SocketConfig.defaultUDS, str)

    fun send(socketConfig: SocketConfig, str: String): String {
        java.nio.channels.SocketChannel.open(socketConfig.address).use { socketChannel ->
            val buf = java.nio.ByteBuffer.wrap(str.toByteArray())
            socketChannel.write(buf)
            val byteBufferIn = java.nio.ByteBuffer.allocate(socketConfig.byteBufferInputSize)
            val readByteCount = socketChannel.read(byteBufferIn)
            if (readByteCount > 0) {
                val byteArrayIn = ByteArray(readByteCount)
                byteBufferIn.rewind()
                byteBufferIn.get(byteArrayIn)
                val strIn = byteArrayIn.decodeToString()
                return strIn
            }
        }
        return ""
    }
}

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

//Ide.print("kotlinVersion=${KotlinVersion.CURRENT}")
//Ide.print("jvmVersion=${Runtime.version()}")

Ide.project?.let {
    var qwsProject: IdeApi.Project? = null
    ProjectManager.getInstance().openProjects.forEach {
        if ("qws" == it.name) qwsProject = IdeApi.Project(it)
    }
    qwsProject?.basePath?.let {
        val chanelId = 8091
        Ide.application.invokeLater {
            val currentScriptEngine = bindings["kotlin.script.engine"] as org.jetbrains.kotlin.jsr223.KotlinJsr223JvmScriptEngine4Idea// ?: TODO()
            val ktsEngine = currentScriptEngine.factory.scriptEngine
            ktsEngine.put(IdeApiHolder.keyNameIDE, bindings[IdeApiHolder.keyNameIDE])

            fun String.content() = java.io.File("$it/$this").readText()

            ktsEngine.eval("libs/libLocalHost/src/main/kotlin/LocalHost.kt".content())
            Ide.print(ktsEngine.eval("LocalHost.uds"))
            //ktsEngine.eval("ide-scripting/ide-scripting-IdeApi.kts".content())
            ktsEngine.eval("ide-scripting/ide-scripting-IdeApiPlus.kts".content())
            ktsEngine.eval("""Ide.print("from 'Ide.print'") """)
            ktsEngine.eval(
                """"""
                        + "libs/libQws/src/main/kotlin/QwsUtilsAll.kt".content()
                        + "libs/libQws/src/main/kotlin/QwsPlus.kt".content()
                        + "libs/libQws/src/main/kotlin/QwsLocal.kt".content()
                        + "libs/libQws/src/main/kotlin/Qws.kt".content()
                        + "ide-scripting/ide-scripting-qwsApi.kts".content()
            )
            ktsEngine.eval("""qws out "from 'qws out'" """)
            Thread {
                Ide.print("listen $chanelId Thread start ${Thread.currentThread()}")
                LocalHost.uds(chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
                    Ide.print("beg chanel=$chanelId index=$connectionIndex ${Thread.currentThread()} ")
                    //Ide.print("chanel $chanelId '$msg' $connectionIndex, $param")
                    val t = measureTimeMillis {
                        val scriptResult = try {
                            ktsEngine.eval(msg)?.run { toString() } ?: "Script Result is Null"
                        } catch (e: ScriptException) {
                            e.printStackTrace()
                            "Script Result is Error: ${e.toString()}"
                        }
                        Ide.print("chanel $chanelId scriptResult=${scriptResult}")
                        result(scriptResult)
                    }
                    Ide.print("end $connectionIndex ${Thread.currentThread()} t=$t")
                }
                Ide.print("listen $chanelId Thread   end ${Thread.currentThread()}")
            }.start()
        }
    }
}