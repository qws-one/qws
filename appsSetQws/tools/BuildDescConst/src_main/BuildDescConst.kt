//

@Suppress("PropertyName")
interface BuildDescConst {
    companion object : BuildDescConst {
        override val qws = "qws"
        override val settings_gradle_kts = "settings.gradle.kts"
        override val dependencies_src_txt = ".dependencies_src.txt"
        override val src_module_info = "src_module_info"
        override val ModuleInfo = "ModuleInfo"

        val IdeProjectName = qws

        const val app__init = "app__init"
    }

    val qws: String
    val settings_gradle_kts: String
    val dependencies_src_txt: String
    val src_module_info: String
    val ModuleInfo: String
}
// this is copy, do not edit it