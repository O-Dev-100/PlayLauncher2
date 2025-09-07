plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
    id("androidx.room") version "2.6.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}