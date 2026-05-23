plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
    alias(libs.plugins.androidx.navigation.safeargs)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.amibar.boggle"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.amibar.boggle"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            res.directories.clear()
            res.directories.addAll(
                listOf(
                    "src/main/res",
                    "src/main/res-features/auth",
                    "src/main/res-features/mainmenu",
                    "src/main/res-features/game-single",
                    "src/main/res-features/game-multi",
                    "src/main/res-features/shared"
                )
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.fragment.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.core.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.palette.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.serialization.json)
}

dokka {
    moduleName.set("Boggle")
    
    dokkaPublications.configureEach {
        outputDirectory.set(file("${project.rootDir}/docs"))
    }

    dokkaSourceSets.configureEach {
        // Link to online Android documentation
        enableAndroidDocumentationLink.set(true)

        // Exclude internal/generated classes from the final documentation output
        perPackageOption {
            matchingRegex.set(".*\\.databinding.*|.*\\.R.*|.*\\.BuildConfig.*|.*\\.BR.*")
            suppress.set(true)
        }
    }
}

// Alias for the user's requested task name
tasks.register("generateKDoc") {
    group = "documentation"
    description = "Generates KDoc documentation using Dokka."
    dependsOn("dokkaGenerate")
}

