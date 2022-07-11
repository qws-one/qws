@file:Suppress("RemoveRedundantQualifierName")

typealias JvmFile = java.io.File

//@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused", "LocalVariableName", "FunctionName")
@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused", "FunctionName")
object BuildDesc {
    private val emptyFile = JvmFile("")
    private val JvmFile.up: JvmFile get() = absoluteFile.parentFile
    private fun String.capitalize() = get(0).toUpperCase() + substring(1)

    class AppsPlace(val dir: JvmFile, val placeDir: JvmFile, val name: String = "qws", val gradleBuildPlaceConfTxt: JvmFile = emptyFile) {
        companion object {
            fun from(place: JvmFile, name: String) =
//                AppsPlace(place, JvmFile(place, name), place.name, JvmFile(place, "app_init/gradle.build.place.conf.txt"))
                AppsPlace(place, JvmFile(place, name), "qws", JvmFile(place, "app_init/gradle.build.place.conf.txt"))
        }
    }

    class BuildGradlePlus(
        val plugins: List<String> = emptyList(),
        val imports: String = "",
        val plus: String = ""
    )

    object lib {
        const val build_gradle_kts = "build.gradle.kts"
        const val gradle_build_place_txt = "gradle.build.place.txt"
        const val dependencies_src_txt = "dependencies_src.txt"

        @Suppress("NOTHING_TO_INLINE")
        inline fun file(baseFile: JvmFile, path: String) = file(JvmFile(baseFile, path))

        @Suppress("NOTHING_TO_INLINE")
        inline fun file(file: JvmFile): JvmFile = file.absoluteFile

        private infix fun JvmFile.update(text: String) =
            if (!exists()) create { text } else if (readText() != text) {
                parentFile.mkdirs()
                writeText(text)
                println("update: $absolutePath")
            } else Unit

        private inline fun JvmFile.create(getText: () -> String) = if (!exists()) {
            val str = getText()
            if (str.isNotEmpty()) {
                parentFile.mkdirs()
                writeText(str)
                println("create: $absolutePath")
            } else Unit
        } else Unit

        interface Tools {
            fun localBuildDir()
            fun build_gradle()
            fun build_gradle(plus: BuildGradlePlus)
            fun settings_gradle()
            fun build_gradle_of_sub_projects()
            fun active(vararg list: SubProject)

            companion object {
                val empty = object : Tools {
                    override fun localBuildDir() {}
                    override fun build_gradle() {}
                    override fun build_gradle(plus: BuildGradlePlus) {}
                    override fun settings_gradle() {}
                    override fun build_gradle_of_sub_projects() {}
                    override fun active(vararg list: SubProject) {}
                }
            }
        }

        class tools(val appsPlace: AppsPlace, val map: Map<String, SubProject>, block: tools.() -> Unit) : Tools {
            val placeDir = appsPlace.placeDir

            //val SubProject.place get() = file(placeDir, path())
            fun SubProject.place(path: String) = file(placeDir, "${path()}/$path")
            val SubProject.relativePlace get() = "${placeDir.name}/${path()}".replace("//", "/").replace("//", "/")
            fun SubProject.relativePlace(path: String) = "${relativePlace}/$path".replace("//", "/")

            var extBuildDir = true

            init {
                block()
            }

            override fun localBuildDir() {
                extBuildDir = false
            }

            fun SubProject.srcDirs() = when (conf) {
                is Main -> listOf(*conf.srcDirs.toTypedArray(), Main.srcDir)
                else -> conf.srcDirs
            }

            override fun settings_gradle() {
                /*
                TODO: buildCache {
                    local {
                        directory = ...
                    }
                }
                */
                file(placeDir, "settings.gradle.kts").update(map.keys.sorted().joinToString("\n") { "include(\"$it\")" })
            }

            override fun build_gradle() = buildGradle()
            override fun build_gradle(plus: BuildGradlePlus) = buildGradle(imports = plus.imports, plugins = plus.plugins, plus = plus.plus)

