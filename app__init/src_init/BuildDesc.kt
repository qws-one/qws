@file:Suppress("RemoveRedundantQualifierName")

typealias JvmFile = java.io.File


@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused", "FunctionName")
object BuildDesc {
    const val kotlin_version = "1.7.10"
    const val jvmTarget = "17"

    const val opt_local_gradle = "/opt/local/gradle"

    const val app_init_by_gradle = "app__init/app_init_by_gradle"
    const val gradle_build_place_conf_txt = "$app_init_by_gradle/../gradle.build.place.conf.txt"

    const val build_gradle_root_kts = ".root.gradle.kts"
    const val settings_gradle_kts = "settings.gradle.kts"
    const val build_gradle_kts = "build.gradle.kts"
    const val all_build_place_txt = ".all.build.place.txt"
    const val dependencies_src_txt = ".dependencies_src.txt"

    @Suppress("ObjectPropertyName")
    val _gradle_dir = ".gradle"

    val emptyFile = JvmFile("")
    private val JvmFile.up: JvmFile get() = absoluteFile.parentFile

    @Suppress("NOTHING_TO_INLINE")
    inline fun file(file: JvmFile): JvmFile = file.absoluteFile

    @Suppress("NOTHING_TO_INLINE")
    inline fun file(baseFile: JvmFile, path: String) = file(JvmFile(baseFile, path))
    fun JvmFile.forEachDir(action: (JvmFile) -> Unit) = listFiles()?.forEach { if (it.isDirectory) action(it) }
    val JvmFile.deleteIfExist get() = with(runtime) { deleteIfExist }

    @Suppress("DEPRECATION")
    private fun String.capitalize() = get(0).toUpperCase() + substring(1)

    class AppsPlace(val dir: JvmFile, val placeDir: JvmFile, val name: String = qws, val gradleBuildPlaceConfTxt: JvmFile = emptyFile) {
        companion object {
            const val qws = "qws"
            fun from(place: JvmFile, name: String) = AppsPlace(
                place,
                file(place, name),
                qws,
                file(place, gradle_build_place_conf_txt).run { if (exists()) absoluteFile else emptyFile })
        }
    }

    val libTools = lib.tools
    lateinit var runtime: Runtime

    interface Runtime {
        val JvmFile.deleteIfExist: Boolean
        infix fun JvmFile.update(text: String)
        fun JvmFile.create(getText: () -> String)

        companion object {

            val empty = object : Runtime {
                override val JvmFile.deleteIfExist: Boolean get() = false
                override fun JvmFile.update(text: String) {}
                override fun JvmFile.create(getText: () -> String) {}
            }

            val writable = object : Runtime {
                override val JvmFile.deleteIfExist: Boolean
                    get() {
                        if (exists()) {
                            if (isDirectory) deleteRecursively()
                            else delete()
                            println("remove: $absolutePath")
                            return true
                        }
                        return false
                    }

                override infix fun JvmFile.update(text: String) = if (!exists()) create { text } else if (readText() != text) {
                    parentFile.mkdirs()
                    writeText(text)
                    println("update: $absolutePath")
                } else Unit

                override fun JvmFile.create(getText: () -> String) = if (!exists()) {
                    val str = getText()
                    if (str.isNotEmpty()) {
                        parentFile.mkdirs()
                        writeText(str)
                        println("create: $absolutePath")
                    } else Unit
                } else Unit
            }
        }
    }

    class BuildGradlePlus(
        val plugins: List<String> = emptyList(), val imports: String = "", val plus: String = ""
    )

    object lib {
        private infix fun JvmFile.update(text: String) = with(runtime) { update(text) }
        private fun JvmFile.create(getText: () -> String) = with(runtime) { create(getText) }

        class tools(val appsPlace: AppsPlace, val map: Map<String, SubProject>) {
            val placeDir = appsPlace.placeDir.also { mapConfigured[appsPlace.placeDir.name] = appsPlace }

            //val SubProject.place get() = file(placeDir, path())
            fun SubProject.place(path: String) = file(placeDir, "${path()}/$path")
            val SubProject.relativePlace get() = "${placeDir.name}/${path()}".replace("//", "/").replace("//", "/")
            fun SubProject.relativePlace(path: String) = "${relativePlace}/$path".replace("//", "/")

