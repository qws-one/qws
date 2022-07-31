typealias Toolkit = java.awt.Toolkit
typealias StringSelection = java.awt.datatransfer.StringSelection
typealias ClipboardOwner = java.awt.datatransfer.ClipboardOwner
typealias DataFlavor = java.awt.datatransfer.DataFlavor

@Suppress("ClassName")
object SmartInterface_Generator {

    fun getStringFromSystemClipboard() =
        Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor)?.toString() ?: ""

    fun putStringToSystemClipboard(str: String) {
        val clipboardOwner: ClipboardOwner? = null
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(str), clipboardOwner)
    }

    enum class Type {
        List_String,
        String,
        Int,
        ;

        val nm
            get() = when (this) {
                List_String -> "List<String>"
                else -> name
            }
    }

    fun defaultValueBy(type: String) = when (type) {
        Type.Int.name -> "0"
        Type.String.name -> "\"\""
        else -> ""
    }


    fun interfaceFrom(interfaceName: String, inStr: String): String {
        println("SmartInterface_Generator.interfaceFrom $inStr")
        var interfaceContent = ""
        var companionContent = ""
        for (l in inStr.trim().lines()) {
            val line = l.trim()
            if (line.isNotEmpty()) {
                val (name, type) = line.split(':').map { it.trim() }.let { if (it.size == 1) it + listOf(Type.String.name) else it }
                val value = defaultValueBy(type)
                companionContent += "        override val $name = $value\n"
                interfaceContent += "    val $name: $type\n"
            }
        }
        val outStr = """interface $interfaceName {
    companion object : $interfaceName {
$companionContent    }

$interfaceContent}"""
        println("SmartInterface_Generator\n$outStr")
        return outStr
    }

    fun interfaceFromStrByValue(interfaceName: String, inStr: String): String {
        println("SmartInterface_Generator.interfaceFrom $inStr")
        var interfaceContent = ""
        var companionContent = ""
        for (l in inStr.trim().lines()) {
            val line = l.trim()
            if (line.isNotEmpty() && !line.startsWith("//") && !line.startsWith('@')) {
                val arr = line.split('=')
                val name = arr[0].split("val ")[1].trim()
                val value = arr[1].trim()
                val type = when {
                    value.startsWith("listOf") -> Type.List_String
                    value.startsWith("\"") -> Type.String
                    else -> Type.Int
                }.nm
                companionContent += "        override val $name = $value\n"
                interfaceContent += "    val $name: $type\n"
            }
        }
        val outStr = """interface $interfaceName {
    companion object : $interfaceName {
$companionContent    }

$interfaceContent}"""
        println("SmartInterface_Generator\n$outStr")
        return outStr
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val outStr = interfaceFrom("GeneratedInterface", getStringFromSystemClipboard())
        putStringToSystemClipboard(outStr)
    }
}