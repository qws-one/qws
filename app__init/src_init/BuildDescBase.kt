typealias LocalFile = java.io.File


@Suppress("ClassName", "MemberVisibilityCanBePrivate", "FunctionName", "unused")
abstract class BuildDescBase {
    companion object : LocalFs.Is by LocalFs, BuildDescConst by BuildDescConst, BuildDescConstInternal by BuildDescConstInternal

    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    class LocalFs {
        interface Is {
            fun file(baseFile: LocalFile, path: String): LocalFile
            val emptyFile: LocalFile
            fun file(file: LocalFile): LocalFile = file.absoluteFile
            val LocalFile.listFiles: Array<LocalFile>
            val LocalFile.up: LocalFile
            fun LocalFile.forEachDir(action: (LocalFile) -> Unit)
        }

        companion object : Is {
            override inline fun file(baseFile: LocalFile, path: String) = file(LocalFile(baseFile, path))
            override val emptyFile = LocalFile("")
            override inline val LocalFile.up get() = absoluteFile.parentFile!!
            override inline val LocalFile.listFiles: Array<LocalFile> get() = listFiles() ?: emptyArray()
            override inline fun LocalFile.forEachDir(action: (LocalFile) -> Unit) = listFiles()?.forEach { if (it.isDirectory) action(it) } ?: Unit
        }
    }

    class AppsPlace(val dir: LocalFile, val name: String, val gradleBuildPlaceConfTxt: LocalFile = emptyFile) {
        companion object {
            fun from(place: LocalFile) =
                AppsPlace(place, place.name, file(place, gradle_build_place_conf_txt).run { if (exists()) absoluteFile else emptyFile })
        }
    }

    class AppsSetPlace(val placeDir: LocalFile, val name: String, val mapOfProjects: Map<String, SubProject>) {
        companion object {
            fun from(place: LocalFile, name: String, map: Map<String, SubProject>) = AppsSetPlace(file(place, name), name, map)
        }
    }

    object lib {
        @Suppress("DEPRECATION")
        private fun String.capitalize() = get(0).toUpperCase() + substring(1)
        val mapConfigured = mutableMapOf<String, AppsSetPlace>()
        val refOfObjLib = lib
        lateinit var refOfLastTools: tools
        infix fun LocalFile.update(text: String): Unit = this.let { with(refOfLastTools) { it.update(text) } }

        fun allUsedGradleLinkToIde(place: LocalFile) = file(place, ".idea/gradle.xml") update """<?xml version="1.0" encoding="UTF-8"?>
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

        fun allUnusedGradleUnlink(place: LocalFile) = place.forEachDir { dir ->
            if (!mapConfigured.containsKey(dir.name) && file(dir, build_gradle_root_kts).exists()) unusedGradleUnlink(dir)
        }

        fun unusedGradleUnlink(dir: LocalFile) = with(refOfLastTools) {
            file(dir, build_gradle_root_kts).deleteIfExist
            file(dir, settings_gradle_kts).deleteIfExist
            file(dir, all_build_place_txt).deleteIfExist
            file(dir, _gradle_dir).deleteIfExist
        }

        class tools(
            val jar: BuildDesc.jar,
            val appsPlace: AppsPlace,
            val appsSet: AppsSetPlace,
            fsAtRuntime: FsAtRuntime
        ) : FsAtRuntime by fsAtRuntime {
            init {
                refOfLastTools = this
            }

            val map = appsSet.mapOfProjects
            val placeDir = appsSet.placeDir.also { mapConfigured[appsSet.name] = appsSet }

            //val SubProject.place get() = file(placeDir, path())
            fun SubProject.place(path: String) = file(placeDir, "${path()}/$path")
            val SubProject.relativePlace get() = "${placeDir.name}/${path()}".replace("//", "/").replace("//", "/")
            fun SubProject.relativePlace(path: String) = "${relativePlace}/$path".replace("//", "/")


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
                if (prj.conf is CliApp || prj.mainUnit.validToRun) {
                    //https://docs.gradle.org/current/userguide/application_plugin.html
                    //gradle run --args="foo --bar" (see JavaExec.setArgsString(java.lang.String).
                    plugins = plugins.plus(listOf("application"))
                    applicationConf = """application {
    mainClass.set("${prj.mainUnit.name.ifEmpty { prj.name }.capitalize()}")
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
                if (prj.mainUnit.validPlain) file(placeDir, "${prj.pathOfKotlinSrcDir()}/${prj.mainUnit.name}.kt") create { "object ${prj.mainUnit.name} {\n}" }
                if (prj.name == BuildDescConst::class.simpleName) "${prj.name}.kt".let {
                    file(placeDir, "${prj.pathOfKotlinSrcDir()}/$it") update file(src_init, it).run {
                        if (exists()) readText() + "\n// this is copy, do not edit it" else /*writeChangesMode == false*/ ""
                    }
                }
            }

//            private fun objectCliApp2(prj: SubProject, name: String) = file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt") create {
//                """//
//object $name {
//
//    @JvmStatic
//    fun main(args: Array<String>) {
//        println("${'$'}{this::class.simpleName} args.size=${'$'}{args.size} args=${'$'}{args.toList()}")
//    }
//}"""
//            }

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

