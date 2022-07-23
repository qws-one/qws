object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        println("MainTmp.main ")
        System.getProperties().keys.toList().sortedBy { it.toString() }.forEach {
            println("MainTmp.main $it")
        }
    }
}