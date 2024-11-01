plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.register<JavaExec>("runWithJavaExec") {
    mainClass = "com.mrboomdev.awery.desktop.Main"
    errorOutput = System.err
}

dependencies {
    // Utils
    implementation(project(":ext"))
    implementation(libs.moshi)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.dnsoverhttps)
    implementation(libs.okhttp.brotli)
}