@Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused")
class BuildDesc : BuildDescBase() {

    object root : appsSetConf("appsSetQws") {
        override val settings by Settings

        object apps {
            val appA by projectConf { implementation(libs.libQws, libs.libQwsEmptyImpl) }
            val appA_run1 by project<Main> { implementation(appA, libs.libQws) }
            val appA_run2 by project<Main> { implementation(appA, libs.libQws) }
            val appB by project<ConfFresh> { implementation(libs.libQws, libs.libQwsEmptyImpl, libs.LocalHostSocket) }
            val appC by projectConf(src_main_kotlin_and_java) {
                implementation(appA, libs.libQws, libs.LocalHostSocket, libs.SimpleReflect)
                implementation(libs.Util, libs.logs.OutputPanel, libs.KtsListener, libs.logs.OutputPanelSystemOut)
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
            object logs {
                val LogSimple by projectConf
                val OutputPanel by projectConf
                val OutputPanelSystemOut by projectConf { implementation(OutputPanel) }
            }

            val BaseTypeAlias by projectConf(MainUnit.plain) // for 'BuildDescGen.descUnit.BaseTypeAlias.name + kt'
            val LocalHostSocket by projectConf { implementation(BaseTypeAlias, logs.LogSimple) }
            val libQws by projectConf
            val libQwsEmptyImpl by projectConf { implementation(libQws) }
            val SimpleReflect by projectConf
            val ScriptStr by projectConf
            val ScriptStrRunEnv by projectConf
            val Util by projectConf(src_main, "src_suit_one")
            val KtsListener by projectConf {
                implementation(Util, LocalHostSocket, logs.OutputPanel)
            }
        }

        object tools {
            object script {
                val one by project<Script>()
                val two by project<ConfModule> { implementation(libs.LocalHostSocket, libs.ScriptStr, RunScriptStr, Config) }
                val four by projectConf { implementation(libs.ScriptStr, RunScriptStr) }
            }

            object ide {
                val TypeAlias by projectConf(MainUnit.plain(needName = false, "ignoreSrcFileName" to "TypeAliasTransient.kt"))
                val ActionRegister by projectConf(MainUnit.toRun) {
                    implementation(action.all)
                    implementation(libs.Util)
                    implementation(libs.LocalHostSocket)
                    implementation(libs.SimpleReflect)
                    implementation(libs.ScriptStr, libs.ScriptStrRunEnv, RunScriptStr)
                    implementation(libs.logs.OutputPanel, libs.KtsListener)
                    implementation(Lib, TypeAlias)
                    implementation(BuildDescConst)
                    implementation(Config)
                }
                val Lib by projectConf {
                    implementation(libs.logs.OutputPanel, TypeAlias)
                }
                val KtsListener by project<ConfModule>(MainUnit toRun "IdeListener") {
                    implementation(libs.Util)
                    implementation(libs.LocalHostSocket)
                    implementation(libs.SimpleReflect)
                    implementation(libs.ScriptStr, libs.ScriptStrRunEnv, RunScriptStr)
                    implementation(libs.logs.OutputPanel, libs.KtsListener)
                    implementation(Lib, TypeAlias)
                    implementation(BuildDescConst)
                    implementation(Config)
                }

                object action {
                    val all by projectConf {
                        implementation(BuildDescConst, TypeAlias, Lib)
                        implementation(libs.Util, libs.ScriptStr, libs.logs.OutputPanel)
                    }
                    val ideAction by project<IdeAction>(MainUnit.toRun) { implementation(BuildDescConst) }
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

    object tempApp : appsSetConf() {
        override val settings by Settings
        val tempA by project<CliApp> { implementation(jar.sql_delight) }
    }

    object jar {
        @Suppress("SpellCheckingInspection")
        const val sql_delight = "com.squareup.sqldelight:sqlite-driver:1.5.3"
        const val kotlin_scripting_jsr223 = "org.jetbrains.kotlin:kotlin-scripting-jsr223"
        const val xodus = "org.jetbrains.xodus:dnq:1.4.480"
    }

    companion object {
        val rootToolsModule = root.tools.Module
        val objectIdeScriptDependencies = arrayOf(
            root.tools.ide.TypeAlias,
            root.tools.ide.Lib,
            root.libs.logs.OutputPanel,
        )
        val objectScriptDependencies = arrayOf(
            root.libs.Util,
            root.libs.LocalHostSocket,
            root.libs.SimpleReflect,
            root.libs.ScriptStr, root.libs.ScriptStrRunEnv,
            root.tools.RunScriptStr,
            root.tools.Config,
        )

        fun AllInit.appsSetQwsInit() = root.settings.configureFull(placeDir, fsAtRuntime) {
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
                root.tools.script.two,
                root.tools.script.four,
                root.tools.ide.script.three,
            )
            root.tools.BuildDescConst.constGen(
                root.libs.logs.LogSimple,
                root.libs.LocalHostSocket,
                root.libs.BaseTypeAlias,
                root.libs.ScriptStrRunEnv,
                root.tools.RunScriptStr,
                root.tools.ide.TypeAlias,
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

        private fun allInit(scriptFile: LocalFile, write: Boolean) = AllInit(scriptFile, writeChangesMode = write) {
            appsSetQwsInit()
            "app_cli".configure
            app_cliA configure { active(cliA) }
            app_cliAB configure { active(appA, cliB) }

            //tempApp configure { localBuildDir(); active(tempA) }

            allUnusedGradleUnlink
            allUsedGradleLinkToIde
        }

        fun onAppInit(absolutePath: String, writeChangesMode: Boolean = true): AllInit {
            val scriptFile = LocalFile(absolutePath).absoluteFile
            println(" BuildDesc scriptFile=$scriptFile")
            val init = allInit(scriptFile, writeChangesMode)
            println(" BuildDesc placeDir=${init.placeDir} configured=${lib.mapConfigured.keys}")
            return init
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println(" BuildDesc.main ${args.toList()}")
            onAppInit(LocalFile(args[0]).absolutePath)
        }
    }
}