            companion object {
                val mapConfigured = mutableMapOf<String, AppsPlace>()

                fun allUsedGradleLinkToIde(place: JvmFile) = file(place, ".idea/gradle.xml") update """<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="GradleMigrationSettings" migrationVersion="1" />
  <component name="GradleSettings">
    <option name="linkedExternalProjectsSettings">
${
                    listOf(app_init_by_gradle).plus(mapConfigured.keys.sorted()).joinToString("\n") {
                        """      <GradleProjectSettings>
        <option name="delegatedBuild" value="true" />
        <option name="testRunner" value="GRADLE" />
        <option name="distributionType" value="LOCAL" />
        <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}/$it" />
        <option name="gradleHome" value="$opt_local_gradle" />
        <option name="modules">
          <set>
            <option value="${'$'}PROJECT_DIR${'$'}/$it" />
          </set>
        </option>
      </GradleProjectSettings>"""
                    }
                }
    </option>
  </component>
</project>"""

                fun allUnusedGradleUnlink(place: JvmFile) = place.forEachDir { dir ->
                    if (!mapConfigured.containsKey(dir.name) && file(dir, build_gradle_root_kts).exists()) unusedGradleUnlink(dir)
                }

                fun unusedGradleUnlink(dir: JvmFile) {
                    file(dir, build_gradle_root_kts).deleteIfExist
                    file(dir, settings_gradle_kts).deleteIfExist
                    file(dir, all_build_place_txt).deleteIfExist
                    file(dir, _gradle_dir).deleteIfExist
                }

                //const val extBuildDir = true
            }

            var extBuildDir = true
            fun extBuildDir() = extBuildDir && file(placeDir, all_build_place_txt).exists()
            fun localBuildDir() {
                extBuildDir = false
            }

            fun SubProject.srcDirs() = when (conf) {
                is Main -> listOf(*conf.srcDirs.toTypedArray(), Main.srcDir)
                else -> conf.srcDirs
            }

            fun disable_build_gradle() = unusedGradleUnlink(placeDir)

            fun settings_gradle() {/*
                TODO: buildCache {
                    local {
                        directory = ...
                    }
                }                  */
                file(placeDir, settings_gradle_kts) update """rootProject.buildFileName = "$build_gradle_root_kts"
${map.keys.sorted().joinToString("\n") { "include(\"$it\")" }} 
"""
            }