            fun buildGradle(imports: String = "", plugins: List<String> = emptyList(), plus: String = "") {
                if (emptyFile != appsPlace.gradleBuildPlaceConfTxt) {
                    file(placeDir, gradle_build_place_txt) update appsPlace.gradleBuildPlaceConfTxt.readText() + "/${appsPlace.name}-${placeDir.name}"
                }
                //println("tools.buildGradle extBuildDir=$extBuildDir")
                file(placeDir, build_gradle_kts) update imports + buildGradleInitialContent(plugins) + plus
                file(placeDir, ".gitignore") update """
/.gradle/
build/

/tmp/
/tmp?/
/tmp??/
/tmp???/
/tmp????/
/tmp?????/

$dependencies_src_txt
/$gradle_build_place_txt
"""
                //map.values.forEach {
                //    val src = it.place("src")
                //    println("tools.buildGradle $src")
                //    if (src.exists()) {
                //        src.copyRecursively(it.place("src_main"))
                //        src.deleteRecursively()
                //    }
                //}

                //map.values.forEach {
                //    for ((srcFrom, srcTo) in mapOf("lib " to "src_lib", "tool " to "src_tool", "main " to "src_one")) {
                //        val src = it.place(srcFrom)
                //        if (src.exists()) {
                //            println("tools.buildGradle $src")
                //            src.copyRecursively(it.place(srcTo))
                //            src.deleteRecursively()
                //        }
                //    }
                //}
            }

            @Suppress("NOTHING_TO_INLINE")
            inline fun List<String>.joinWithTab() =
                joinToString(separator = "\n", postfix = if (isEmpty()) "" else "\n") { "    ${it.trim()}" }

            @Suppress("SpellCheckingInspection")
            fun buildGradleInitialContent(plugins: List<String> = emptyList()) = """
plugins {
    kotlin("jvm") version "1.7.10"
${plugins.joinWithTab()}}
${if (extBuildDir) "val buildPlace by extra(file(\"$gradle_build_place_txt\").readLines().first())" else ""}
allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
${if (extBuildDir) "    buildDir = file(buildPlace + path.replace(':', '/')+\"/build\")\n" else ""}}
"""

