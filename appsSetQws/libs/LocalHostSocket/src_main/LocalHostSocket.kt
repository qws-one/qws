object LocalHostSocket {
    fun configureToSystemOut() {
        log.funDebug = LogSimpleSystemOut::debug
        log.funInfo = LogSimpleSystemOut::info
        log.funErr = LogSimpleSystemOut::err
    }

    fun configureLogDebugTo(block: (Any?) -> Unit) {
        log.funDebug = block
    }

    fun configureLogInfoTo(block: (Any?) -> Unit) {
        log.funInfo = block
    }

    fun configureLogErrTo(block: (Any?) -> Unit) {
        log.funErr = block
    }

    @Suppress("ClassName")
    private object log : LogSimple {
        private object empty : LogSimple {
            override fun debug(msg: Any?) = Unit
            override fun info(msg: Any?) = Unit
            override fun err(msg: Any?) = Unit
        }

        var funDebug: (Any?) -> Unit = empty::info
        var funInfo: (Any?) -> Unit = empty::info
        var funErr: (Any?) -> Unit = empty::err
        override fun debug(msg: Any?) = funDebug(msg)
        override fun info(msg: Any?) = funInfo(msg)
        override fun err(msg: Any?) = funErr(msg)
    }

    sealed class SocketConfig(val protocolFamily: java.net.ProtocolFamily, val param: Param) {
        val byteBufferInputSize = param.byteBufferInputSize
        val timeoutSecondsOfWaitClientConnection = param.timeoutSecondsOfWaitClientConnection
        val acceptClientConnectionCount = param.acceptClientConnectionCount

        abstract val address: java.net.SocketAddress
        abstract fun description(): String
        abstract val isFree: Boolean
        abstract fun tryToFree(): Boolean
        open fun onFinally() {}

        sealed class TCP(val port: Int, param: Param) : SocketConfig(java.net.StandardProtocolFamily.INET, param) {

            class LocalHost(port: Int, param: Param = default) : TCP(port, param) {
                override val address by lazy { java.net.InetSocketAddress(java.net.Inet4Address.getLoopbackAddress(), port) }
                override fun description() = "tcp: 127.0.0.1:$port"
                override val isFree get(): Boolean = true //TODO("Not yet implemented")
                override fun tryToFree(): Boolean = true //?maybe waite...? TODO("Not yet implemented")
            }
        }

        class UDS(val fsPath: String, param: Param = default) : SocketConfig(java.net.StandardProtocolFamily.UNIX, param) {
            override val address by lazy { LocalFile(fsPath).parentFile.mkdirs(); java.net.UnixDomainSocketAddress.of(fsPath) }
            override fun description() = "uds: $fsPath"
            override val isFree get() = !LocalFile(fsPath).exists()
            override fun tryToFree() = LocalFile(fsPath).delete().also { log debug "LocalHostSocket: result of tryToFree '$fsPath' is: $it" }
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
                    65536, //4096,
                    240,
                    1,
                )
            }
        }

        class Runtime(val msg: String, val connectionIndex: Int, val param: Param, val closeChannel: () -> Unit, val result: (String) -> Unit)
        companion object {
            val default = Param()

            //$ while true;                 do echo -e 'HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World</p>\r\n'  | nc -l 8080; done #server
            //$ for NUM_VALUE in 0 1 2 3 4; do echo -e "HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World num=$NUM_VALUE</p>\r\n"  | nc -l 8080; done
            //$ echo -ne 'GET / HTTP/1.0\r\n\r\n' | nc -N 127.0.0.1 8080 #client
            val defaultTCP = TCP.LocalHost(8080)

            // $ while true; do echo -e 'HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World</p>\r\n'  | nc -lU /var/run/user/1001/uds_tmp_8080; done #server
            // $ echo -ne "GET / HTTP/1.0\r\n\r\n" | nc -NU /var/run/user/1001/uds_tmp_8080 #client
            val defaultUDS = UDS(tmpUdsFilePath(defaultTCP.port)) //"File.createTempFile .../uds_tmp_8080")

            fun tcp(port: Int, param: Param = default) = TCP.LocalHost(port, param)
            fun uds(path: String, param: Param = default) = UDS(path, param)

            fun instanceWithUpdatedParams(socketConfig: SocketConfig, param: Param): SocketConfig {
                @Suppress("REDUNDANT_ELSE_IN_WHEN") val result = when (socketConfig) {
                    is UDS -> UDS(socketConfig.fsPath, param)
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
    fun uds(tmpDir: String, suffix: Int) = SocketConfig.UDS(tmpUdsFilePath(tmpPlace(tmpDir.trim().ifEmpty { tempDir }), suffix.toString()))

    private const val dirName = "qws_LocalHostSocket"
    private val tempDir by lazy { kotlin.io.path.Path(System.getProperty("user.home"), ".cache/").toAbsolutePath().toString() }
    private fun tmpPlace(tmpDir: String) = kotlin.io.path.Path(tmpDir, dirName).toAbsolutePath().toString()
    fun tmpUdsFilePath(suffix: Int) = tmpUdsFilePath(suffix.toString())
    fun tmpUdsFilePath(suffix: String) = tmpUdsFilePath(tmpPlace(tempDir), suffix)
    fun tmpUdsFilePath(tmpPlace: String, suffix: String) = kotlin.io.path.Path(tmpPlace, "uds_tmp_$suffix").toAbsolutePath().toString()

    fun socketListen(block: SocketConfig.Runtime.() -> Unit) = listen(SocketConfig.defaultUDS, block)

    fun listen(socketConfig: SocketConfig, block: SocketConfig.Runtime.() -> Unit) {
        if (!socketConfig.isFree) "Error start listen on ${socketConfig.description()}, looks like already in use.".let {
            log err it
            throw RuntimeException(it)
        }
        log info "LocalHostSocket.listen on ${socketConfig.description()}"
        try {
            java.nio.channels.ServerSocketChannel.open(socketConfig.protocolFamily).use { serverSocketChannel ->
                serverSocketChannel.bind(socketConfig.address)
                for (connectionCount in 0 until socketConfig.acceptClientConnectionCount) {
                    var closeChannel = false
                    serverSocketChannel.accept().use { socketChannel ->
                        val byteBufferIn = java.nio.ByteBuffer.allocate(socketConfig.byteBufferInputSize)
                        val readByteCount = socketChannel.read(byteBufferIn)
                        if (readByteCount > 0) {
                            val byteArrayIn = ByteArray(readByteCount)
                            byteBufferIn.rewind()
                            byteBufferIn.get(byteArrayIn)
                            val strIn = byteArrayIn.decodeToString()
                            var strOut = ""
                            SocketConfig.Runtime(connectionIndex = connectionCount,
                                msg = strIn,
                                param = socketConfig.param,
                                closeChannel = { closeChannel = true },
                                result = { strOut = it }).block()
                            log debug "LocalHostSocket.listen on ${socketConfig.description()} receive: '$strIn'"
                            log debug "LocalHostSocket.listen on ${socketConfig.description()} result : '$strOut'"
                            val byteBufferOut = java.nio.ByteBuffer.wrap(strOut.toByteArray())
                            socketChannel.write(byteBufferOut)
                        }
                    }
                    if (closeChannel) return
                }
            }
        } catch (e: Exception) {
            log err e
        } finally {
            socketConfig.onFinally()
        }
    }

    fun socketSend(str: String): String = send(SocketConfig.defaultUDS, str)

    fun send(socketConfig: SocketConfig, str: String): String {
        //log info socketConfig.description()
        try {
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
        } catch (e: Exception) {
            log err "Error while LocalHostSocket.sent to '${socketConfig.description()}' error: '$e'"
        }
        return ""
    }
}