            fun addConst(to: SubProject, vararg list: SubProject) {
                addBuildDescGen(to, list)
                val inMap = mutableMapOf<String, String>()
                list.forEach { prj ->
//                    inMap["idOf${prj.name}"] = "\"${prj.id}\""
//                    inMap["nameOf${prj.name}"] = "\"${prj.name}\""
                    inMap["srcDirOf${prj.name}"] = "\"${prj.pathOfKotlinSrcDir()}\""
                }
                "BuildDescConstGen".let { file(placeDir, "${to.pathOfKotlinSrcDir()}/$it.kt") update interfaceFrom(it, inMap) }
            }

            fun interfaceFrom(interfaceName: String, inMap: Map<String, String>): String {
                var interfaceContent = ""
                var companionContent = ""
                for ((name, value) in inMap) {
                    val type = "String"
                    companionContent += "        override val $name = $value\n"
                    interfaceContent += "    val $name: $type\n"
                }
                return """interface $interfaceName {
    companion object : $interfaceName {
$companionContent    }

$interfaceContent}"""
            }

            fun addBuildDescGen(to: SubProject, list: Array<out SubProject>) {
                var objects = ""
                var objectsAsVal = ""
                list.forEach { prj ->
                    val objectValues = mutableListOf<String>()
                    objectsAsVal += "        val ${prj.name} = Companion.${prj.name}\n"
                    objectValues += "            const val name = \"${prj.name}\""
                    objectValues += "            const val srcDir = \"${prj.pathOfKotlinSrcDir()}\""
                    for ((key, value) in prj.mainUnit.constMap) {
                        objectValues += "            const val ${key.trim()} = \"$value\""
                    }
                    objects += """
        object ${prj.name} {
${objectValues.joinToString("\n")}
        }
"""
                }
                val genName = "BuildDescGen"; file(placeDir, "${to.pathOfKotlinSrcDir()}/$genName.kt") update """interface $genName {
    @Suppress("PropertyName")
    class DescUnit {
$objectsAsVal    }

    companion object : $genName {$objects
        override val descUnit = DescUnit()
    }

    val descUnit: DescUnit
}"""
            }

            private fun objectIdeAction(prj: SubProject) = objectIdeScript(prj)

            private fun objectIdeScript(prj: SubProject) {
                prj.conf.implementation(*BuildDesc.objectIdeScriptDependencies)
                objectScript(prj)
            }

            private fun objectScript(prj: SubProject) {
                prj.conf.implementation(*BuildDesc.objectScriptDependencies)
                objectMain(prj)
            }

