package com.appcoins.wallet.convention.plugins

import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.get
import com.appcoins.wallet.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class JvmLibPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply(ConfigureArchivesNamePlugin::class.java)
                apply("kotlin")
            }
            extensions.configure(JavaPluginExtension::class.java) {
                sourceCompatibility = Config.jvm.javaVersion
                targetCompatibility = Config.jvm.javaVersion
            }

            tasks.withType(KotlinCompile::class.java) {
                kotlinOptions {
                    jvmTarget = Config.jvm.kotlinJvm
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-Xopt-in=kotlin.RequiresOptIn",
                        "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                        "-Xopt-in=kotlinx.coroutines.FlowPreview"
                    )
                }
            }

            dependencies.apply {
                add("compileOnly", libs["javaxInject"])
                add("implementation", libs["kotlin.stdlib"])
            }
        }
    }

}