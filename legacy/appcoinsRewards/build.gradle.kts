plugins {
  id("appcoins.jvm.library")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))
  implementation(project(":legacy:bdsbilling"))

  implementation(libs.bundles.network)
  implementation(libs.kotlin.stdlib)
  testImplementation(libs.bundles.testing)
//  implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
//  implementation("com.squareup.retrofit2:adapter-rxjava2:$project.retrofit_version")
//  implementation("com.google.code.gson:gson:$project.gson_version")
//  implementation("com.squareup.retrofit2:converter-gson:$project.retrofit_version")
//  testImplementation("junit:junit:$project.junit_version")
//  testImplementation("org.mockito:mockito-core:$project.mockito_version")
}