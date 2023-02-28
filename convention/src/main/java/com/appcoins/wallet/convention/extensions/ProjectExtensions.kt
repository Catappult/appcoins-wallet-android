package com.appcoins.wallet.convention.extensions

import org.gradle.api.Project

val Project.fullName: String
    get() = mutableListOf(name).apply {
        var project: Project = this@fullName
        while (project.parent != null) {
            val parent = project.parent
            project = parent ?: break
            add(parent.name)
        }
    }.joinToString("-")