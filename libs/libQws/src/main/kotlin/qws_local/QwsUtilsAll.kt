package qws_local

//
//
//

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

class QwsUtilsTmp{
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

