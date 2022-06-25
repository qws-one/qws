package qws_c

import J0
import J00
import J000
import J1
import QwsReflect


object MainDelegateO {
    val qwsReflectCValue = QwsReflect.Value.Companion

    val j0 = J0()
    val j00 = J00()
    val j000 = J000()

    class KJ0a(jObj: Any) : QwsReflect.Delegate.Base(jObj) {
        val f0 by QwsReflect.Delegate.C<J1>()
    }

    class RJ0b(jObj: Any) : QwsReflect.Delegate.Base(jObj) {
        val f0 by QwsReflect.Delegate.R { RJ1(it) }

        class RJ1(jObj: Any) : QwsReflect.Delegate.Base(jObj) {
            val f1 by QwsReflect.Delegate.C<String>()
            val f2 by QwsReflect.Delegate.R { RJ2(it) }

            class RJ2(jObj: Any) : QwsReflect.Delegate.Base(jObj) {
                val fOfJ2 by QwsReflect.Delegate.C<Int>()
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("<top>.main ${RJ0b(j0).f0.f1}")
        println("<top>.main ${RJ0b(j0).f0.f2.fOfJ2}")
        println("<top>.main ${RJ0b(j00).f0.f1}")
        println("<top>.main ${RJ0b(j00).f0.f2.fOfJ2}")
        println("<top>.main ${RJ0b(j000).f0.f2.fOfJ2}")
        println("<top>.main ${qwsReflectCValue<Any>(j000, "f0")}")
        println("<top>.main ${qwsReflectCValue(j000)["f0"]["f2"]["fOfJ2"].value<Int>()}")
        println("<top>.main ${qwsReflectCValue(j000)["f0"]["f2"].value<Int>("fOfJ2")}")
        println("<top>.main ${qwsReflectCValue<Int>(j000, "f0.f2.fOfJ2")}")
        println("<top>.main ${qwsReflectCValue(j000).value<Any>("f0.f2.fOfJ2")}")
        println("<top>.main ${qwsReflectCValue(j000).value<Int>("f0.f2.fOfJ2")}")
        println("<top>.main ${qwsReflectCValue(j000, "f0.f2.fOfJ2ttt", -2)}")
        println("<top>.main ${qwsReflectCValue(j000).value("f0.f2tt.fOfJ2", -3)}")
        println("<top>.main ${qwsReflectCValue(j000).value("f0tt.f2.fOfJ2", -4)}")
        println("<top>.main ${qwsReflectCValue(j000, "f0.f2.fOfJ2", -2)}")
        println("<top>.main ${qwsReflectCValue(j000).value("f0.f2.fOfJ2", -3)}")
    }
}

