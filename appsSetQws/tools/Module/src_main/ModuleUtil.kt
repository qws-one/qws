//

@Suppress("unused")
object ModuleUtil {
    object Conf {
        const val src = "src_main"
        const val srcOne = "src_one"
        const val srcInfo = "src_module_info"
    }

    interface Info {
        val appsSetName: String
        val name: String
        val srcDirsCsv: String
        val relativePath: String
        val dependenciesSrcCsv: String
        val relativePlace get() = "$appsSetName/$relativePath"
        val srcDirs get() = fromCsvLine(srcDirsCsv)
        val dependenciesSrc get() = fromCsvLine(dependenciesSrcCsv)
    }

    fun fromCsvLine(csvLine: String) = csvLine.split(',').map { it.trim() }
}