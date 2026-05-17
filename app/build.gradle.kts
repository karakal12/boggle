plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.compose)
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        tasks.register<Javadoc>("generateJavadoc") {
            group = "documentation"
            description = "Generates Javadoc for the debug variant."

            // Use the new variant API to get sources and classpath
            val javaSources = variant.sources.java?.all
            if (javaSources != null) {
                source(javaSources)
            }
            
            classpath = variant.compileClasspath + files(sdkComponents.bootClasspath.get())

            // We change the destination to a non-ignored folder so it can be committed to GitHub.
            destinationDir = file("${project.rootDir}/docs")

            options {
                (this as StandardJavadocDocletOptions).apply {
                    encoding = "UTF-8"
                    // Link to online Android documentation.
                    // Added a trailing slash to ensure Javadoc tool resolves it correctly.
                    links("https://developer.android.com/reference/")
                    
                    // Silence linting errors that often break Javadoc on Android
                    addStringOption("Xdoclint:none", "-quiet")
                }
            }

            // Exclude internal/generated classes from the final documentation output
            exclude("**/R.java", "**/BuildConfig.java", "**/databinding/**", "**/BR.java")
            
            // Javadoc often encounters errors with Android's complex dependency graph; 
            // we set this to false to allow the task to complete even with minor resolution warnings.
            isFailOnError = false
        }
    }
}