            private fun moduleInfo(prj: SubProject) {
                prj.conf.implementation(BuildDesc.rootToolsModule)
                //file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").apply { if (exists()) delete() }
                file(placeDir, "${prj.pathOfModuleInfoSrcDir()}/$ModuleInfo.kt") update """object $ModuleInfo : ModuleUtil.Info {
    override val appsSetName = "${placeDir.name}"
    override val name = "${prj.name}"
    override val srcDirsCsv = "${prj.srcDirs().joinToString(",")}"
    override val relativePath = "${prj.moduleRelativePath()}"
    override val dependenciesSrcCsv = "${prj.dependenciesSrcList().joinToString(",")}"
}"""
                if (prj.mainUnit.validToRun) objectCliApp(prj, prj.mainUnit.name)
            }

            private fun objectMain(prj: SubProject) {
                moduleInfo(prj)
                if (prj.mainUnit.isEmpty) objectCliApp(prj, "Main")
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

        class SubProjectDelegate(val map: MutableMap<String, SubProject>, val mainUnit: MainUnit, val conf: Conf, val block: Conf.() -> Unit) {
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
                val mainUnit = if (mainUnit.needName) mainUnit.updateName(property.name.capitalize()) else mainUnit
                val prj = SubProject(id, mainUnit, path, property.name, conf)
                map[id] = prj
                return prj
            }
        }
    }

    @Suppress("PropertyName")
    interface FsAtRuntime {
        val src_init: LocalFile
        val LocalFile.deleteIfExist: Boolean
        infix fun LocalFile.update(text: String)
        infix fun LocalFile.create(getText: () -> String)

