@Suppress("PropertyName")
interface BuildDescConstInternal {
    companion object : BuildDescConstInternal {
        override val kotlin_version = "1.7.10"
        override val jvmTarget = "17"
        override val opt_local_gradle = "/opt/local/gradle"
        override val app_init_by_gradle = "${BuildDescConst.app__init}/app_init_by_gradle"
        override val gradle_build_place_conf_txt = "$app_init_by_gradle/../conf.place.of.ext.all.gradle.build.txt"
        override val build_gradle_root_kts = ".root.gradle.kts"
        override val build_gradle_kts = ".build.gradle.kts"
        override val all_build_place_txt = ".all.build.place.txt"

        override val _gradle_dir = ".gradle"
        override val _idea_gradle_xml = ".idea/gradle.xml"

        override val src_main_java = "src/main/java"
        override val src_main_kotlin = "src/main/kotlin"
        override val src_main = "src_main"

        override val src_folder = listOf(src_main)
        override val src_main_kotlin_and_java = listOf(src_main_kotlin, src_main_java)
    }

    val kotlin_version: String
    val jvmTarget: String
    val opt_local_gradle: String
    val app_init_by_gradle: String
    val gradle_build_place_conf_txt: String
    val build_gradle_root_kts: String
    val build_gradle_kts: String
    val all_build_place_txt: String

    val _gradle_dir: String
    val _idea_gradle_xml: String

    val src_main_java: String
    val src_main_kotlin: String
    val src_main: String

    val src_folder: List<String>
    val src_main_kotlin_and_java: List<String>
}