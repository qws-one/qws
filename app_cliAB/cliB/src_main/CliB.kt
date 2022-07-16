//
object CliB {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")

//        qws.one.MainA.main(args)
//        qws.one.MainB.main(args)
        qws.one.MainC.main(args)
    }
}