plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.amibar.boggle"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
            res.srcDirs(
                "src/main/res",
                "src/main/res-features/auth",
                "src/main/res-features/mainmenu",
                "src/main/res-features/game-single",
                "src/main/res-features/game-multi",
                "src/main/res-features/shared"
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.core.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.functions)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

afterEvaluate {
    tasks.register<Javadoc>("generateJavadoc") {
        group = "documentation"
        description = "Generates Javadoc for the debug variant."

        val debugVariant = android.applicationVariants.find { it.name == "debug" }
        if (debugVariant != null) {
            val javaCompile = debugVariant.javaCompileProvider.get()
            
            // Source files from the variant (includes manual and some generated sources)
            source = javaCompile.source
            
            // Classpath must include:
            // 1. All dependencies (javaCompile.classpath)
            // 2. Android SDK (android.bootClasspath)
            // 3. Compiled classes of the module (javaCompile.destinationDir) 
            //    This is crucial for Javadoc to resolve symbols from generated classes.
            classpath = javaCompile.classpath + 
                        files(android.bootClasspath) + 
                        files(javaCompile.destinationDirectory)
            
            // Ensure the project is compiled so all generated classes are available
            dependsOn(javaCompile)
        }

        // We change the destination to a non-ignored folder so it can be committed to GitHub.
        destinationDir = file("${project.rootDir}/docs")

        options {
            (this as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                // Link to online Android documentation.
                // Added a trailing slash to ensure Javadoc tool resolves it correctly.
                links("https://developer.android.com/reference/")
                
                // Removed the problematic Firebase link as it lacks a valid package-list/element-list 
                // at the expected location, which was causing the FileNotFoundException.

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
