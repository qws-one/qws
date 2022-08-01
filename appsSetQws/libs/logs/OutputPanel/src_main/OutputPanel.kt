interface OutputPanel : PrintLine {
    interface Out : PrintLine {
        operator fun invoke(msg: Any?) = this.println(msg)
        infix fun print(msg: Any?)
    }

    val out: Out
    val err: Out
    fun printHyperlinkDummy(msg: Any?, block: () -> Unit)
    fun toFrontRunContent(): OutputPanel
}