        companion object {
            val empty = object : FsAtRuntime {
                override val src_init = emptyFile
                override val LocalFile.deleteIfExist: Boolean get() = false
                override fun LocalFile.update(text: String) {}
                override fun LocalFile.create(getText: () -> String) {}
            }

            fun writable(srcInit: LocalFile) = object : FsAtRuntime {
                override val src_init = srcInit
                override val LocalFile.deleteIfExist: Boolean
                    get() {
                        if (exists()) {
                            if (isDirectory) deleteRecursively()
                            else delete()
                            println("remove: $absolutePath")
                            return true
                        }
                        return false
                    }

                override infix fun LocalFile.update(text: String) = if (!exists()) create { text } else if (readText() != text) {
                    parentFile.mkdirs()
                    writeText(text)
                    println("update: $absolutePath")
                } else Unit

                override fun LocalFile.create(getText: () -> String) = if (!exists()) {
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

    class SubProject(val id: String, val mainUnit: MainUnit, val path: List<String>, val name: String, val conf: Conf) {
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

    class MainUnit(val name: String, val constMap: Map<String, String>, val type: Type, val needName: Boolean) {
        enum class Type { Plain, ToRun }
        companion object {
            val empty = MainUnit("", emptyMap(), Type.Plain, false)
            val toRun = MainUnit("", emptyMap(), Type.ToRun, true)
            val plain = MainUnit("", emptyMap(), Type.Plain, true)
            fun plain(vararg const: Pair<String, String>) = MainUnit("", const.toMap(LinkedHashMap()), Type.Plain, true)
            fun plain(needName: Boolean = true, vararg const: Pair<String, String>) = MainUnit("", const.toMap(LinkedHashMap()), Type.Plain, needName)
            fun plain(name: String, vararg const: Pair<String, String>) = MainUnit(name.trim(), const.toMap(LinkedHashMap()), Type.Plain, false)
        }

        val isEmpty get() = empty == this
        val validToRun get() = !isEmpty && type == Type.ToRun
        val validPlain get() = !isEmpty && type == Type.Plain && name.isNotEmpty()
        fun updateName(name: String) = MainUnit(name, constMap, type, needName)
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
        val projectConf get() = lib.SubProjectDelegate(mapOfProjects, MainUnit.empty, ConfPlane()) {}
        inline fun projectConf(vararg srcDir: String) = lib.SubProjectDelegate(mapOfProjects, MainUnit.empty, ConfC(srcDir.toList())) {}
        inline fun projectConf(noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, MainUnit.empty, ConfPlane(), block) //@formatter:off
        inline fun projectConf(mainUnit: MainUnit) = lib.SubProjectDelegate(mapOfProjects, mainUnit, ConfPlane()){}
        inline fun projectConf(mainUnit: MainUnit, noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, mainUnit, ConfPlane(), block)
        inline fun projectConf(srcDirs: List<String>, noinline block: Conf.() -> Unit) = lib.SubProjectDelegate(mapOfProjects, MainUnit.empty, ConfC(srcDirs), block)
        inline fun <reified T : Conf> project() = project<T> {} //@formatter:on
        inline fun <reified T : Conf> project(noinline block: T.() -> Unit) = project(MainUnit.empty, block)
        inline fun <reified T : Conf> project(mainUnit: MainUnit, noinline block: T.() -> Unit): lib.SubProjectDelegate {
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
            @Suppress("UNCHECKED_CAST") return lib.SubProjectDelegate(mapOfProjects, mainUnit, conf, block as Conf.() -> Unit)
        }
    }

    class AppsSetConfSettingsDelegate {
        operator fun <T : appsSetConf> getValue(thisRef: T, property: kotlin.reflect.KProperty<*>) = AppsSetConfSettings(thisRef)
    }

    class AppsSetConfSettings<T : appsSetConf>(val appsSet: T) {
        fun initTools(projectDir: LocalFile, fsAtRuntime: FsAtRuntime) = appsSet.let {
            it._tools = lib.tools(
                BuildDesc.jar,
                AppsPlace.from(projectDir),
                AppsSetPlace.from(projectDir, it.name.ifEmpty { it.javaClass.simpleName }, it.mapOfProjects),
                fsAtRuntime
            )
        }

        fun configure(projectDir: LocalFile, fsAtRuntime: FsAtRuntime) {
            initTools(projectDir, fsAtRuntime)
            appsSet._tools.disable_build_gradle()
        }

        fun configure(projectDir: LocalFile, fsAtRuntime: FsAtRuntime, block: T.() -> Unit) =
            if (configureFull(projectDir, fsAtRuntime, block)) with(appsSet._tools) {
                build_gradle_of_sub_projects()
                build_gradle()
                settings_gradle()
            } else Unit

        fun configureFull(projectDir: LocalFile, fsAtRuntime: FsAtRuntime, block: T.() -> Unit) = with(appsSet) {
            initTools(projectDir, fsAtRuntime)
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
        fun SubProject.constGen(vararg list: SubProject) = _tools.addConst(this, *list)
        //fun addConst(to: SubProject, vararg list: SubProject) = _tools.addConst(to, *list)
    }

    class app(name: String) : appsSetConf(name) {
        override val settings by Settings
        val cli by project<CliApp>()
    }

    class AllInit(scriptFile: LocalFile, val placeDir: LocalFile = scriptFile.up.up.up, writeChangesMode: Boolean, block: AllInit.() -> Unit) {
        val fsAtRuntime = if (writeChangesMode) FsAtRuntime.writable(LocalFile(scriptFile.up.up, "src_init")) else FsAtRuntime.empty

        init {
            this.block()
        }

        @Suppress("UNCHECKED_CAST")
        infix fun <T : appsSetConf> T.configure(block: T.() -> Unit) = this.settings.configure(placeDir, fsAtRuntime, block as appsSetConf.() -> Unit)
        val String.configure get() = app(this).settings.configure(placeDir, fsAtRuntime) { active(cli) }
        val allUnusedGradleUnlink get() = lib.allUnusedGradleUnlink(placeDir)
        val allUsedGradleLinkToIde get() = lib.allUsedGradleLinkToIde(placeDir)
    }
}