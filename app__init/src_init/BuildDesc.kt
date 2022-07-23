@file:Suppress("RemoveRedundantQualifierName")

typealias JvmFile = java.io.File


@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused", "FunctionName")
object BuildDesc : BuildDescConst by BuildDescConst.Companion {
    const val kotlin_version = "1.7.10"
    const val jvmTarget = "17"

    const val opt_local_gradle = "/opt/local/gradle"

    const val app_init_by_gradle = "${BuildDescConst.app__init}/app_init_by_gradle"
    const val gradle_build_place_conf_txt = "$app_init_by_gradle/../conf.place.of.ext.all.gradle.build.txt"

    const val build_gradle_root_kts = ".root.gradle.kts"
    const val build_gradle_kts = ".build.gradle.kts"
    const val all_build_place_txt = ".all.build.place.txt"

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

    class AppsPlace(val dir: JvmFile, val name: String, val gradleBuildPlaceConfTxt: JvmFile = emptyFile) {
        companion object {
            fun from(place: JvmFile) = AppsPlace(place, place.name, file(place, gradle_build_place_conf_txt).run { if (exists()) absoluteFile else emptyFile })
        }
    }

    class AppsSetPlace(val placeDir: JvmFile, val name: String, val mapOfProjects: Map<String, SubProject>) {
        companion object {
            fun from(place: JvmFile, name: String, map: Map<String, SubProject>) = AppsSetPlace(file(place, name), name, map)
        }
    }

    val libTools = lib.tools
    lateinit var runtime: Runtime

    @Suppress("PropertyName")
    interface Runtime {
        val src_init: JvmFile
        val JvmFile.deleteIfExist: Boolean
        infix fun JvmFile.update(text: String)
        fun JvmFile.create(getText: () -> String)

