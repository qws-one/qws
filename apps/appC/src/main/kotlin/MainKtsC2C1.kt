import qws_local_host.LocalHost
import qws_local_host.LocalHost.SocketConfig.Companion.send
import qws_local_host.LocalHost.params
import kotlin.system.measureTimeMillis

@Suppress("unused")
object MainKtsC2C1 {
    val ktsName = this::class.simpleName ?: "MainKts"

    @Suppress("unused")
    fun go(): String {
        val t = measureTimeMillis {
            qws out "check output message"
            qws err "check error message"
            qws out "project name = ${qws.prj.name}"
        }

        val t2 = measureTimeMillis {
            val res = LocalHost.uds(MainL1.udsSuffix).params().send("10+5")
            println("from 'println' ktsName=$ktsName res='''$res'''")
            qws out ("from 'qws out' ktsName=$ktsName res=''''$res''''")
        }

        println("t=${t} t2=$t2")
        return ktsName
    }
}