//
object Cli {
    class InternalA {
        companion object {
            val n0 = javaClass.canonicalName
            val n00 = javaClass.name
            val n000 = this::class.java.name
            val n1 = javaClass.enclosingClass.canonicalName
            val n2 = javaClass.enclosingMethod
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("${this::class.simpleName} args.size=${args.size} args=${args.toList()}")
        class InternalB {
            val n0 = javaClass.canonicalName
            val n00 = javaClass.name
            val n1 = javaClass.enclosingClass.canonicalName
            val n2 = javaClass.enclosingMethod.name
        }
        println("Cli.main ${InternalA.n0}")
        println("Cli.main ${InternalA.n00}")
        println("Cli.main ${InternalA.n000}")
        println("Cli.main ${InternalA.n1}")
        println("Cli.main ${InternalA.n2}")
        println("Cli.main ${InternalB().n0}")
        println("Cli.main ${InternalB().n00}")
        println("Cli.main ${InternalB().n1}")
        println("Cli.main ${InternalB().n2}")
    }
}

//Cli args.size=0 args=[]
//Cli.main Cli.InternalA.Companion
//Cli.main Cli$InternalA$Companion
//Cli.main Cli.InternalA
//Cli.main null
//Cli.main null
//Cli.main Cli$main$InternalB
//Cli.main Cli
//Cli.main main