        companion object {
            val empty = object : Runtime {
                override val src_init = emptyFile
                override val JvmFile.deleteIfExist: Boolean get() = false
                override fun JvmFile.update(text: String) {}
                override fun JvmFile.create(getText: () -> String) {}
            }

            fun writable(srcInit: JvmFile) = object : Runtime {
                override val src_init = srcInit
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
        private infix fun JvmFile.create(getText: () -> String) = with(runtime) { create(getText) }

        class tools(val appsPlace: AppsPlace, val appsSet: AppsSetPlace) {
            val map = appsSet.mapOfProjects
            val placeDir = appsSet.placeDir.also { mapConfigured[appsSet.name] = appsSet }

            //val SubProject.place get() = file(placeDir, path())
            fun SubProject.place(path: String) = file(placeDir, "${path()}/$path")
            val SubProject.relativePlace get() = "${placeDir.name}/${path()}".replace("//", "/").replace("//", "/")
            fun SubProject.relativePlace(path: String) = "${relativePlace}/$path".replace("//", "/")

            companion object {
                val mapConfigured = mutableMapOf<String, AppsSetPlace>()

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
                is Main -> listOf(*conf.srcDirs.toTypedArray(), src_module_info, Main.srcDir)
                is ConfModule -> listOf(*conf.srcDirs.toTypedArray(), src_module_info)
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
${map.keys.sorted().joinToString("\n") { "\"$it\".let { include(it); project(it).buildFileName = \"$build_gradle_kts\" }" }}"""
            }

            fun build_gradle() = buildGradle()
            fun build_gradle(plus: BuildGradlePlus) = buildGradle(imports = plus.imports, plugins = plus.plugins, plus = plus.plus)
            fun buildGradle(imports: String = "", plugins: List<String> = emptyList(), plus: String = "") {
                if (extBuildDir && emptyFile != appsPlace.gradleBuildPlaceConfTxt) {
                    file(placeDir, all_build_place_txt) update appsPlace.gradleBuildPlaceConfTxt.readText().trim() + "/${appsPlace.name}-${placeDir.name}"
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
                    val packageName = "${qws}.one"
                    val sqFilePlace = prj.place("src/main/$srcSqlDelight/${packageName.replace('.', '/')}")
                    if ((sqFilePlace.listFiles() ?: emptyArray()).none { it.extension == "sq" }) {
                        file(sqFilePlace, "/OneData.sq") create { "--" }
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
                if (prj.conf is CliApp || prj.mainClassToRun.valid) {
                    //https://docs.gradle.org/current/userguide/application_plugin.html
                    //gradle run --args="foo --bar" (see JavaExec.setArgsString(java.lang.String).
                    plugins = plugins.plus(listOf("application"))
                    applicationConf = """application {
    mainClass.set("${prj.mainClassToRun.name.ifEmpty { prj.name }.capitalize()}")
}
//                              --build-file Note: This property is deprecated and will be removed in the next major version of Gradle.
// /opt/local/gradle/bin/gradle --build-file .../${prj.relativePlace(build_gradle_kts)} run
// sh -c 'cd .../${prj.relativePlace} && /opt/local/gradle/bin/gradle run'
"""
                }
                prj.srcDirs().forEach { prj.place(it).mkdirs() }
                prj.place(build_gradle_kts) update """$imports
plugins {
    kotlin("jvm")
${plugins.joinWithTab()}}
${if (prj.conf.dependencies.isNotEmpty()) "dependencies {\n${prj.conf.dependencies.joinToString("\n") { "    $it" }}\n}" else ""}
${if (prj.srcDirsStandard) "" else """sourceSets.main { java.srcDirs(${prj.srcDirs().joinToString { """"$it"""" }}) }"""}
$sqlDelightConf
$applicationConf""".trim()
                if (prj.name == BuildDescConst::class.simpleName) "${prj.name}.kt".let {
                    file(placeDir, "${prj.pathOfKotlinSrcDir()}/$it") update file(runtime.src_init, it).run {
                        if (exists()) readText() + "\n// this is copy, do not edit it" else /*writeChangesMode == false*/ ""
                    }
                }
            }

            fun active(vararg list: SubProject) = list.forEach { prj ->
                when (prj.conf) {
                    is IdeAction -> objectIdeAction(prj)
                    is IdeScript -> objectIdeScript(prj)
                    is Script -> objectScript(prj)
                    is Main -> objectMain(prj)
                    is ConfModule -> moduleInfo(prj)
                    is CliApp -> objectCliApp(prj, prj.name.capitalize())
                    else -> {}
                }
                prj.place(dependencies_src_txt) update prj.dependenciesSrcList().joinToString("\n")
            }

            private fun objectIdeAction(prj: SubProject) = objectIdeScript(prj)

            private fun objectIdeScript(prj: SubProject) {
                prj.conf.implementation(root.tools.ide.TypeAlias)
                prj.conf.implementation(root.tools.ide.Lib)
                prj.conf.implementation(root.libs.OutputPanel)
                objectScript(prj)
            }

            private fun objectScript(prj: SubProject) {
                prj.conf.implementation(root.libs.Util)
                prj.conf.implementation(root.libs.LocalHostSocket)
                prj.conf.implementation(root.libs.SimpleReflect)
                prj.conf.implementation(root.libs.ScriptStr, root.libs.ScriptStrRunEnv)
                prj.conf.implementation(root.tools.RunScriptStr)
                prj.conf.implementation(root.tools.Config)
                objectMain(prj)
            }

            private fun moduleInfo(prj: SubProject) {
                prj.conf.implementation(root.tools.Module)
                //file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").apply { if (exists()) delete() }
                file(placeDir, "${prj.pathOfModuleInfoSrcDir()}/$ModuleInfo.kt") update """object $ModuleInfo : ModuleUtil.Info {
    override val appsSetName = "${placeDir.name}"
    override val name = "${prj.name}"
    override val srcDirsCsv = "${prj.srcDirs().joinToString(",")}"
    override val relativePath = "${prj.moduleRelativePath()}"
    override val dependenciesSrcCsv = "${prj.dependenciesSrcList().joinToString(",")}"
}"""
                if (prj.mainClassToRun.valid) objectCliApp(prj, prj.mainClassToRun.name)
            }

            private fun objectMain(prj: SubProject) {
                moduleInfo(prj)
                if (prj.mainClassToRun.isEmpty) objectCliApp(prj, "Main")
            }

            private fun objectCliApp(prj: SubProject, name: String) = file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt") create {
                """//
object $name {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${'$'}{this::class.simpleName} args.size=${'$'}{args.size} args=${'$'}{args.toList()}")
    }
}"""
            }
        }

        class SubProjectDelegate(val map: MutableMap<String, SubProject>, val mainClassToRun: MainClassToRun, val conf: Conf, val block: Conf.() -> Unit) {
            operator fun getValue(thisRef: Any, property: kotlin.reflect.KProperty<*>): SubProject {
                val className = thisRef::class.java.name ?: TODO("... for BuildDesc")
                val c = '$'
                val arr = className.split("${c}root$c") // TODO: ${c}root$c"
                val path = if (arr.size > 1) arr[1].split(c) else emptyList()
                val splitter = ":"
                val id = "${splitter}${path.joinToString(splitter)}$splitter${property.name}".replace("::", ":")
                if (map.containsKey(id)) {
                    return map[id] ?: TODO("... for BuildDesc")
                }
                conf.block()
                val mainClass = if (mainClassToRun.valid) MainClassToRun(property.name.capitalize()) else mainClassToRun
                val prj = SubProject(id, mainClass, path, property.name, conf)
                map[id] = prj
                return prj
            }
        }
    }

    class SubProject(val id: String, val mainClassToRun: MainClassToRun, val path: List<String>, val name: String, val conf: Conf) {
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

    val src_folder = listOf(src_main)
    val src_main_kotlin_and_java = listOf(src_main_kotlin, src_main_java)

    class MainClassToRun(val name: String) {
        val isEmpty get() = empty == this
        val valid get() = !isEmpty

        companion object {
            val empty = MainClassToRun("")
            val byName = MainClassToRun("")
        }
    }

    class ConfFresh : Conf(listOf(src_main_kotlin))
    class ConfC(srcDirs: List<String>) : Conf(srcDirs)
    open class ConfPlane : Conf(src_folder)
    class CliApp : Conf(src_folder)
    open class Script : Main()
    open class IdeScript : Script()
    class IdeAction : IdeScript()
    open class ConfModule : ConfPlane()
    open class Main : ConfModule() {
        companion object {
            const val srcDir = "src_one"
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    abstract class AppsSetConf(val mapOfProjects: MutableMap<String, SubProject> = mutableMapOf()) {
        val projectConf get() = lib.SubProjectDelegate(mapOfProjects, MainClassToRun.empty, ConfPlane()) {}
        inline fun projectConf(noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, MainClassToRun.empty, ConfPlane(), block) //@formatter:off
        inline fun projectConf(mainClassToRun: MainClassToRun, noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, mainClassToRun, ConfPlane(), block)
        inline fun projectConf(srcDirs: List<String>, noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, MainClassToRun.empty, ConfC(srcDirs), block)
        inline fun <reified T : Conf> project() = project<T> {} //@formatter:on
        inline fun <reified T : Conf> project(noinline block: T.() -> Unit) = project(MainClassToRun.empty, block)
        inline fun <reified T : Conf> project(mainClassToRun: MainClassToRun, noinline block: T.() -> Unit): lib.SubProjectDelegate {
            val conf = when (T::class) {
                ConfFresh::class -> ConfFresh()
                ConfPlane::class -> ConfPlane()
                IdeAction::class -> IdeAction()
                IdeScript::class -> IdeScript()
                Script::class -> Script()
                Main::class -> Main()
                ConfModule::class -> ConfModule()
                CliApp::class -> CliApp()
                else -> TODO("... for BuildDesc")
            }
            @Suppress("UNCHECKED_CAST") return lib.SubProjectDelegate(mapOfProjects, mainClassToRun, conf, block as Conf.() -> Unit)
        }
    }

    class AppsSetConfSettingsDelegate {
        operator fun <T : appsSetConf> getValue(thisRef: T, property: kotlin.reflect.KProperty<*>) = AppsSetConfSettings(thisRef)
    }

    class AppsSetConfSettings<T : appsSetConf>(val appsSet: T) {
        fun initTools(projectDir: JvmFile) = with(appsSet) {
            _tools = lib.tools(AppsPlace.from(projectDir), AppsSetPlace.from(projectDir, name.ifEmpty { this.javaClass.simpleName }, mapOfProjects))
        }

        fun configure(projectDir: JvmFile) {
            initTools(projectDir)
            appsSet._tools.disable_build_gradle()
        }

        fun configure(projectDir: JvmFile, block: T.() -> Unit) = if (configureFull(projectDir, block)) with(appsSet._tools) {
            build_gradle_of_sub_projects()
            build_gradle()
            settings_gradle()
        } else Unit

        fun configureFull(projectDir: JvmFile, block: T.() -> Unit) = with(appsSet) {
            initTools(projectDir)
            block()//@formatter:off
            if (mapOfProjects.isEmpty()) { _tools.disable_build_gradle(); false } else true
        }          //@formatter:on
    }

    abstract class appsSetConf(val name: String = "") : AppsSetConf() {
        @Suppress("PropertyName")
        lateinit var _tools: lib.tools

        @Suppress("PropertyName")
        val Settings get() = AppsSetConfSettingsDelegate()
        abstract val settings: AppsSetConfSettings<*>

        fun localBuildDir() = _tools.localBuildDir()
        fun active(vararg list: SubProject) = _tools.active(*list)
    }

    object root : appsSetConf("appsSetQws") {
        override val settings by Settings

        object apps {
            val appA by projectConf { implementation(libs.libQws, libs.libQwsEmptyImpl) }
            val appA_run1 by project<Main> { implementation(appA, libs.libQws) }
            val appA_run2 by project<Main> { implementation(appA, libs.libQws) }
            val appB by project<ConfFresh> { implementation(libs.libQws, libs.libQwsEmptyImpl, libs.LocalHostSocket) }
            val appC by projectConf(src_main_kotlin_and_java) {
                implementation(appA, libs.libQws, libs.LocalHostSocket, libs.SimpleReflect)
                implementation(libs.Util, libs.OutputPanel, libs.KtsListener, libs.OutputPanelSystemOut)
                implementation(libs.ScriptStr, libs.ScriptStrRunEnv)
                implementation(tools.RunScriptStr, tools.Config, tools.BuildDescConst)
                runtimeOnly(jar.kotlin_scripting_jsr223)
            }
            val appPlainIde by projectConf {
                implementation(tools.Config)
                implementation(libs.LocalHostSocket)
                implementation(libs.ScriptStr)
            }
        }

        object libs {
            val LocalHostSocket by projectConf
            val libQws by projectConf
            val libQwsEmptyImpl by projectConf { implementation(libQws) }
            val SimpleReflect by projectConf
            val ScriptStr by projectConf
            val ScriptStrRunEnv by projectConf
            val Util by projectConf
            val OutputPanel by projectConf
            val OutputPanelSystemOut by projectConf { implementation(OutputPanel) }
            val KtsListener by projectConf {
                implementation(Util, LocalHostSocket, OutputPanel)
            }
        }

        object tools {
            object script {
                val one by project<Script>()
            }

            object ide {
                val TypeAlias by projectConf
                val ActionRegister by projectConf(srcDirs = listOf("src_lib", "src_actions", "src_tool")) {
                    implementation(TypeAlias)
                }
                val Lib by projectConf {
                    implementation(libs.OutputPanel, TypeAlias)
                }

                val KtsListener by project<ConfModule>(MainClassToRun.byName) {
                    implementation(libs.Util)
                    implementation(libs.LocalHostSocket)
                    implementation(libs.SimpleReflect)
                    implementation(libs.ScriptStr, libs.ScriptStrRunEnv, RunScriptStr)
                    implementation(libs.OutputPanel, libs.KtsListener)
                    implementation(ide.Lib, ide.TypeAlias)
                    implementation(BuildDescConst)
                    implementation(Config)
                }

                object action {
                    val ideAction by project<IdeAction>(MainClassToRun.byName) { implementation(BuildDescConst) }
                }

                object script {
                    val three by project<IdeScript>()
                }
            }

            val BuildDescConst by projectConf
            val Config by projectConf
            val Module by projectConf
            val RunScriptStr by projectConf {
                implementation(Config, BuildDescConst, Module, libs.LocalHostSocket, libs.Util)
                implementation(libs.ScriptStr, libs.ScriptStrRunEnv)
            }
        }
    }

    class app(name: String) : appsSetConf(name) {
        override val settings by Settings
        val cli by project<CliApp>()
    }

    object app_cliA : appsSetConf() {
        override val settings by Settings
        val cliA by project<CliApp> { implementation(jar.xodus) }
    }

    object app_cliAB : appsSetConf() {
        override val settings by Settings
        val libA by projectConf
        val appA by project<CliApp> { implementation(libA, jar.xodus) }
        val cliB by project<CliApp> { implementation(libA, jar.sql_delight) }
    }

    object jar {
        @Suppress("SpellCheckingInspection")
        const val sql_delight = "com.squareup.sqldelight:sqlite-driver:1.5.3"
        const val kotlin_scripting_jsr223 = "org.jetbrains.kotlin:kotlin-scripting-jsr223"
        const val xodus = "org.jetbrains.xodus:dnq:1.4.480"
    }

    object tempApp : appsSetConf() {
        override val settings by Settings
        val tempA by project<CliApp> { implementation(jar.sql_delight) }
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
            root.tools.ide.KtsListener,
            root.tools.ide.action.ideAction,
            root.tools.script.one,
            root.tools.ide.script.three,
        )
        _tools.build_gradle_of_sub_projects()
        _tools.settings_gradle()
        _tools.buildGradle(
            """
import org.jetbrains.gradle.ext.*
import org.jetbrains.gradle.ext.ActionDelegationConfig.TestRunner.CHOOSE_PER_TEST
import org.jetbrains.gradle.ext.EncodingConfiguration.BomPolicy
""", listOf(""" id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.5" """), """
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

    class AllInit(scriptFile: JvmFile, val placeDir: JvmFile = scriptFile.up.up.up, writeChangesMode: Boolean, block: AllInit.() -> Unit) {
        init {
            runtime = if (writeChangesMode) Runtime.writable(JvmFile(scriptFile.up.up, "src_init")) else Runtime.empty
            this.block()
        }

        @Suppress("UNCHECKED_CAST")
        infix fun <T : appsSetConf> T.configure(block: T.() -> Unit) = this.settings.configure(placeDir, block as appsSetConf.() -> Unit)
        val String.configure get() = app(this).settings.configure(placeDir) { active(cli) }
        val allUnusedGradleUnlink get() = lib.tools.allUnusedGradleUnlink(placeDir)
        val allUsedGradleLinkToIde get() = lib.tools.allUsedGradleLinkToIde(placeDir)
    }

    private fun allInit(scriptFile: JvmFile, write: Boolean) = AllInit(scriptFile, writeChangesMode = write) {
        appsSetQwsInit(placeDir)
        "app_cli".configure
        app_cliA configure { active(cliA) }
        app_cliAB configure { active(appA, cliB) }

        //tempApp configure { localBuildDir(); active(tempA) }

        allUnusedGradleUnlink
        allUsedGradleLinkToIde
    }

    fun onAppInit(absolutePath: String, writeChangesMode: Boolean = true): AllInit {
        val scriptFile = JvmFile(absolutePath).absoluteFile
        println(" BuildDesc scriptFile=$scriptFile")
        val init = allInit(scriptFile, writeChangesMode)
        println(" BuildDesc placeDir=${init.placeDir} configured=${BuildDesc.libTools.mapConfigured.keys}")
        return init
    }
}

