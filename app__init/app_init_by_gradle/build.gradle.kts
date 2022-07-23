buildscript {
    ".gradle/_generated_app_init.gradle.kts".let {
        val ktsFile = file(it)
        val ktsStr = """
${file("../src_init/BuildDesc.kt").readText()}
${file("../src_init/BuildDescConst.kt").readText()}
tasks.register("appInit"){ doLast { BuildDesc.onAppInit(project.buildFile.absolutePath) } }
"""
        if (!ktsFile.exists() || ktsFile.readText() != ktsStr) {
            ktsFile.parentFile.mkdirs()
            ktsFile.writeText(ktsStr)
        }
        apply(from = it)
    }
}
// .../qws/app__init/app_init_by_gradle$ /opt/local/gradle/bin/gradle --offline :appInit
