package qws_local_host

import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files


object LocalHost {

    sealed class SocketConfig(val protocolFamily: ProtocolFamily, val param: Param) {
        val byteBufferInputSize = param.byteBufferInputSize
        val timeoutSecondsOfWaitClientConnection = param.timeoutSecondsOfWaitClientConnection
        val acceptClientConnectionCount = param.acceptClientConnectionCount

        abstract val address: SocketAddress
        abstract fun desccription(): String
        open fun onFinally() {}

        sealed class TCP(val port: Int, param: Param) :
            SocketConfig(StandardProtocolFamily.INET, param) {

            class LocalHost(port: Int, param: Param = default) : TCP(port, param) {
                override val address by lazy { InetSocketAddress(Inet4Address.getLoopbackAddress(), port) }
                override fun desccription() = "tcp: 127.0.0.1:$port"
            }
        }

        class UDS(val path: String, param: Param = default) :
            SocketConfig(StandardProtocolFamily.UNIX, param) {
            override val address by lazy { UnixDomainSocketAddress.of(path) }
            override fun desccription() = "uds: $path"
            override fun onFinally() {
                Files.deleteIfExists(address.path)
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

            //$ while true;                 do echo -e 'HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World</p>\r\n'  | nc -l 8080; done #server
            //$ for NUM_VALUE in 0 1 2 3 4; do echo -e "HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World num=$NUM_VALUE</p>\r\n"  | nc -l 8080; done
            //$ echo -ne 'GET / HTTP/1.0\r\n\r\n' | nc -N 127.0.0.1 8080 #client
            val defaultTCP = TCP.LocalHost(8080)

            // $ while true; do echo -e 'HTTP/1.1 200 OK\r\n\r\n\r\n<p>Hello World</p>\r\n'  | nc -lU /var/run/user/1001/uds_tmp_8080; done #server
            // $ echo -ne "GET / HTTP/1.0\r\n\r\n" | nc -NU /var/run/user/1001/uds_tmp_8080 #client
            val defaultUDS = UDS(tmpUdsFilePath(defaultTCP.port)) //"File.createTempFile .../uds_tmp_8080")

            fun tcp(port: Int, param: Param = default) = TCP.LocalHost(port, param)
            fun uds(path: String, param: Param = default) = UDS(path, param)

            fun SocketConfig.listen(block: Runtime.() -> Unit) = listen(this, block)

            @Suppress("NOTHING_TO_INLINE")
            inline fun SocketConfig.send(str: String) = send(this, str)
        }
    }

    val tcp get() = SocketConfig.defaultTCP
    val uds get() = SocketConfig.defaultUDS

    fun tcp(port: Int) = SocketConfig.TCP.LocalHost(port)

    fun uds(path: String) = SocketConfig.UDS(path)

    fun uds(suffix: Int) = SocketConfig.UDS(tmpUdsFilePath(suffix))

    fun tmpUdsFilePath(suffix: Int) = tmpUdsFilePath("$suffix")

    fun tmpUdsFilePath(suffix: String) = "/var/run/user/1001/uds_tmp_$suffix"

    inline fun <reified T : SocketConfig> T.params(
        byteBufferInputSize: Int = SocketConfig.default.byteBufferInputSize,
        timeoutSecondsOfWaitClientConnection: Int = SocketConfig.default.timeoutSecondsOfWaitClientConnection,
        acceptClientConnectionCount: Int = SocketConfig.default.acceptClientConnectionCount,
    ): T {
        val param = SocketConfig.Param(byteBufferInputSize, timeoutSecondsOfWaitClientConnection, acceptClientConnectionCount)
        val result = when (this) {
            is SocketConfig.UDS -> SocketConfig.UDS(path, param)
            is SocketConfig.TCP.LocalHost -> SocketConfig.TCP.LocalHost(port, param)
            else -> TODO()
        }
        return result as T
    }

    fun socketListen(block: SocketConfig.Runtime.() -> Unit) = listen(SocketConfig.defaultUDS, block)

    fun listen(socketConfig: SocketConfig, block: SocketConfig.Runtime.() -> Unit) {
        println("LocalHost.listen on ${socketConfig.desccription()}")
        try {
            ServerSocketChannel.open(socketConfig.protocolFamily).use { serverSocketChannel ->
                serverSocketChannel.bind(socketConfig.address)
                for (connectionCount in 0 until socketConfig.acceptClientConnectionCount) {
                    serverSocketChannel.accept().use { socketChannel ->
                        val byteBufferIn = ByteBuffer.allocate(socketConfig.byteBufferInputSize)
                        val readByteCount = socketChannel.read(byteBufferIn)
                        if (readByteCount > 0) {
                            val byteArrayIn = ByteArray(readByteCount)
                            byteBufferIn.rewind()
                            byteBufferIn.get(byteArrayIn)
                            val strIn = byteArrayIn.decodeToString()
                            var strOut = ""
                            SocketConfig.Runtime(
                                connectionIndex = connectionCount,
                                msg = strIn,
                                param = socketConfig.param
                            ) { strOut = it }.block()
                            val byteBufferOut = ByteBuffer.wrap(strOut.toByteArray())
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
        SocketChannel.open(socketConfig.address).use { socketChannel ->
            val buf = ByteBuffer.wrap(str.toByteArray())
            socketChannel.write(buf)
            val byteBufferIn = ByteBuffer.allocate(socketConfig.byteBufferInputSize)
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
