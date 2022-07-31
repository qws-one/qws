object BuildDescConstInternalGenerator {


    @Suppress("MemberVisibilityCanBePrivate")
    fun go() {
        val inStr = """
                const val kotlin_version = "1.7.10"
    const val jvmTarget = "17"

    const val opt_local_gradle = "/opt/local/gradle"

    const val app_init_by_gradle = "${'$'}{BuildDescConst.app__init}/app_init_by_gradle"
    const val gradle_build_place_conf_txt = "${'$'}app_init_by_gradle/../conf.place.of.ext.all.gradle.build.txt"

    const val build_gradle_root_kts = ".root.gradle.kts"
    const val build_gradle_kts = ".build.gradle.kts"
    const val all_build_place_txt = ".all.build.place.txt"

    @Suppress("ObjectPropertyName")
    val _gradle_dir = ".gradle"
            
    const val src_main_java = "src/main/java"
    const val src_main_kotlin = "src/main/kotlin"
    const val src_main = "src_main"

    val src_folder = listOf(src_main)
    val src_main_kotlin_and_java = listOf(src_main_kotlin, src_main_java)
"""
        val outStr = SmartInterface_Generator.interfaceFromStrByValue("BuildDescConstInternal", inStr)
        LocalFile("app__init/src_init/BuildDescConstInternal.kt").writeText(outStr)
    }

    @JvmStatic
    fun main(args: Array<String>) = go()
}