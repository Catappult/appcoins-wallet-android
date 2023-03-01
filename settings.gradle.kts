pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

buildCache {
  local {
    removeUnusedEntriesAfterDays = 3
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

val (projects, modules) = rootDir.projectsAndModules()
println("Projects:\n\t- ${projects.sortedBy { it }.joinToString("\n\t- ") { it }}")
println("Modules:\n\t- ${modules.sortedBy { it }.joinToString("\n\t- ") { it }}")

for (project in projects) includeBuild(project)
for (module in modules) include(module)


fun File.projectsAndModules(): Pair<Set<String>, Set<String>> {
  val blacklist = setOf(
    ".git",
    ".gradle",
    ".idea",
    "buildSrc",
    "config",
    "build",
    "src",
    "molecule-viewmodel"
  )

  fun File.childrenDirectories() = listFiles { _, name -> name !in blacklist }
    ?.filter { it.isDirectory }
    .orEmpty()

  fun File.isProject() = File(this, "settings.gradle.kts").exists() ||
      File(this, "settings.gradle").exists()

  fun File.isModule() = !isProject() &&
      (File(this, "build.gradle.kts").exists() || File(this, "build.gradle").exists())


  val modules = mutableSetOf<String>()
  val projects = mutableSetOf<String>()

  fun File.find(name: String? = null, includeModules: Boolean = true): List<File> {
    return childrenDirectories().flatMap {
      val newName = (name ?: "") + it.name
      when {
        it.isProject() -> {
          projects += newName
          it.find("$newName:", includeModules = false)
        }
        it.isModule() && includeModules -> {
          modules += ":$newName"
          it.find("$newName:")
        }
        else -> it.find("$newName:")
      }
    }
  }

  find()

  // we need to replace here since some Projects have a Module as a parent folder
  val formattedProjects = projects.map { it.replace(":", "/") }.toSet()
  return Pair(formattedProjects, modules)
}