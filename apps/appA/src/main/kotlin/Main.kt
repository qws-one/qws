//
//
//

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