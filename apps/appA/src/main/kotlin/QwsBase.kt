class QwsBase {

    val log = QwsLogger.forInstance( this)

    init {
//        log = QwsLogger()

//        val processId = Runtime.getRuntime().availableProcessors()
//        val processId = Runtime.getRuntime().
//        val process = Process()

        System.nanoTime()
        System.getProperties().propertyNames()
        Thread.currentThread().id
        Thread.currentThread().contextClassLoader.name
        Thread.currentThread().contextClassLoader.parent.name
        Thread.currentThread().name
        Thread.currentThread().name
    }
}

fun main(args: Array<String>) {
    println("<top>.main " + ProcessHandle.current().info())
}