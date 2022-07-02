//

@Suppress("unused")
object ModuleUtil {
    interface Info {
        val name: String
        val srcDirsCsv: String
        val relativePath: String
        val dependenciesSrcCsv: String
        val srcDirs get() = fromCsvLine(srcDirsCsv)
        val dependenciesSrc get() = fromCsvLine(dependenciesSrcCsv)
    }
    fun fromCsvLine(csvLine: String) = csvLine.split(',').map { it.trim() }
}