package com.appcoins.wallet.convention.plugins

import com.appcoins.wallet.convention.extensions.fullName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.BasePluginExtension

class ConfigureArchivesNamePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(BasePlugin::class.java)
        extensions.configure(BasePluginExtension::class.java) {
            archivesName.set(fullName)
        }
    }

}

