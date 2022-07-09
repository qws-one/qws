package qws_c

import J0
import J00
import J000
import J1


object MainRDelegate {

    val j0 = J0()
    val j00 = J00()
    val j000 = J000()

    class KJ0a(jObj: Any) : SimpleReflect.Delegate.Base(jObj) {
        val f0 by SimpleReflect.Delegate.C<J1>()
    }

    class RJ0b(jObj: Any) : SimpleReflect.Delegate.Base(jObj) {
        val f0 by SimpleReflect.Delegate.R { RJ1(it) }

        class RJ1(jObj: Any) : SimpleReflect.Delegate.Base(jObj) {
            val f1 by SimpleReflect.Delegate.C<String>()
            val f2 by SimpleReflect.Delegate.R { RJ2(it) }

            class RJ2(jObj: Any) : SimpleReflect.Delegate.Base(jObj) {
                val fOfJ2 by SimpleReflect.Delegate.C<Int>()
            }
        }
    }
}

fun main() {
    val a: Any = MainRDelegate.j0
    val b: Any = MainRDelegate.j00
//    println("<top>.main ${Mc.j0.f0.f1}")
//    println("<top>.main ${Mc.KJ0a(a).f0.f1}")
    println("<top>.main ${MainRDelegate.RJ0b(a).f0.f1}")
    println("<top>.main ${MainRDelegate.RJ0b(a).f0.f2.fOfJ2}")
    println("<top>.main ${MainRDelegate.RJ0b(b).f0.f1}")
    println("<top>.main ${MainRDelegate.RJ0b(b).f0.f2.fOfJ2}")
    println("<top>.main ${MainRDelegate.RJ0b(MainRDelegate.j000).f0.f2.fOfJ2}")
    println("<top>.main ${SimpleReflect.Value<Any>(MainRDelegate.j000, "f0")}")
    println("<top>.main ${SimpleReflect.Value.Wrap(MainRDelegate.j000)["f0"]["f2"]["fOfJ2"].value<Int>()}")
    println("<top>.main ${SimpleReflect.Value.Wrap(MainRDelegate.j000)["f0"]["f2"].value<Int>("fOfJ2")}")
    println("<top>.main ${SimpleReflect.Value<Int>(MainRDelegate.j000, "f0.f2.fOfJ2")}")
}
