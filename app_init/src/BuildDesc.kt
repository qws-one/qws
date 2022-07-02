//

@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused", "LocalVariableName", "FunctionName")
object BuildDesc {
    private val java.io.File.up: java.io.File get() = absoluteFile.parentFile

    object lib {
        @Suppress("NOTHING_TO_INLINE")
        inline fun file(baseFile: java.io.File, path: String) = file(java.io.File(baseFile, path))

        @Suppress("NOTHING_TO_INLINE")
        inline fun file(file: java.io.File): java.io.File = file.absoluteFile

        private fun java.io.File.update(text: String) = if (!exists() || readText() != text) {
            parentFile.mkdirs()
            writeText(text)
            println("update: $absolutePath")
        } else Unit

        private fun java.io.File.create(getText: () -> String) = if (!exists()) {
            parentFile.mkdirs()
            writeText(getText())
            println("create: $absolutePath")
        } else Unit

        val map = mutableMapOf<String, Project>()

        class tools(val placeDir: java.io.File) {

            fun Project.srcDirs() = when (conf) {
                is Main -> listOf(*conf.srcDirs.toTypedArray(), Main.srcDir)
                else -> conf.srcDirs
            }

            fun settings_gradle() = file(placeDir, "settings.gradle.kts").update(
                map.keys.sorted().joinToString("\n") { "include(\"$it\")" })