            fun build_gradle() = buildGradle()
            fun build_gradle(plus: BuildGradlePlus) = buildGradle(imports = plus.imports, plugins = plus.plugins, plus = plus.plus)
            fun buildGradle(imports: String = "", plugins: List<String> = emptyList(), plus: String = "") {
                if (extBuildDir && emptyFile != appsPlace.gradleBuildPlaceConfTxt) {
                    file(placeDir, all_build_place_txt) update appsPlace.gradleBuildPlaceConfTxt.readText() + "/${appsPlace.name}-${placeDir.name}"
                }
                //println("tools.buildGradle extBuildDir=$extBuildDir placeDir=$placeDir")
                file(placeDir, build_gradle_root_kts) update imports + buildGradleInitialContent(plugins) + plus
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
/$all_build_place_txt
"""
            }

            @Suppress("NOTHING_TO_INLINE")
            inline fun List<String>.joinWithTab() = joinToString(separator = "\n", postfix = if (isEmpty()) "" else "\n") { "    ${it.trim()}" }

            @Suppress("SpellCheckingInspection")
            fun buildGradleInitialContent(plugins: List<String> = emptyList()) = """
plugins {
    kotlin("jvm") version "$kotlin_version"
${plugins.joinWithTab()}}
${if (extBuildDir()) "val buildPlace by extra(file(\"$all_build_place_txt\").readLines().first())" else ""}
allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "$jvmTarget"
    }
${if (extBuildDir()) "    buildDir = file(buildPlace + path.replace(':', '/')+\"/build\")\n" else ""}}
"""

            @Suppress("SpellCheckingInspection")
            fun build_gradle_of_sub_projects() = map.values.forEach { prj ->
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

            fun active(vararg list: SubProject) = list.forEach { prj ->
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
    abstract class ProjectsSetPlace(val mapOfProjects: MutableMap<String, SubProject> = mutableMapOf()) {

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

    class ProjectsSetSettingsDelegate {
        operator fun <T : appsBase> getValue(thisRef: T, property: kotlin.reflect.KProperty<*>) = ProjectsSetSettings(thisRef)
    }

    class ProjectsSetSettings<T : appsBase>(val projectsSetPlace: T) {
        fun initTools(projectDir: JvmFile) = with(projectsSetPlace) {
            _tools = lib.tools(AppsPlace.from(projectDir, name.ifEmpty { this.javaClass.simpleName }), mapOfProjects)
        }

        fun configure(projectDir: JvmFile) {
            initTools(projectDir)
            projectsSetPlace._tools.disable_build_gradle()
        }

        fun configure(projectDir: JvmFile, block: T.() -> Unit) = if (configureFull(projectDir, block)) with(projectsSetPlace._tools) {
            build_gradle_of_sub_projects()
            build_gradle()
            settings_gradle()
        } else Unit

        fun configureFull(projectDir: JvmFile, block: T.() -> Unit) = with(projectsSetPlace) {
            initTools(projectDir)
            block()
            if (mapOfProjects.isEmpty()) {
                _tools.disable_build_gradle()
                false
            } else true
        }
    }

    abstract class appsBase(val name: String = "") : ProjectsSetPlace() {
        @Suppress("PropertyName")
        lateinit var _tools: lib.tools

        @Suppress("PropertyName")
        val Settings get() = ProjectsSetSettingsDelegate()
        abstract val settings: ProjectsSetSettings<*>

        fun localBuildDir() = _tools.localBuildDir()
        fun active(vararg list: SubProject) = _tools.active(*list)
    }

    object root : appsBase("appsSetQws") {
        override val settings by Settings

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

    class app(name: String) : appsBase(name) {
        override val settings by Settings
        val cli by projectConf<CliApp> {}
    }

    object app_cliA : appsBase() {
        override val settings by Settings
        val cliA by projectConf<CliApp> { implementation(jar.xodus) }
    }

    object app_cliAB : appsBase() {
        override val settings by Settings
        val libA by projectConf
        val appA by projectConf<CliApp> { implementation(libA, jar.xodus) }
        val cliB by projectConf<CliApp> { implementation(libA, jar.sql_delight) }
    }

    object jar {
        @Suppress("SpellCheckingInspection")
        const val sql_delight = "com.squareup.sqldelight:sqlite-driver:1.5.3"
        const val kotlin_scripting_jsr223 = "org.jetbrains.kotlin:kotlin-scripting-jsr223"
        const val xodus = "org.jetbrains.xodus:dnq:1.4.480"
    }

    object tempApp : appsBase() {
        override val settings by Settings
        val tempA by projectConf<CliApp> { implementation(jar.sql_delight) }
    }

    fun appsSetQwsInit(placeDir: JvmFile) = root.settings.configureFull(placeDir) {
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
        _tools.build_gradle_of_sub_projects()
        _tools.settings_gradle()
        _tools.buildGradle(
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

    class AllInit(val placeDir: JvmFile, write: Boolean, block: AllInit.() -> Unit) {
        init {
            runtime = if (write) Runtime.writable else Runtime.empty
            this.block()
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : appsBase> T.configure(block: T.() -> Unit) = this.settings.configure(placeDir, block as appsBase.() -> Unit)
        val String.configure get() = app(this).settings.configure(placeDir) { active(cli) }
        val allUnusedGradleUnlink get() = lib.tools.allUnusedGradleUnlink(placeDir)
        val allUsedGradleLinkToIde get() = lib.tools.allUsedGradleLinkToIde(placeDir)
    }

    private fun allInit(placeDir: JvmFile, write: Boolean) = AllInit(placeDir, write) {
        appsSetQwsInit(placeDir)
        "app_cli".configure
        app_cliA.configure { active(cliA) }
        app_cliAB.configure { active(appA, cliB) }

        //tempApp.configure { localBuildDir(); active(tempA) }

        allUnusedGradleUnlink
        allUsedGradleLinkToIde
    }

    fun onAppInit(absolutePath: String, write: Boolean = true): AllInit {
        val scriptFile = JvmFile(absolutePath).absoluteFile
        println(" BuildDesc scriptFile=$scriptFile")
        val init = allInit(scriptFile.up.up.up, write)
        println(" BuildDesc placeDir=${init.placeDir} configured=${BuildDesc.libTools.mapConfigured.keys}")
        return init
    }
}

