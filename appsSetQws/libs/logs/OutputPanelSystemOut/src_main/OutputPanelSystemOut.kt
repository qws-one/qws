//

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
object OutputPanelSystemOut : OutputPanel {
    override infix fun println(msg: Any?) = System.out.println(msg)
    override val out = object : OutputPanel.Out {
        override fun print(msg: Any?) = System.out.print(msg)
        override fun println(msg: Any?) = System.out.println(msg)
    }
    override val err = object : OutputPanel.Out {
        override fun print(msg: Any?) = System.err.print(msg)
        override fun println(msg: Any?) = System.err.println(msg)
    }

    override fun printHyperlinkDummy(msg: Any?, block: () -> Unit) = System.out.print(msg)
    override fun toFrontRunContent() = this
}