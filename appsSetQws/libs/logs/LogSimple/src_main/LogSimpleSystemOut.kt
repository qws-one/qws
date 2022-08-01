object LogSimpleSystemOut : LogSimple {
    override fun debug(msg: Any?) = println(msg)
    override fun info(msg: Any?) = println(msg)
    override fun err(msg: Any?) = System.err.println(msg)
}