            fun build_gradle() = map.values.forEach { prj ->
                prj.srcDirs().forEach {
                    java.io.File(placeDir, "${prj.path()}/$it").absoluteFile.mkdirs()
                }
                file(placeDir, "${prj.path()}/build.gradle.kts").update(
                    """
//
plugins {
    kotlin("jvm")
}
${
                        if (prj.conf.dependencies.isNotEmpty()) {
                            var str = "//\ndependencies {\n"
                            prj.conf.dependencies.forEach {
                                str += "    $it\n"
                            }
                            "$str}"
                        } else ""
                    }
${
                        when {
                            prj.conf is ConfFresh || prj.conf.srcDirs == src_main_kotlin_and_java -> ""
                            else -> """
//
sourceSets.main { java.srcDirs(${prj.srcDirs().joinToString { """"$it"""" }}) }
        """.trim()
                        }
                    }          """.trim()
                )
            }

            fun active(vararg list: Project) = list.forEach { prj ->
                when (prj.conf) {
                    is IdeAction -> objectIdeAction(prj)
                    is IdeScript -> objectIdeScript(prj)
                    is Script -> objectScript(prj)
                    is Main -> objectMain(prj)
                    else -> {}
                }
                dependenciesSrcTxt(prj)
            }

            private fun dependenciesSrcTxt(prj: Project) {
                file(placeDir, "${prj.path()}/dependencies_src.txt").update(prj.dependenciesSrcList().joinToString("\n"))
            }

            private fun objectIdeAction(prj: Project) {
                objectIdeScript(prj)
            }

            private fun objectIdeScript(prj: Project) {
                prj.conf.implementation(root.tools.ide.TypeAlias)
                prj.conf.implementation(root.tools.ide.Lib)
                objectScript(prj)
            }

            private fun objectScript(prj: Project) {
                prj.conf.implementation(root.libs.Util)
                prj.conf.implementation(root.libs.LocalHostSocket)
                prj.conf.implementation(root.libs.SimpleScript)
                prj.conf.implementation(root.libs.SimpleReflect)
                prj.conf.implementation(root.tools.RunSimpleScript)
                prj.conf.implementation(root.tools.Config)
                objectMain(prj)
            }

            private fun moduleInfo(prj: Project) = "ModuleInfo".let { name ->
                prj.conf.implementation(root.tools.Module)
                file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").update(
                    """
object $name : ModuleUtil.Info {
    override val name = "${prj.name}"
    override val srcDirsCsv = "${prj.srcDirs().joinToString(",")}"
    override val relativePath = "${prj.moduleRelativePath()}"
    override val dependenciesSrcCsv = "${prj.dependenciesSrcList().joinToString(",")}"
}
                        """.trim()
                )
            }

            private fun objectMain(prj: Project) = "Main".let { name ->
                moduleInfo(prj)
                file(placeDir, "${prj.pathOfKotlinSrcDir()}/$name.kt").create {
                    """
object $name {

    @JvmStatic
    fun main(args: Array<String>) {
        println("${'$'}{this::class.simpleName} args.size=${'$'}{args.size} args=${'$'}{args.toList()}")
    }
}
                """.trim()
                }
            }
        }

        class ProjectDelegate(val conf: Conf, val block: Conf.() -> Unit) {
            operator fun getValue(thisRef: Any, property: kotlin.reflect.KProperty<*>): Project {
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
                val prj = Project(id, path, property.name, conf)
                map[id] = prj
                return prj
            }
        }

        inline fun <reified T : Conf> project(noinline block: T.() -> Unit): ProjectDelegate {
            val conf = when (T::class) {
                ConfFresh::class -> ConfFresh()
                ConfPlane::class -> ConfPlane()
                IdeAction::class -> IdeAction()
                IdeScript::class -> IdeScript()
                Script::class -> Script()
                Main::class -> Main()
                else -> TODO("... for BuildDesc")
            }
            @Suppress("UNCHECKED_CAST")
            return ProjectDelegate(conf, block as Conf.() -> Unit)
        }
    }

    class Project(val id: String, val path: List<String>, val name: String, val conf: Conf) {
        val slash = "/"
        fun path() = "${slash}${path.joinToString(slash)}$slash$name$slash"
        fun pathOfKotlinSrcDir() = "${slash}${path.joinToString(slash)}$slash$name$slash${conf.srcDirs[0]}"
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

    sealed class Conf(val srcDirs: List<String>) {
        private val _dependencies = mutableSetOf<String>()
        private val _runtimeOnly = mutableSetOf<String>()
        private val _dependenciesSrc = mutableSetOf<Project>()
        val dependencies get() = _dependencies.sorted().plus(_runtimeOnly.sorted())
        val dependenciesSrc get() = _dependenciesSrc.sortedBy { it.id }
        fun runtimeOnly(libNotation: String) = _dependencies.add("""runtimeOnly("$libNotation")""")
        fun implementation(libNotation: String) = libNotation.forEach { _dependencies.add("""implementation("$it")""") }
        fun implementation(vararg project: Project) = project.forEach {
            _dependenciesSrc.add(it)
            _dependencies.add("""implementation(project("${it.id}"))""")
        }
    }

    const val src_main_java = "src/main/java"
    const val src_main_kotlin = "src/main/kotlin"
    val src_folder = listOf("src")
    val src_main_kotlin_and_java = listOf(src_main_kotlin, src_main_java)

    class ConfFresh : Conf(listOf(src_main_kotlin))
    class ConfC(srcDirs: List<String>) : Conf(srcDirs)
    open class ConfPlane : Conf(src_folder)
    open class Script : Main()
    open class IdeScript : Script()
    class IdeAction : IdeScript()
    open class Main : ConfPlane() {
        companion object {
            const val srcDir = "main"
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun project(srcDirs: List<String>, block: ConfC.() -> Unit) = lib.ProjectDelegate(ConfC(srcDirs), block as Conf.() -> Unit)
    inline fun <reified T : Conf> project(noinline block: T.() -> Unit) = lib.project(block)
    val project get() = lib.ProjectDelegate(ConfPlane()) {}

    object jar {
        const val kotlin_scripting_jsr223 = "org.jetbrains.kotlin:kotlin-scripting-jsr223"
    }

    object root {
        val app_init by project

        object apps {
            val appA by project<ConfPlane> { implementation(libs.libQws, libs.libQwsEmptyImpl) }
            val appA_run1 by project<Main> { implementation(appA, libs.libQws) }
            val appA_run2 by project<Main> { implementation(appA, libs.libQws) }
            val appB by project<ConfFresh> { implementation(libs.libQws, libs.libQwsEmptyImpl, libs.LocalHostSocket) }
            val appC by project(src_main_kotlin_and_java) {
                implementation(appA, libs.libQws, libs.LocalHostSocket, libs.SimpleReflect)
                runtimeOnly(jar.kotlin_scripting_jsr223)
            }
            val appPlainIde by project<ConfPlane> {
                implementation(tools.Config)
                implementation(tools.toolPlainIdeListener)
                implementation(libs.LocalHostSocket)
                implementation(libs.SimpleScript)
            }
        }

        object libs {
            val LocalHostSocket by project
            val libQws by project
            val libQwsEmptyImpl by project<ConfPlane> { implementation(libQws) }
            val SimpleReflect by project
            val SimpleScript by project
            val Util by project
        }

        object tools {
            object script {
                val one by project<Script> {}
            }

            object ide {
                val TypeAlias by project
                val ActionRegister by project(listOf("lib", "src_actions", "tool")) {
                    implementation(TypeAlias)
                }
                val Lib by project<ConfPlane> {
                    implementation(TypeAlias)
                }

                object action {
                    val KtsListener by project<IdeAction> {}
                }

                object script {
                    val two by project<IdeScript> {}
                }
            }

            val Config by project
            val Module by project
            val RunSimpleScript by project<ConfPlane> {
                implementation(Config, Module, libs.LocalHostSocket, libs.Util)
            }
            val toolPlainIdeListener by project<ConfPlane> {
                implementation(Config, libs.LocalHostSocket)
            }
            val toolPlainTypeAliasIdeListener by project<ConfPlane> {
                implementation(Config, ide.TypeAlias, libs.LocalHostSocket)
            }
            val toolSimpleScriptIdeListener by project<ConfPlane> {
                implementation(Config, libs.LocalHostSocket)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(args.toList())
        val scriptFile = java.io.File(args[0]).absoluteFile
        println(scriptFile)

        @Suppress("UNUSED_VARIABLE") val BuildDesc = root.app_init

        with(lib.tools(scriptFile.up.up.up)) {
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
            build_gradle()
            settings_gradle()
        }
    }
}