            @Suppress("SpellCheckingInspection")
            override fun build_gradle_of_sub_projects() = map.values.forEach { prj ->
                var imports = ""
                var plugins = emptyList<String>()
                var sqlDelightConf = ""
                if (prj.conf.dependenciesContains(jar.sql_delight)) {
                    val srcSqlDelight = "sql"
                    val packageName = "${appsPlace.name}.one"
                    val sqFilePlace = prj.place("src/main/$srcSqlDelight/${packageName.replace('.', '/')}")
                    if ((sqFilePlace.listFiles() ?: emptyArray()).none { it.extension == "sq" }) {
                        file(sqFilePlace, "/OneData.sq").create { "--" }
                    }
                    val sqlDelightVersion = jar.sql_delight.split(':')[2].trim()
                    prj.conf.implementation("com.squareup.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")
                    imports += "@file:Suppress(\"SpellCheckingInspection\")\n"
                    plugins = listOf("""id("com.squareup.sqldelight") version "$sqlDelightVersion"""")
                    sqlDelightConf = """sqldelight {
    database("OneDatabase") {
        packageName = "qws.one"
        sourceFolders = listOf("$srcSqlDelight")
    }
}"""
                }
                var applicationConf = ""
                if (prj.conf is CliApp) {
                    //https://docs.gradle.org/current/userguide/application_plugin.html
                    //gradle run --args="foo --bar" (see JavaExec.setArgsString(java.lang.String).
                    plugins = plugins.plus(listOf("application"))
                    applicationConf = """application {
    mainClass.set("${prj.name.capitalize()}")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../${prj.relativePlace(build_gradle_kts)} run
// sh -c 'cd .../${prj.relativePlace} && /opt/local/gradle/bin/gradle run'
"""
                }
                //prj.srcDirs().forEach { prj.place(it).let { dir -> if (dir.name != src_module_info) dir.mkdirs() } }
                prj.srcDirs().forEach { if (it != src_module_info) prj.place(it).mkdirs() }
                prj.place(build_gradle_kts) update """$imports
plugins {
    kotlin("jvm")
${plugins.joinWithTab()}}
${if (prj.conf.dependencies.isNotEmpty()) "dependencies {\n${prj.conf.dependencies.joinToString("\n") { "    $it" }}\n}" else ""}
${if (prj.srcDirsStandard) "" else """sourceSets.main { java.srcDirs(${prj.srcDirs().joinToString { """"$it"""" }}) }"""}
$sqlDelightConf
$applicationConf""".trim()
            }

            override fun active(vararg list: SubProject) = list.forEach { prj ->
                when (prj.conf) {
                    is IdeAction -> objectIdeAction(prj)
                    is IdeScript -> objectIdeScript(prj)
                    is Script -> objectScript(prj)
                    is Main -> objectMain(prj)
                    is CliApp -> objectCliApp(prj, prj.name.capitalize())
                    else -> {}
                }
                prj.place(dependencies_src_txt) update prj.dependenciesSrcList().joinToString("\n")
            }

            private fun objectIdeAction(prj: SubProject) {
                objectIdeScript(prj)
            }

            private fun objectIdeScript(prj: SubProject) {
                prj.conf.implementation(root.tools.ide.TypeAlias)
                prj.conf.implementation(root.tools.ide.Lib)
                objectScript(prj)
            }

            private fun objectScript(prj: SubProject) {
                prj.conf.implementation(root.libs.Util)
                prj.conf.implementation(root.libs.LocalHostSocket)
                prj.conf.implementation(root.libs.SimpleScript)
                prj.conf.implementation(root.libs.SimpleReflect)
                prj.conf.implementation(root.tools.RunSimpleScript)
                prj.conf.implementation(root.tools.Config)
                objectMain(prj)
            }

            private fun moduleInfo(prj: SubProject) = "ModuleInfo".let { name ->
                prj.conf.implementation(root.tools.Module)
                //file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").apply { if (exists()) delete() }
                file(placeDir, "${prj.pathOfModuleInfoSrcDir()}/$name.kt").update(
                    """
object $name : ModuleUtil.Info {
    override val appsSetName = "${placeDir.name}"
    override val name = "${prj.name}"
    override val srcDirsCsv = "${prj.srcDirs().joinToString(",")}"
    override val relativePath = "${prj.moduleRelativePath()}"
    override val dependenciesSrcCsv = "${prj.dependenciesSrcList().joinToString(",")}"
}""".trim()
                )
            }

            private fun objectMain(prj: SubProject) {
                moduleInfo(prj)
                objectCliApp(prj, "Main")
            }

            private fun objectCliApp(prj: SubProject, name: String) {
                file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").create {
                    """//
object $name {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${'$'}{this::class.simpleName} args.size=${'$'}{args.size} args=${'$'}{args.toList()}")
    }
}"""
                }
            }
        }

        class SubProjectDelegate(val map: MutableMap<String, SubProject>, val conf: Conf, val block: Conf.() -> Unit) {
            operator fun getValue(thisRef: Any, property: kotlin.reflect.KProperty<*>): SubProject {
                val className = thisRef::class.java.name ?: TODO("... for BuildDesc")
                val c = '$'
                val arr = className.split("${c}root$c")
                val path = if (arr.size > 1) arr[1].split(c) else emptyList()
                val splitter = ":"
                val id = "${splitter}${path.joinToString(splitter)}$splitter${property.name}".replace("::", ":")
                if (map.containsKey(id)) {
                    return map[id] ?: TODO("... for BuildDesc")
                }
                conf.block()
                val prj = SubProject(id, path, property.name, conf)
                map[id] = prj
                return prj
            }
        }
    }

    class SubProject(val id: String, val path: List<String>, val name: String, val conf: Conf) {
        val srcDirsStandard get() = conf is ConfFresh || conf.srcDirs == src_main_kotlin_and_java
        val slash = "/"
        fun path() = "${slash}${path.joinToString(slash)}$slash$name$slash"
        fun pathOfKotlinSrcDir() = "${path.joinToString(slash)}$slash$name$slash${conf.srcDirs[0]}"
        fun pathOfModuleInfoSrcDir() = "${path.joinToString(slash)}$slash$name$slash${src_module_info}"
        fun pathSrcDirs(): List<String> = conf.srcDirs.map {
            "${moduleRelativePath()}$slash${it}"
        }

        fun dependenciesSrcList(): List<String> {
            val result = mutableListOf<String>()
            conf.dependenciesSrc.forEach { prj ->
                prj.pathSrcDirs().forEach { result.add(it) }
            }
            return result
        }

        fun moduleRelativePath() = "${path.joinToString(slash)}$slash$name"
    }

    abstract class Conf(val srcDirs: List<String>) {
        private val _dependencies = mutableSetOf<String>()
        private val _runtimeOnly = mutableSetOf<String>()
        private val _dependenciesSrc = mutableSetOf<SubProject>()
        fun dependenciesContains(element: String) = _dependencies.contains(element)
        val dependencies
            get() = _dependenciesSrc.sortedBy { it.id }.map { """implementation(project("${it.id}"))""" }
                .plus(_dependencies.sorted().map { """implementation("$it")""" })
                .plus(_runtimeOnly.sorted().map { """runtimeOnly("$it")""" })
        val dependenciesSrc get() = _dependenciesSrc.sortedBy { it.id }
        fun runtimeOnly(libNotation: String) = _runtimeOnly.add(libNotation)
        fun implementation(vararg libNotation: String) = libNotation.forEach { _dependencies.add(it) }
        fun implementation(vararg subProject: SubProject) = subProject.forEach { _dependenciesSrc.add(it) }
        fun implementation(vararg item: Any) = item.forEach {
            when (it) {
                is String -> implementation(it)
                is SubProject -> implementation(it)
            }
        }
    }

    const val src_main_java = "src/main/java"
    const val src_main_kotlin = "src/main/kotlin"
    const val src_main = "src_main"
    const val src_module_info = "src_module_info"
    val src_folder = listOf(src_main, src_module_info)
    val src_main_kotlin_and_java = listOf(src_main_kotlin, src_main_java)

    class ConfFresh : Conf(listOf(src_main_kotlin))
    class ConfC(srcDirs: List<String>) : Conf(srcDirs)
    open class ConfPlane : Conf(src_folder)
    class CliApp : Conf(src_folder)
    open class Script : Main()
    open class IdeScript : Script()
    class IdeAction : IdeScript()
    open class Main : ConfPlane() {
        companion object {
            const val srcDir = "src_one"
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    abstract class ProjectPlace(val mapOfProjects: MutableMap<String, SubProject> = mutableMapOf()) {

        @Suppress("UNCHECKED_CAST")
        inline fun projectConf(srcDirs: List<String>, noinline block: ConfC.() -> Unit) =
            lib.SubProjectDelegate(mapOfProjects, ConfC(srcDirs), block as Conf.() -> Unit)

        inline fun <reified T : Conf> projectConf(noinline block: T.() -> Unit): lib.SubProjectDelegate {
            val conf = when (T::class) {
                ConfFresh::class -> ConfFresh()
                ConfPlane::class -> ConfPlane()
                IdeAction::class -> IdeAction()
                IdeScript::class -> IdeScript()
                Script::class -> Script()
                Main::class -> Main()
                CliApp::class -> CliApp()
                else -> TODO("... for BuildDesc")
            }
            @Suppress("UNCHECKED_CAST") return lib.SubProjectDelegate(mapOfProjects, conf, block as Conf.() -> Unit)
        }

        val projectConf get() = lib.SubProjectDelegate(mapOfProjects, ConfPlane()) {}
    }

    object root : ProjectPlace(mutableMapOf()) {
        object apps {
            val appA by projectConf<ConfPlane> { implementation(libs.libQws, libs.libQwsEmptyImpl) }
            val appA_run1 by projectConf<Main> { implementation(appA, libs.libQws) }
            val appA_run2 by projectConf<Main> { implementation(appA, libs.libQws) }
            val appB by projectConf<ConfFresh> { implementation(libs.libQws, libs.libQwsEmptyImpl, libs.LocalHostSocket) }
            val appC by projectConf(src_main_kotlin_and_java) {
                implementation(appA, libs.libQws, libs.LocalHostSocket, libs.SimpleReflect)
                runtimeOnly(jar.kotlin_scripting_jsr223)
            }
            val appPlainIde by projectConf<ConfPlane> {
                implementation(tools.Config)
                implementation(tools.toolPlainIdeListener)
                implementation(libs.LocalHostSocket)
                implementation(libs.SimpleScript)
            }
        }

        object libs {
            val LocalHostSocket by projectConf
            val libQws by projectConf
            val libQwsEmptyImpl by projectConf<ConfPlane> { implementation(libQws) }
            val SimpleReflect by projectConf
            val SimpleScript by projectConf
            val Util by projectConf
        }

        object tools {
            object script {
                val one by projectConf<Script> {}
            }

            object ide {
                val TypeAlias by projectConf
                val ActionRegister by projectConf(srcDirs = listOf("src_lib", "src_actions", "src_tool")) {
                    implementation(TypeAlias)
                }
                val Lib by projectConf<ConfPlane> {
                    implementation(TypeAlias)
                }

                object action {
                    val KtsListener by projectConf<IdeAction> {}
                }

                object script {
                    val two by projectConf<IdeScript> {}
                }
            }

            val Config by projectConf
            val Module by projectConf
            val RunSimpleScript by projectConf<ConfPlane> {
                implementation(Config, Module, libs.LocalHostSocket, libs.Util)
            }
            val toolPlainIdeListener by projectConf<ConfPlane> {
                implementation(Config, libs.LocalHostSocket)
            }
            val toolPlainTypeAliasIdeListener by projectConf<ConfPlane> {
                implementation(Config, ide.TypeAlias, libs.LocalHostSocket)
            }
            val toolSimpleScriptIdeListener by projectConf<ConfPlane> {
                implementation(Config, libs.LocalHostSocket)
            }
        }
    }

    class app(appsPlace: AppsPlace, mapOfProjects: MutableMap<String, SubProject> = mutableMapOf()) : ProjectPlace(mapOfProjects),
        lib.Tools by lib.tools(appsPlace, map = mapOfProjects, block = {}) {
        fun configure(block: app.() -> Unit) {
            block()
            build_gradle_of_sub_projects()
            settings_gradle()
            build_gradle()
        }

        val cli by projectConf<CliApp> {}
    }

    abstract class appsBase(val toolsHolder: Tools.Holder = Tools.Holder()) : ProjectPlace(), lib.Tools by localTool(toolsHolder) {
        class Tools(val toolsHolder: Holder) : lib.Tools {
            class Holder {
                var tools = lib.Tools.empty
            }

            override fun localBuildDir() = toolsHolder.tools.localBuildDir()
            override fun build_gradle() = toolsHolder.tools.build_gradle()
            override fun build_gradle(plus: BuildGradlePlus) = toolsHolder.tools.build_gradle(plus)
            override fun settings_gradle() = toolsHolder.tools.settings_gradle()
            override fun build_gradle_of_sub_projects() = toolsHolder.tools.build_gradle_of_sub_projects()
            override fun active(vararg list: SubProject) = toolsHolder.tools.active(*list)
        }

        companion object {
            fun localTool(toolsHolder: Tools.Holder) = Tools(toolsHolder)
            fun <AC : appsBase> AC.configure(projectDir: JvmFile, block: AC.() -> Unit) {
                toolsHolder.tools = lib.tools(AppsPlace.from(projectDir, javaClass.simpleName), mapOfProjects) {}
                block()
                build_gradle_of_sub_projects()
                build_gradle()
                settings_gradle()
            }
        }
    }

    object appCliA : appsBase() {
        val cliA by projectConf<CliApp> { implementation(jar.xodus) }
    }

    object appCliAB : appsBase() {
        val libA by projectConf
        val appA by projectConf<CliApp> { implementation(libA, jar.xodus) }
        val cliB by projectConf<CliApp> { implementation(libA, jar.sql_delight) }
    }

    object jar {
        @Suppress("SpellCheckingInspection")
        const val sql_delight = "com.squareup.sqldelight:sqlite-driver:1.5.3"
        const val kotlin_scripting_jsr223 = "org.jetbrains.kotlin:kotlin-scripting-jsr223"
        const val xodus = "org.jetbrains.xodus:dnq:2.0.0"
    }

    object tempApp : appsBase() {
        val tempA by projectConf<CliApp> { implementation(jar.sql_delight) }
    }

    fun appsSetQwsInit(placeDir: JvmFile) = lib.tools(AppsPlace.from(placeDir, "appsSetQws"), map = root.mapOfProjects) {
        active(
            root.apps.appA,
            root.apps.appA_run1,
            root.apps.appA_run2,
            root.apps.appB,
            root.apps.appC,

            root.apps.appPlainIde,
            root.tools.ide.ActionRegister,
            root.tools.ide.action.KtsListener,
            root.tools.script.one,
            root.tools.ide.script.two,

            root.tools.toolSimpleScriptIdeListener,
            root.tools.toolPlainTypeAliasIdeListener,
            root.tools.toolPlainIdeListener,
        )
        build_gradle_of_sub_projects()
        settings_gradle()
        buildGradle(
            """
import org.jetbrains.gradle.ext.*
import org.jetbrains.gradle.ext.ActionDelegationConfig.TestRunner.CHOOSE_PER_TEST
import org.jetbrains.gradle.ext.EncodingConfiguration.BomPolicy
""",
            listOf(""" id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.5" """),
            """
group = "local.qws"
version = "1.0-SNAPSHOT"

idea.project.settings { //https://github.com/JetBrains/gradle-idea-ext-plugin/wiki
    runConfigurations {
        defaults(TestNG::class.java) {
            //vmParameters = ""
        }
    }
    doNotDetectFrameworks("android", "web")
    delegateActions {
        delegateBuildRunToGradle = true
        testRunner = CHOOSE_PER_TEST
    }
    encodings {
        bomPolicy = BomPolicy.WITH_NO_BOM
        properties {
            encoding = "<System Default>"
            transparentNativeToAsciiConversion = false
        }
    }
    taskTriggers {
        afterSync(tasks.getByName("projects"), tasks.getByName("tasks"))
    }
}
"""
        )
    }

    fun allInit(placeDir: JvmFile) {
        appsSetQwsInit(placeDir)
//        app(AppsPlace.from(placeDir, "appCli")).configure { active(cli) }
//        appCliA.configure(placeDir) { active(cliA) }

//        lib.tools(AppsPlace.from(placeDir, "appsSetQws"), map = root.mapOfProjects) { build_gradle(); settings_gradle() }
//        app(AppsPlace.from(placeDir, "appCli")).configure { }
//        appCliA.configure(placeDir) { }
        with(appsBase){
            appCliAB.configure(placeDir) {  active(appA, cliB) }
        }
        //appCliAB.configure(placeDir) { localBuildDir(); active(appA, cliB) }

//        tempApp.configure(placeDir) { localBuildDir(); active(tempA) }
    }

    //    @JvmStatic
//    @Suppress("RedundantLambdaArrow")
//    fun main(args: Array<String>) = args.let { _ -> println(args.toList()); projectInit(scriptFile(args[0]).up.up.up) }
    fun onAppInit(absolutePath: String) = allInit(scriptFile(absolutePath).up.up)
    private fun scriptFile(absolutePath: String) = JvmFile(absolutePath).absoluteFile.also { println(" BuildDesc scriptFile=$it") }
}

//tasks.create("appInit") { doLast { BuildDesc.onAppInit(project.buildFile.absolutePath) } }
//???tasks.registering("appInit") { doLast { BuildDesc.onAppInit(project.buildFile.absolutePath) } }
val appInit by tasks.registering { doLast { BuildDesc.onAppInit(project.buildFile.absolutePath) } }

// .../qws/app_init$ /opt/local/gradle/bin/gradle --offline :appInit
