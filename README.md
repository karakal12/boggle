<h1 align=center>מבוא</h1>

## רקע

## מחקר

## ניהול נתונים בפרוייקט

<h1 align=center>מבנה / ארכיטקטורה</h1>

## קבצי הפרוייקט

## מסכי הפרוייקט

## תרשים זרימת מסכים

<h1 align=center>מימוש הפרוייקט</h1>

## קבצי gradle, libs.versions.toml, וmanifest

gradle:
[] הסבר על gradle
רמת אפליקציה:
``` gradle
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
```
רמת פרוייקט:
``` gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}
```

[] הסבר על toml
``` toml
[versions]
agp = "9.0.1"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.7.1"
material = "1.13.0"
activity = "1.12.2"
constraintlayout = "2.2.1"
firebaseBom = "34.9.0"
annotation = "1.6.0"
lifecycleLivedataKtx = "2.6.1"
lifecycleViewmodelKtx = "2.6.1"
kotlin = "2.2.0"
coreKtx = "1.17.0"
firebaseMessaging = "25.0.1"
firebaseFunctions = "22.1.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-database = { group = "com.google.firebase", name = "firebase-database" }
annotation = { group = "androidx.annotation", name = "annotation", version.ref = "annotation" }
lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycleLivedataKtx" }
lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleViewmodelKtx" }
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging", version.ref = "firebaseMessaging" }
firebase-functions = { group = "com.google.firebase", name = "firebase-functions", version.ref = "firebaseFunctions" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

[] הסבר על manifest
``` xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.INTERNET"/>

    <application
        android:name=".BoggleApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="false"
        android:theme="@style/Theme.Boggle">
        <activity
            android:name=".ui.game.singleplayer.SingleplayerActivity"
            android:exported="false" />
        <activity android:name=".ui.game.multiplayer.MultiplayerActivity"
            android:exported="false"/>
        <activity
            android:name=".ui.mainmenu.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".ui.mainmenu.FriendListActivity"
            android:exported="false"
            android:label="Friends List" />
        <activity
            android:name=".ui.DonutActivity"
            android:exported="true"/>
        <service android:name=".services.InvitationService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT"/>
            </intent-filter>
            <meta-data
                android:name="com.google.firebase.messaging.default_notification_icon"
                android:resource="@mipmap/ic_launcher" />
            <meta-data
                android:name="com.google.firebase.messaging.default_notification_color"
                android:resource="@color/primary"/>
        </service>
    </application><?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.INTERNET"/>

    <application
        android:name=".BoggleApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="false"
        android:theme="@style/Theme.Boggle">
        <activity
            android:name=".ui.game.singleplayer.SingleplayerActivity"
            android:exported="false" />
        <activity android:name=".ui.game.multiplayer.MultiplayerActivity"
            android:exported="false"/>
        <activity
            android:name=".ui.mainmenu.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".ui.mainmenu.FriendListActivity"
            android:exported="false"
            android:label="Friends List" />
        <activity
            android:name=".ui.DonutActivity"
            android:exported="true"/>
        <service android:name=".services.InvitationService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT"/>
            </intent-filter>
            <meta-data
                android:name="com.google.firebase.messaging.default_notification_icon"
                android:resource="@mipmap/ic_launcher" />
            <meta-data
                android:name="com.google.firebase.messaging.default_notification_color"
                android:resource="@color/primary"/>
        </service>
    </application>

</manifest>

</manifest>
```
## תיאור מחלקות UML

## בסיס נתונים

## פונקציות שרת

## מחלקות הפרוייקט

### חבילה: data

[] הסבר

#### `abstract class Trie<T extends Trie<T>`
תפקיד המחלקה: מחלקת בסיס לעץ תחיליות ששומר על עצמו מתהליכונים שפועלים במקביל. המחלקה היא ג'נרית רקורסיבית כדי שהמחלקות שממשות אותם לא יצטרכו לעשות את העבודה הקשה.

שדות המחלקה:
```java
/** The number of letters in the English alphabet ('a' through 'z'). */
protected static final int ALPHABET_SIZE = 26;

/** Atomic array of pointers to child nodes, indexed by character ('a' to 'z'). */
protected final AtomicReferenceArray<T> children = new AtomicReferenceArray<>(ALPHABET_SIZE);

/** Flag indicating if this node represents the end of a complete word. */
protected volatile boolean isEndOfWord;

/** Flag indicating if this node has no children. */
protected volatile boolean isLeaf;


/** Atomic integer to track the number of words stored in the subtree rooted at this node. */
protected final AtomicInteger size = new AtomicInteger(0);
```
תכונות המחלקה:
``` java
- isEndOfWord - from IsEndOfWord()
- isLeaf - from IsLeaf()
- words - from getWords()
- string representation - from toString()
```

פעולות המחלקה:
