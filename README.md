<div align="right" dir="rtl">

# תוכן עניינים

* **[מבוא](#intro)**
  * [רקע](#רקע)
    * [שם הפרוייקט:](#שם-הפרוייקט)
    * [תיאור הפרוייקט:](#תיאור-הפרוייקט)
    * [קהל היעד:](#קהל-היעד)
    * [מטרות האפליקציה:](#מטרות-האפליקציה)
  * [מחקר וסקירת שוק](#מחקר-וסקירת-שוק)
  * [ניהול נתונים בפרוייקט](#ניהול-נתונים-בפרוייקט)
* **[מבנה / ארכיטקטורה](#architecture)**
  * [קבצי הפרוייקט](#קבצי-הפרוייקט)
  * [מסכי הפרוייקט](#מסכי-הפרוייקט)
  * [תרשים זרימת מסכים](#תרשים-זרימת-מסכים)
* **[מימוש הפרוייקט](#implementation)**
  * [קבצי gradle, libs.versions.toml, וmanifest](#קבצי-gradle-libsversionstoml-וmanifest)
  * [תיאור מחלקות UML](#תיאור-מחלקות-uml)
    * [פירוק לחלקים:](#פירוק-לחלקים)
  * [בסיס נתונים](#בסיס-נתונים)
  * [פונקציות שרת](#פונקציות-שרת)
  * [מחלקות הפרוייקט](#מחלקות-הפרוייקט)
    * [מחלקת אפליקציה](#מחלקת-אפליקציה)
      * [BoggleApplication](#boggleapplication)
    * [חבילה: data](#חבילה-data)
      * [Trie](#trie)
      * [Dictionary](#dictionary)
      * [PathTrie](#pathtrie)
      * [FirebaseHandler](#firebasehandler)
      * [GameMode](#gamemode)
      * [PlayerRole](#playerrole)
      * [User](#user)
    * [חבילה: engine](#חבילה-engine)
      * [BoggleGame](#bogglegame)
      * [GameSolver](#gamesolver)
        * [GameSolverTask](#gamesolvertask)
      * [DonutRenderer](#donutrenderer)
    * [חבילה: services](#חבילה-services)
      * [InvitationService](#invitationservice)
    * [חבילה: ui.mainmenu](#חבילה-uimainmenu)
      * [MainActivity](#mainactivity)
      * [LoginFragment](#loginfragment)
      * [SignUpFragment](#signupfragment)
      * [FriendListActivity](#friendlistactivity)
      * [FriendAdapter](#friendadapter)
    * [חבילה: ui.game.multiplayer](#חבילה-uigamemultiplayer)
      * [MultiplayerActivity](#multiplayeractivity)
      * [JoinOrCreateRoomFragment](#joinorcreateroomfragment)
      * [LobbyFragment](#lobbyfragment)
      * [MultiplayerGameFragment](#multiplayergamefragment)
      * [MultiplayerOnGameEndFragment](#multiplayerongameendfragment)
      * [PlayerAdapter](#playeradapter)
      * [PlayersWordsAdapter](#playerswordsadapter)
    * [חבילה: ui.game.singleplayer](#חבילה-uigamesingleplayer)
      * [SingleplayerActivity](#singleplayeractivity)
      * [SingleplayerOnGameEndFragment](#singleplayerongameendfragment)
    * [חבילה: ui.shared](#חבילה-uishared)
      * [WordsAdapter](#wordsadapter)
    * [חבילה: ui](#חבילה-ui)
      * [DonutActivity](#donutactivity)
    * [חבילה: views](#חבילה-views)
      * [BoggleView](#boggleview)
      * [SquareTextView](#squaretextview)
    * [חבילה: utils](#חבילה-utils)
      * [ImageUtils](#imageutils)
      * [Timer](#timer)
      * [PointAndDepth](#pointanddepth)
      * [Quad](#quad)

</div>


<h1 align=center id="intro">מבוא</h1>

## רקע

### שם הפרוייקט: 
Boggle (בוגל)

### תיאור הפרוייקט:
אפליקציית משחק מילים דיגיטלית המבוססת על משחק הקופסה הקלאסי "בוגל". המשחק מציג לוח בגודל 4x4 של קוביות עם אותיות באנגלית. על השחקן למצוא כמה שיותר מילים תקניות על ידי חיבור אותיות סמוכות (אופקית, אנכית ואלכסונית) בתוך מגבלת זמן של 3 דקות. האפליקציה תומכת בשני מצבי משחק:
* **שחקן יחיד:** אתגר אישי למציאת כמה שיותר מילים וקבלת ניקוד בהתאם לאורך המילים שניחשו.
* **מרובה משתתפים (Multiplayer):** משחק בזמן אמת מול חברים דרך רשת האינטרנט, המאפשר ליצור חדר משחק, לשלוח הזמנות לחברים דרך התראות, ולהתחרות מי מוצא הכי הרבה מילים ייחודיות.

### קהל היעד:
האפליקציה מיועדת לאוהבי משחקי מילים, חובבי פאזלים ואתגרי חשיבה, תלמידים, ולכל מי שמעוניין להעביר את הזמן בצורה חווייתית ומהנה תוך כדי תרגול ושיפור אוצר המילים שלו באנגלית. בנוסף, היא מיועדת לשחקנים שאוהבים אינטראקציה חברתית ותחרותיות מול חברים.

### מטרות האפליקציה:
* **בידור והנאה:** לספק חווית משחק אינטראקטיבית, חלקה ונוחה המדמה את המשחק המסורתי במכשיר הנייד.
* **אתגר חברתי:** לאפשר לחברים להתחבר ולשחק יחד מרחוק בקלות, כולל מערכת הזמנות והתראות.
* **למידה:** לשמש ככלי עזר לשיפור ואימון אוצר המילים והאיות בשפה האנגלית.
* **יכולות טכנולוגיות:** להדגים בניית אפליקציית אנדרואיד המשלבת אלגוריתמיקה מורכבת (עצי Trie, פותרן לוחות ב-DFS, רינדור 3D) יחד עם שירותי ענן ואותנטיקציה מתקדמים (Firebase Auth, Realtime Database, Cloud Messaging).

## מחקר וסקירת שוק

שוק משחקי המילים במובייל הוא אחד הפעילים והרווחיים ביותר בקטגוריית משחקי הקז'ואל (Casual Games). שחקנים מחפשים חוויות קצרות (סשנים של 3-5 דקות, אידיאלי למשחק בוגל סטנדרטי), אתגר מחשבתי, ואלמנט תחרותי קליל מול חברים או שחקנים אקראיים מרחבי העולם. בעוד שיש מספר אפליקציות הדומות לבוגל בחנויות, האפליקציה שלי תוכננה עם דגשים ייחודיים בהשראת מודלים מוצלחים בשוק:

* **Boggle With Friends: Word Game (Zynga):**
האפליקציה הרשמית והמוכרת ביותר כיום בשוק למשחק בוגל. היא מציעה חוויה חברתית עשירה. עם זאת, קיים הבדל משמעותי במכניקת הליבה: ב-"Boggle With Friends", שיטת הניקוד מבוססת על ערך אינדיבידואלי לכל אות (בדומה למשחק "שבץ-נא" / Scrabble), בתוספת קוביות המעניקות מכפילים (Double/Triple Word/Letter). **בשונה מכך, האפליקציה שלי שומרת על נאמנות לחוקי משחק הקופסה המקוריים:** הניקוד נקבע אך ורק על פי אורך המילה (לדוגמה, מילה בת 3-4 אותיות שווה נקודה אחת, 5 אותיות = 2 נקודות, וכן הלאה). גישה זו מחזירה את המיקוד של השחקן למציאת מילים ארוכות ומורכבות, במקום חיפוש טקטי של אותיות נדירות על גבי מכפילים.

* **Chess.com:**
למרות שזהו משחק שחמט ולא משחק מילים, פלטפורמת המובייל של Chess.com משמשת כמודל השראה מצוין עבורי לניהול משחקים מרובי משתתפים (Multiplayer) בזמן אמת. האופן שבו שחקנים יכולים להזמין חברים למשחק בקליק, לנהל רשימת חברים (Friends List), ולחוות התאמה מהירה וחלקה בשרתים (Realtime Sync), מהווה את הסטנדרט אליו שאפתי. מודל הלובי (Lobby), מערכת ההזמנות ושירותי הרקע (Push Notifications) באפליקציה שלי נבנו מתוך רצון לספק חווית חיבור חלקה ומהירה בדומה לפלטפורמות מבוססות-תחרות כגון זו.

## ניהול נתונים בפרוייקט

**אובייקטים נחוצים:**

**במשחק:**
* **לוח (Board)** - מיוצג על ידי מערך חד-ממדי של תווים (16 תווים) המייצגים את האותיות שהוגרלו בקוביות.
* **קוביות/משבצות (Dice/Cells)** - רכיב ויזואלי מותאם אישית (`SquareTextView`) השומר על פרופורציה ריבועית (1:1), מכיל את האות ויכול לשנות את צבע הרקע שלו בהתאם למצב (נבחר, מילה נכונה, מילה שגויה, רמז).
* **עץ תחיליות (Trie / Dictionary)** - מבנה נתונים רקורסיבי המשמש לאחסון וחיפוש יעיל של מילים. המילון הרשמי של המשחק נטען למבנה זה עם עליית האפליקציה. במהלך המשחק, מנוע המשחק משתמש בגרסה מורחבת שלו (`PathTrie`) אשר שומרת לא רק את המילים החוקיות שנמצאו על הלוח בעזרת אלגוריתם DFS, אלא גם את המסלול המדויק שלהן, מה שמאפשר אימות מהיר והצגת רמזים למשתמש.
* **מנוע המשחק (BoggleGame)** - מחלקה המנהלת את מצב המשחק (State), כולל הניקוד הנוכחי, רשימת המילים שנמצאו, האינדקסים של הקוביות שנבחרו כרגע, מד הזמן, ועץ התחיליות (`PathTrie`) המכיל ומחשב מראש את כל הפתרונות האפשריים ללוח הנוכחי.
* **חדר משחק (Multiplayer Room)** - מיוצג בבסיס הנתונים (Firebase) עם פרטים כמו: קוד החדר, רשימת השחקנים (מארח ואורחים), מחרוזת הלוח שהוגרל, מצב המשחק (פעיל/הסתיים), ורשימות המילים שכל שחקן מצא, המתעדכנות בזמן אמת.

**מחוץ למשחק:**
* **משתמש (User)** - המשתמש שמור בשני מקומות שונים. החלקים הקשורים לאימות והתחברות (דוא"ל וסיסמה) שמורים ב-`Firebase Authentication`. שאר הפרטים כמו: מזהה ייחודי (UID), שם תצוגה, תמונת פרופיל (מקודדת ב-Base64), אסימון התראות (FCM Token), רשימת חברים, והיסטוריית תוצאות משחקים ששוחקו, נשמרים ב-`Firebase Realtime Database`.
* **הזמנות (Invitations)** - מיוצגות במסד הנתונים וכוללות את פרטי השולח, קוד החדר והודעה אישית.

**אינטראקציה ועדכון אובייקטים מרובים:**
הפעולות באפליקציה בדרך כלל משפיעות על יותר מאובייקט אחד. לדוגמה, כאשר שחקן מוצא מילה תקנית במשחק, המערכת חייבת להוסיף את המילה לרשימת המילים שנמצאו במנוע המשחק המקומי (`BoggleGame`), להגדיל את הניקוד, לעדכן את התצוגה הויזואלית (לצבוע את הקוביות הרלוונטיות בירוק ב-`BoggleView`), ואם מדובר במשחק מרובה משתתפים - לעדכן בזמן אמת את מסד הנתונים (`Firebase`) כדי שהשרת ישתף את המילה שנמצאה עם שאר השחקנים.
דוגמה נוספת: שליחת הזמנה לחבר מעדכנת את רשומת ההזמנות ב-`Firebase Realtime Database`, מה שמפעיל פונקציית שרת (Cloud Function) הקוראת את אסימון ה-FCM של החבר ושולחת לו התראת דחיפה (Push Notification) למכשיר, דרכה הוא יכול להצטרף לחדר.

<h1 align=center id="architecture">מבנה / ארכיטקטורה</h1>

## קבצי הפרוייקט

| קבצי קוד | קבצי משאב |
| :---: | :---: |
| ![code files](https://github.com/user-attachments/assets/e715ff5f-a94b-4a83-89f1-e172fc92c3ce) | ![res files](https://github.com/user-attachments/assets/b1503308-0418-4e0a-a403-ef0054967a96) |



## מסכי הפרוייקט

## תרשים זרימת מסכים

<h1 align=center id="implementation">מימוש הפרוייקט</h1>

## קבצי gradle, libs.versions.toml, וmanifest

gradle:
Gradle (גרדל) הוא כלי בניית הפרוייקט (Build System) הרשמי של אנדרואיד. הוא אחראי על תהליך ההידור (קומפילציה), אריזת הקוד, קבצי המשאבים (Resources) והספריות החיצוניות לקובץ התקנה סופי (APK או AAB). בנוסף, דרך קבצי ה-Gradle אני מנהל את הגדרות הפרוייקט, גרסאות ה-SDK, סוגי הבנייה (למשל Debug מול Release) והתלויות (Dependencies) של האפליקציה. הקבצים מחולקים לשתי רמות: רמת הפרוייקט (הגדרות כלליות) ורמת האפליקציה (הגדרות ספציפיות למודול).
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
        destinationDir = file("${project.rootDir}/docs/javadoc")

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
```
רמת פרוייקט:
``` gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
}
```

libs.versions.toml:
קובץ זה (Version Catalog) משמש לניהול מרוכז ומסודר של כל גרסאות הספריות, התוספים (Plugins) והתלויות בפרוייקט. במקום לכתוב את גרסת הספרייה בקובץ ה-Gradle של כל מודול בנפרד, מגדירים הכל כאן, מה שמקל על תחזוקה, מונע התנגשויות גרסאות, ושומר על סדר (במיוחד בפרוייקטים מרובי מודולים).
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

manifest:
קובץ ה-`AndroidManifest.xml` הוא קובץ הגדרות הליבה של האפליקציה, והוא "תעודת הזהות" שלה מול מערכת ההפעלה Android. בו מוצהרים כל רכיבי האפליקציה (כמו מסכים - Activities, ושירותי רקע - Services), ההרשאות הנדרשות (כמו גישה לאינטרנט או קבלת התראות), הגדרות הנושא (Theme), אייקון האפליקציה, ונקודת הכניסה הראשית (איזה מסך נפתח כשהאפליקציה עולה). בלעדיו, המערכת לא תדע כיצד להריץ את האפליקציה.
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
    </application>

</manifest>
```
## תיאור מחלקות UML
<img width="7160" height="2386" alt="UML Chart" src="https://github.com/user-attachments/assets/4e7f9871-8ec7-4ce1-a1c3-4314dffc1ff8" />

### פירוק לחלקים:

מסך ראשי:
<img width="1710" height="838" alt="Main Menu" src="https://github.com/user-attachments/assets/b6017e9f-272c-4185-8e34-7835dc67f12a" />

לוגיקת משחק:
<img width="984" height="624" alt="Game logic" src="https://github.com/user-attachments/assets/e172fbb0-0df4-4ee7-bb3f-0174f94c62ff" />

מחלקות עזר:
<img width="1629" height="1074" alt="Utility classes" src="https://github.com/user-attachments/assets/7154cd94-94ec-4497-b667-f67e31ae83e9" />

מרובה שחקנים:
<img width="3705" height="1345" alt="Multiplayer Logic" src="https://github.com/user-attachments/assets/50fbdacd-e711-42aa-bad8-cabdf27b3d51" />

שחקן יחיד:
<img width="1201" height="842" alt="Singleplayer" src="https://github.com/user-attachments/assets/9c46cfbd-2d32-4f9d-8137-2b1c5fb5a298" />

ביצת הפתעה (דונאט מסתובב):
<img width="577" height="465" alt="Donut easter egg" src="https://github.com/user-attachments/assets/d9e0774a-4b74-4958-9024-6935ef04ccad" />




## בסיס נתונים

## פונקציות שרת

``` node.js
const {setGlobalOptions} = require("firebase-functions");

setGlobalOptions({maxInstances: 10});

const functions = require("firebase-functions");
const { onValueCreated } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");
admin.initializeApp();

// eslint-disable-next-line max-len
exports.sendInvitationNotification = onValueCreated(
    {
        ref: "/invitations/{targetUserId}/{invitationId}",
        region: "europe-west1",
        instance: "idk-a-school-project-or-smth-default-rtdb"
    },
    async (event) => {
        // get everything from the single 'event' object
        const targetUserId = event.params.targetUserId;
        const invitationData = event.data.val();

        try {
            // eslint-disable-next-line max-len
            const tokenSnapshot = await admin.database().ref(`/users/${targetUserId}/fcmToken`).once("value");
            const fcmToken = tokenSnapshot.val();

            if (!fcmToken) {
                console.log("No FCM token found for user: ", targetUserId);
                // keep the invitation even if notification fails so user can see it manually
                return null;
            }

            const payload = {
                notification: {
                    title: `New Invite from ${invitationData.senderName}`,
                    // eslint-disable-next-line max-len
                    body: `${invitationData.message} Room Code: ${invitationData.roomCode}`,
                },
                data: {
                    roomCode: String(invitationData.roomCode),
                    invitationId: String(event.params.invitationId)
                },
                token: fcmToken,
            };

            const response = await admin.messaging().send(payload);
            console.log("Successfully sent invitation with room code:", response);

            return null;
        } catch (e) {
            console.error("Error sending notification:", e);
            return null;
        }
    }
);
```
מטרה: כאשר נכתבת הזמנה למסד הנתונים, תשלח הודעה לשחקן שהוזמן כדי שתקפוץ לו בטלפון התרעה

## מחלקות הפרוייקט

### מחלקת אפליקציה

<a id="boggleapplication"></a>
#### `public class BoggleApplication extends Application`
תפקיד המחלקה: מחלקת האפליקציה המותאמת המשמשת לאתחולים גלובליים הדורשים הפעלה פעם אחת בלבד במהלך עליית האפליקציה.

פעולות המחלקה:
```java
@Override
public void onCreate()
```
מופעלת עם עליית האפליקציה לפני הפעלת המסכים. מאתחלת את המילון (Dictionary) מקובץ רשימת המילים כדי שיהיה מוכן ומסודר בזיכרון, ובנוסף מפעילה עדכון אסינכרוני לנתוני המשתמש ב-`FirebaseHandler`.

### חבילה: data

<a id="trie"></a>
#### `public abstract class Trie<T extends Trie<T>`
תפקיד המחלקה: מחלקת בסיס לעץ תחיליות ששומר על עצמו מתהליכונים שפועלים במקביל. המחלקה היא ג'נרית רקורסיבית כדי שהמחלקות שממשות אותם לא יצטרכו לעשות את העבודה הקשה.

שדות המחלקה:
``` java
/** The number of letters in the English alphabet ('a' through 'z'). */
protected static final int ALPHABET_SIZE = 26;

/** Atomic array of pointers to child nodes, indexed by character ('a' to 'z'). */
protected final AtomicReferenceArray<T> children = new AtomicReferenceArray<>(ALPHABET_SIZE);

/** Flag indicating if this node represents the end of a complete word. */
protected volatile boolean isEndOfWord;

/** Flag indicating if this node has no children. */
protected volatile boolean isLeaf;
```
תכונות המחלקה:
``` java
- boolean isEndOfWord
- boolean isLeaf
- Set<String> words
- int size
- toString()
```

פעולות המחלקה:
``` java
public boolean containsKey(char ch) {
    int index = ch - 'a';
    return index >= 0 && index < ALPHABET_SIZE && children.get(index) != null;
}
```
בודק ומחזיר האם יש ילד ב"כיוון" של האות

``` java
public T get(char ch) {
    int index = ch - 'a';
    if (index < 0 || index >= ALPHABET_SIZE) return null;
    return children.get(index);
}
```
מחזיר את הילד בכיוון של אות, או null אם לא קיים, או אם מחוץ לתחום.

``` java
public T get(String s){
    T node = (T) this;
    for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (node.containsKey(ch)) {
            node = node.get(ch);
        } else {
            return null;
        }
    }
    return node;
}
```
מחפש איטרטיבית את הצומת של השרשרת המדוברת, או null אם לא קיים.

``` java
public T putIfAbsent(char ch){
    if (ch > 'z' || ch < 'a') return null;
    if (containsKey(ch)) return get(ch);

    T newNode;
    try {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        assert genericSuperclass != null : "Failed to instantiate Trie node";
        Class<T> type = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        newNode = type.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
        throw new RuntimeException("Failed to instantiate Trie node", e);
    }

    if (children.compareAndSet(ch - 'a', null, newNode)) {
        isLeaf = false;
        return newNode;
    } else {
        return children.get(ch - 'a');
    }
}
```
מוסיף את הילד בכיוון האות אם הוא חסר, ומחזיר את מה שהוסיף אם לא היה או את מה שהיה.
משתמש בשיקוף (reflection) כדי להשיג את הבנאי הנכון למחלקה גם למחלקות בנות.
משתמש בפעולות מוגנות לתהליכונים בשביל שימוש במקביל.

``` java
public T put(String str){
    T node = (T) this;
    for (int i = 0; i < str.length(); i++) {
        char ch = str.charAt(i);
        if (ch < 'a' || ch > 'z') continue;
        node = node.putIfAbsent(ch);
    }
    node.isEndOfWord = true;
    return node;
}
```
מוסיף את כל השרשרת מהצומת והלאה, ומסמן את הצומת האחרונה כסוף המילה.
מחזיר את הצומת האחרונה.

``` java
public int size() {
    int toAdd = isEndOfWord ? 1: 0;
    for (int i = 0; i < ALPHABET_SIZE; i++) {
        T child = children.get(i);
        if (child != null) {
            toAdd += child.size();
        }
    }
    return toAdd;
}
```
מחשב את כמות המילים שנמצאות בעץ מהצומת הנוכחית והלאה בצורה רקורסיבית.

``` java
public Set<String> getWords(){
    Set<String> words = new ArraySet<>(size());
    _getWords("", words);
    return words;
}

protected void _getWords(String word, Set<String> set) {
    if (isEndOfWord()) {
        set.add(word);
    }
    if (isLeaf) return;
    for (int i = 0; i < ALPHABET_SIZE; i++) {
        T child = children.get(i);
        if (child != null) {
            child._getWords(word + (char)(i + 'a'), set);
        }
    }
}
```
זוג פונקציות, רקורסיבית ומעטפת, שאוספות את כל המילים בעץ ומחזירות אותר בקבוצה.

<a id="dictionary"></a>
#### `public final class Dictionary extends Trie<Dictionary>`
שדות המחלקה:
``` java
/**
 * Static root instance of the dictionary.
 */
public static final Dictionary ROOT = new Dictionary();

/** Flag indicating if the dictionary has been loaded with words. */
private boolean isInitialized = false;
```
תגונות המחלקה: אין ייחודיות

פעולות המחלקה:
``` java
public static boolean contains(@NonNull String word) {
    Trie<?> node = ROOT.get(word);
    return node != null && node.isEndOfWord();
}
```
בודק אם המילה קיימת במילון, תמיד בודק מהשורש.

``` java
public synchronized void init(InputStream file) {
    if (isInitialized) return;
    
    Scanner sc = new Scanner(file);
    while (sc.hasNextLine()) {
        String word = sc.nextLine().trim().toLowerCase();
        if (!word.isEmpty()) {
            put(word);
        }
    }
    sc.close();
    isInitialized = true;
}
```
ממלא את המילון עם הקובץ שהועבר.
זה synchronized כדי להגן מקריאה יותר מפעם אחת, גם אם הקריאות באותו הזמן.

<a id="pathtrie"></a>
#### `public class PathTrie extends Trie<PathTrie>`
תפקיד המחלקה: להרחיב את Trie עם יכולת גם לשמור את המסלול על הלוח עליו הצירוף אותיות נמצא, יכול לשמור רק מסלול אחד לכל צירוף.

שדות המחלקה:
``` java
    /** The path (sequence of board indices) associated with the word ending at this node. */
    private String path;
```

תכונות המחלקה:
``` java
- String path
```

פעולות המחלקה:
``` java
public PathTrie put(String str, String path){
    PathTrie node = super.put(str);
    if (path != null) {
        node.path = path;
    }
    return node;
}
```
מוסיף גם את המסלול לצומת האחרונה.

``` java
public HashMap<String, String> toMap() {
    HashMap<String, String> map = new HashMap<>();
    for (String s : getWords()) {
        map.put(s, get(s).getPath());
    }
    return map;
}
```
יוצר מפה מהמחלקה כאשר המפתחות הן המילים, והערכים הם המסלולים.

<a id="firebasehandler"></a>
#### `public class FirebaseHandler`
תפקיד המחלקה: מחלקה יחידנית ששומרת אצלה את כל הדברים שקשורים לFirebase ולשחקן הנוכחי.

שדות המחלקה:
``` java
/** Tag used for logging. */
private static final String TAG = "FirebaseHandler";
/** Singleton instance. */
private static FirebaseHandler instance;
/** Instance of Firebase Authentication. */
private final FirebaseAuth mAuth;
/** Instance of Firebase Realtime Database. */
private final FirebaseDatabase mDatabase;
/** Instance of Firebase Messaging. */
private final FirebaseMessaging mMessaging;

/** Cached local user data. */
private User user;
```

תכונות המחלקה:
``` java
- FirebaseHandler instance
- FirebaseAuth auth
- FirebaseDatabase database
- FirebaseMessaging messaging
- FirebaseUser currentUser
- User Userdata
- String currentUserId
- DatabaseReference rootRef
- DatabaseReference userRef
```

פעולות המחלקה:
``` java
public void updateUserData() {
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
        // First, reload the user to check if they are still valid in Firebase Auth
        currentUser.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                // User is still valid in Auth, now check the database
                DatabaseReference userRef = getUserRef();
                if (userRef != null) {
                    userRef.get().addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful() && dbTask.getResult().exists()) {
                            user = dbTask.getResult().getValue(User.class);
                        }
                    });
                }
            } else {
                Log.e(TAG, "User reload failed", reloadTask.getException());
            }
        });
    } else {
        user = null;
    }
}
```
בודק אם המשתמש עדיין ואלידי (אם לא נמחק או הוקפא) ומעדכן את הפרטים שלו מהמוסד נתונים

``` java
public void signOut() {
    mAuth.signOut();
    user = null;
}
```
מנתק את המשתמש ומנקה את המידע השמור מקומית.

``` java
public void addFriend(String id) {
    DatabaseReference usersRef = mDatabase.getReference("users");
    DatabaseReference myFriendRef = getUserRef();
    if (myFriendRef != null) {
        myFriendRef.child("friends").child(id).setValue(true);
        DatabaseReference friendFriendsRef = usersRef.child(id).child("friends").child(getCurrentUserId());
        friendFriendsRef.setValue(true);
        Log.d(TAG, "Friend added: " + id);
    }
}
```
מוסיף מתשתמש לרשימת החברים


<a id="gamemode"></a>
#### `public enum GameMode`
שדות המחלקה:
`singleplayer`
`multiplayer`
תכונות המחלקה:
``` java
- GameMode singleplayer
- GameMode multiplayer
```
פעולות המחלקה: הפעולות שהורשו מ <Enum<E

<a id="playerrole"></a>
#### `public enum PlayerRole`
שדות המחלקה:
`host`
`guest`
תכונות המחלקה:
``` java
- PlayerRole host
- PlayerRole guest
```
פעולות המחלקה: הפעולות שהורשו מ <Enum<E

<a id="user"></a>
#### `public class User`

תפקיד המחלקה: לשמור את המידע של המשתמש מקומית, ולבסס את הצורה שהמידע של משתמשים נשמר, במיוחד באינראקציה עם המסד נתונים. 
בגלל שכל מה שהמחלקה עושה היא לשמור מידע,והיא לא תלוייה בשום דבר אחר, היא גם נקראת POJO (Plain Old Java Object)

שדות המחלקה:
``` java
/** The user's unique ID. */
private String uid;
/** The user's chosen display name. */
private String displayName;
/** The user's email address, used for authentication and identification. */
private String profileImageBase64;
/** A Base64 encoded string of the user's profile picture. */
private String fcmToken;
/** The User's current device's Firebase Cloud Messaging (FCM) token. */
private String fcmToken;
```

תכונות המחלקה:
``` java
- String uid;
- String displayName;
- String email;
- String profileImageBase64;
- String fcmToken;
```

פעולות המחלקה: אין

### חבילה: engine

<a id="bogglegame"></a>
#### `public class BoggleGame`
תפקיד המחלקה: מייצגת מופע של משחק בוגל, מנהלת את מצב המשחק, הקוביות, הניקוד ואימות המילים. היא מטפלת ביצירת הלוח, בחירת מילים על ידי השחקן, לוגיקת הניקוד וחישוב מראש של כל הפתרונות האפשריים.

שדות המחלקה:
```java
/** The total duration of a game in milliseconds. */
public static final long GAME_TIME_MILLIS = 180000;

/** The 1D array representation of the 4x4 board letters. */
private final char[] board;
/** Tracks the indices of dice currently selected by the player to form a word. */
private final ArrayDeque<Integer> selectedIndices;
/** Stores words successfully found and submitted by the player. */
private final ArrayList<String> foundWords;
/** The player's current cumulative score. */
private int score = 0;
/** Number of hints available to the player. */
private int hints = 999;
/** Flag indicating if the game has concluded. */
private boolean gameEnded;

// Listeners for game events, using CopyOnWriteArrayList for thread safety during iteration
private final List<OnGameEndListener> onGameEndListeners = new CopyOnWriteArrayList<>();
private final List<OnWordFoundListener> onWordFoundListeners = new CopyOnWriteArrayList<>();
private final List<OnTickListener> onTickListeners = new CopyOnWriteArrayList<>();

/** Trie containing all valid words that can be formed on the current board. */
private final PathTrie solutions;
/** List of all possible valid paths on the board. */
private final List<String> allPaths;
/** Timer managing the game countdown. */
private final Timer gameTimer;
```

ממשקי המאזינים (Listeners):
```java
public interface OnGameEndListener {
    /** Called when the game timer expires or the game is manually ended. */
    void onGameEnd();
}

public interface OnWordFoundListener {
    /** Called when a valid word is found. @param word The word that was found. */
    void onWordFound(String word);
}

public interface OnTickListener {
    /** Called on every timer tick. @param elapsedTime Time elapsed since start in ms. */
    void onTick(long elapsedTime);
}
```

תכונות המחלקה:
``` java
- int score
- int hints
- ArrayList<String> foundWords
- int[] selectedIndices
- char[][] dice
- String word
- PathTrie solutions
- List<String> allPaths
- boolean isEnded
- char[] board
- List<OnGameEndListener> onGameEndListeners
- List<OnWordFoundListener> onWordFoundListeners
- List<OnTickListener> onTickListeners
```

פעולות המחלקה:
``` java
public BoggleGame() {
    this(generateBoard());
}
```
בנאי המאתחל משחק חדש עם לוח שנוצר אקראית.

``` java
public BoggleGame(char[] board) {
    this.board = board;
    this.foundWords = new ArrayList<>();
    this.selectedIndices = new ArrayDeque<>();
    this.gameEnded = false;

    // Solve the board using the GameSolver and the dictionary root.
    // This is done upfront to provide immediate feedback on word validity during the game.
    GameSolver.SolverResult result = new GameSolver().solve(getDice(), Dictionary.ROOT);
    solutions = result.solutions();
    allPaths = result.allPaths();

    // Initialize the game timer with total duration and callbacks for ticks and completion.
    gameTimer = new Timer(GAME_TIME_MILLIS,
            (elapsedTime) -> {
                for (OnTickListener listener : onTickListeners) {
                    listener.onTick(elapsedTime);
                }
            },
            this::endGame);
}
```
בנאי המאתחל משחק עם לוח ספציפי. הוא פותר את הלוח מראש ומגדיר את הטיימר.

``` java
private static char[] generateBoard() {
    ArrayList<Die> diceList = Die.generateDice();
    Collections.shuffle(diceList);
    char[] board = new char[16];
    for (int i = 0; i < 16; i++) {
        Die die = diceList.get(i);
        die.roll();
        board[i] = die.getLetter();
    }
    return board;
}
```
מייצר לוח אקראי של 16 אותיות על בסיס קוביות הבוגל הסטנדרטיות.

``` java
public void addOnGameEndListener(OnGameEndListener listener) {
    this.onGameEndListeners.add(listener);
}

public void addOnWordFoundListener(OnWordFoundListener listener) {
    this.onWordFoundListeners.add(listener);
}

public void addOnTickListener(OnTickListener listener) {
    this.onTickListeners.add(listener);
}
```
פעולות להוספת מאזינים לאירועי סיום משחק, מציאת מילה ותקתוק של הטיימר.

``` java
public WordCheckResult submitWord() {
    String formedWord = formWord(); // Note: this clears the selection indices
    if (formedWord.isBlank()) return NULL_WORD;
    if (formedWord.length() < 3) return TOO_SHORT;
    if (foundWords.contains(formedWord)) return ALREADY_FOUND;
    
    // Check if word exists in the dictionary. Using solutions trie would also work and be faster.
    if (Dictionary.contains(formedWord)) {
        score += wordScore(formedWord);
        foundWords.add(formedWord);
        for (OnWordFoundListener listener : onWordFoundListeners) {
            listener.onWordFound(formedWord);
        }
        return VALID;
    }
    return INVALID;
}
```
בודק את המילה שנבחרה, מעדכן את הניקוד ומודיע למאזינים אם המילה תקינה.

``` java
public int wordScore(String word) {
    int wordLength = word.length();
    return switch (wordLength) {
        case 3, 4 -> 1;
        case 5 -> 2;
        case 6 -> 3;
        case 7 -> 5;
        default -> wordLength >= 8 ? 11 : 0;
    };
}
```
מחשב ניקוד למילה לפי חוקי המשחק הסטנדרטיים.

``` java
public boolean selectDie(int index) {
    if (selectedIndices.isEmpty()) {
        selectedIndices.add(index);
        return true;
    }
    int lastIndex = selectedIndices.getLast();
    if (isAdjacent(lastIndex, index) && !selectedIndices.contains(index)) {
        selectedIndices.add(index);
        return true;
    }
    return false;
}
```
מנסה לבחור קובייה בלוח. הבחירה תצליח אם זו הקובייה הראשונה או שהיא סמוכה לקובייה האחרונה שנבחרה וטרם נעשה בה שימוש במילה הנוכחית.

``` java
public int getMaxScore() {
    int maxScore = 0;
    for (String s : solutions.getWords()) {
        maxScore += wordScore(s);
    }
    return maxScore;
}
```
מחשב את הניקוד המקסימלי האפשרי ללוח הנוכחי.

``` java
public void endGame() {
    if (gameEnded) return;
    gameEnded = true;
    gameTimer.stop();
    for (OnGameEndListener listener : onGameEndListeners) {
        listener.onGameEnd();
    }
}
```
מסיימת את המשחק, עוצרת את הטיימר ומעדכנת את המאזינים.

<a id="gamesolver"></a>
#### `public class GameSolver`
תפקיד המחלקה: מספקת את לוגיקת הליבה לפתרון לוח בוגל בצורה יעילה ומקבילית. היא מזהה את כל המילים התקינות מהמילון שניתן ליצור על לוח 4x4 על ידי חיבור קוביות סמוכות, תוך שימוש באלגוריתם חיפוש לעומק (DFS) ומסגרת ה-ForkJoin לניצול מעבדים מרובי ליבות.

שדות המחלקה:
```java
/**
 * A Trie to store all unique words found on the board.
 * The value associated with each terminal node is the hex-encoded path representing the word's discovery.
 */
private PathTrie solutions;

/**
 * A thread-safe queue used to collect every valid path discovered during the search.
 * Since multiple paths can form the same word, this stores all of them for visualization or scoring purposes.
 */
private ConcurrentLinkedQueue<String> allPaths;
```

תכונות המחלקה:
``` java
- PathTrie solutions
- List<String> allPaths
```

פעולות המחלקה:
``` java
public SolverResult solve(char[][] board, Dictionary dictionary)
```
הפעולה המרכזית שמתחילה את תהליך הפתרון. היא מייצרת משימת חיפוש לכל תא בלוח ומפעילה אותן במקביל.

<a id="gamesolvertask"></a>
##### `class GameSolverTask extends RecursiveAction`
מחלקה פנימית המבצעת את החיפוש הרקורסיבית. היא משתמשת ב-`RecursiveAction` כדי להתחלק למשימות משנה המבוצעות במקביל.

פעולות המחלקה הפנימית:
``` java
public GameSolverTask(Dictionary root, char[][] board, int i, int j, short visited, String path, String string)
```
בנאי המאתחל משימת חיפוש עבור מיקום ספציפי בלוח, תוך שמירה על מצב החיפוש (הצומת הנוכחי במילון, תאים שבוקרו, והמילה שנוצרה עד כה).

``` java
@Override
protected void compute()
```
מבצעת את לוגיקת החיפוש המקבילית:
1. בודקת אם הצומת הנוכחי במילון הוא עלה (אין מילים ארוכות יותר). אם כן, מוסיפה את המילה לפתרונות ומפסיקה.
2. בודקת אם הצומת הוא סוף מילה (מילה תקנית). אם כן, מוסיפה לפתרונות וממשיכה לחפש מילים ארוכות יותר.
3. מסמנת את התא הנוכחי כ"בוקר" ב-bitmap.
4. סורקת את כל 8 השכנים בלוח. עבור כל שכן שתואם לאות אפשרית במילון ולא בוקר בעבר, יוצרת משימת משנה (`GameSolverTask`) חדשה.
5. מפעילה את כל משימות המשנה במקביל באמצעות `invokeAll`.

``` java
private boolean isSafe(int i, int j, short visited)
```
פעולת עזר הבודקת האם קואורדינטות `(i, j)` נמצאות בתוך גבולות הלוח והאם התא טרם בוקר במסלול הנוכחי (באמצעות בדיקת הביט המתאים ב-`visited`).

<a id="donutrenderer"></a>
#### `public class DonutRenderer`
תפקיד המחלקה: ביצת הפתעה. מחלקה האחראית על רינדור תלת-ממדי בזמן אמת של צורת טורוס (דונאט) מסתובבת על גבי `SurfaceView`. המימוש הושרא מהקוד המפורסם [`Donut.c`](https://www.a1k0n.net/2011/07/20/donut-math.html) של `a1k0n`.

שדות המחלקה:
```java
/** Default rotation rates for angles A and B */
public static final double A_RATE = 0.005;
public static final double B_RATE = 0.007;

/** Paints for scaling and drawing segments */
public static final Paint scalingPaint = new Paint();
public static final Paint shapePaint = new Paint();

/** Light source settings */
private static final double[] lightVector = normalize(new double[]{0, 1, -1});
public static final float MIN_LIGHT = 0.2f;

/** Rotation angles around two axes */
private double A = 0, B = 0;

/** Back-buffer bitmap for rendering before displaying on surface */
private Bitmap bitmap;

private int screenWidth;
private int screenHeight;

/** The vector representing the light source direction in 3D space */
private static final double[] lightVector = normalize(new double[]{0, 1, -1});
/** Minimum light level for lighting calculations */
public static final float MIN_LIGHT = 0.2f;

// Geometry resolution settings
private static final double thetaSpacing = 0.1;
private static final double phiSpacing = 0.07;
private static final int thetaSteps = (int) (2 * Math.PI / thetaSpacing + 1);
private static final int phiSteps = (int) (2 * Math.PI / phiSpacing + 1);

// Torus dimensions
private static final double R1 = 1; // Radius of the tube
private static final double R2 = 2; // Radius from center to tube center

/** Distance from the viewer to the object center */
private static double K2 = 10;
/** Projection constant based on screen size */
private double K1;

/** Rotation angles around two axes */
private double A = 0, B = 0;

/** Flag indicating if the surface is ready for drawing */
private boolean isSurfaceReady = false;

```

תכונות המחלקה:
``` java
- double A_RATE
- double B_RATE
- float MIN_LIGHT
```

פעולות המחלקה:
``` java
public DonutRenderer(SurfaceView surfaceView) {
    this.surfaceView = surfaceView;
    scalingPaint.setFilterBitmap(false);
    shapePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    shapePaint.setStrokeWidth(5);
    surfaceView.getHolder().addCallback(this);
    
    // If surface is already valid, initialize immediately
    if (surfaceView.getHolder().getSurface().isValid()) {
        isSurfaceReady = true;
        initResources(surfaceView.getWidth(), surfaceView.getHeight());
    }
}
```
בנאי המאתחל את הרנדרר ומקשר אותו ל-`SurfaceView`.

``` java
@Override
public void doFrame(long frameTimeNanos) {
    if (choreographer == null) return;

    if (isSurfaceReady) {
        int width = surfaceView.getWidth();
        int height = surfaceView.getHeight();
        if (width > 0 && height > 0) {
            initResources(width, height);

            if (bitmap != null) {
                Canvas surfaceCanvas = surfaceView.getHolder().lockCanvas();
                if (surfaceCanvas != null) {
                    drawDonut();
                    // Draw the back-buffer bitmap to the surface
                    surfaceCanvas.drawBitmap(bitmap, 0, 0, null);
                    surfaceView.getHolder().unlockCanvasAndPost(surfaceCanvas);
                }
            }
        }
    }

    // Request next frame
    choreographer.postFrameCallback(this);
}
```
מתודה המופעלת בכל פריים על ידי ה-`Choreographer` לניהול האנימציה.

``` java
private void drawDonut() {
    if (bitmap == null) return;

    // Create a canvas to draw on the back-buffer BITMAP
    Canvas bitmapCanvas = new Canvas(bitmap);
    bitmapCanvas.drawColor(Color.DKGRAY);

    // Precompute trigonometric values for current rotation angles to optimize performance
    double cosA = Math.cos(A), sinA = Math.sin(A);
    double cosB = Math.cos(B), sinB = Math.sin(B);

    // Generate the 3D grid of points projected into 2D screen space
    PointAndDepth[][] grid = getToroidalMap(cosA, sinA, cosB, sinB);

    // Thread-safe list to store quadrilateral faces for depth sorting
    java.util.List<Quad> quadsToDraw = Collections.synchronizedList(new java.util.ArrayList<>());

    // Use a parallel stream to distribute heavy mathematical computations across CPU cores
    IntStream.range(0, thetaSteps).parallel().forEach(thetaIndex -> {
        int nextTheta = (thetaIndex + 1) % thetaSteps;
        double theta = thetaIndex * thetaSpacing;
        double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

        for (int phiIndex = 0; phiIndex < phiSteps; phiIndex++) {
            int nextPhi = (phiIndex + 1) % phiSteps;
            double phi = phiIndex * phiSpacing;
            double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

            // --- Lighting Logic ---
            // 1. Define the surface normal in local coordinates
            double nx = cosTheta * cosPhi;
            @SuppressWarnings("UnnecessaryLocalVariable")
            double ny = sinTheta;
            
            // 2. Rotate the normal to match the current orientation of the torus (A and B angles)
            double rotNx = nx * (cosB * cosPhi + sinA * sinB * sinPhi) - ny * cosA * sinB;
            double rotNy = nx * (sinB * cosPhi - sinA * cosB * sinPhi) + ny * cosA * cosB;
            double rotNz = nx * cosA * sinPhi + ny * sinA;
            
            // 3. Calculate luminosity via dot product with the light source vector
            float L = (float) (lightVector[0] * rotNx + lightVector[1] * rotNy + lightVector[2] * rotNz);

            // --- Geometry Mapping ---
            // Retrieve the four projected corners of the current quad face
            PointAndDepth p1 = grid[thetaIndex][phiIndex];
            PointAndDepth p2 = grid[nextTheta][phiIndex];
            PointAndDepth p3 = grid[thetaIndex][nextPhi];
            PointAndDepth p4 = grid[nextTheta][nextPhi];
            
            // Calculate average inverse depth (1/z) for sorting (larger ooz means closer to viewer)
            double avgOoz = (p1.ooz() + p2.ooz() + p3.ooz() + p4.ooz()) * 0.25;

            // --- Styling ---
            // Calculate final brightness, hue (based on phi), and saturation (based on theta)
            float luminosity = getLuminosityWithMinLight(L);
            float hue = (phiIndex * 360f / phiSteps) % 360;
            float saturation = Math.abs(thetaIndex - thetaSteps * 0.5f) / (thetaSteps * 0.5f);
            int color = Color.HSVToColor(new float[]{hue, saturation, luminosity});

            // Define the 2D path for the quad face
            Path path = new Path();
            path.moveTo((float)p1.screenX(), (float)p1.screenY());
            path.lineTo((float)p2.screenX(), (float)p2.screenY());
            path.lineTo((float)p4.screenX(), (float)p4.screenY());
            path.lineTo((float)p3.screenX(), (float)p3.screenY());
            path.close();

            quadsToDraw.add(new Quad(path, color, avgOoz));
        }
    });
    
    // Painter's Algorithm: Sort quads by depth (back-to-front) to ensure correct occlusion
    quadsToDraw.sort(Comparator.comparingDouble(Quad::avgOoz));
    
    // Render the sorted quads to the bitmap canvas
    for (Quad quad : quadsToDraw) {
        shapePaint.setColor(quad.color());
        bitmapCanvas.drawPath(quad.path(), shapePaint);
    }
}
}
```
הלוגיקה המרכזית של הרינדור: ביצוע טרנספורמציות גיאומטריות, חישובי תאורה, מיון לפי עומק וציור בפועל.

``` java
public PointAndDepth[][] getToroidalMap(double cosA, double sinA, double cosB, double sinB) {
    PointAndDepth[][] grid = new PointAndDepth[thetaSteps][phiSteps];

    IntStream.range(0, thetaSteps).parallel().forEach(i -> {
        double theta = i * thetaSpacing;
        double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);
        
        // 2D Circle in the XY plane (cross-section of the torus)
        double circleX = R2 + R1 * cosTheta;
        double circleY = R1 * sinTheta;

        for (int j = 0; j < phiSteps; j++) {
            double phi = j * phiSpacing;
            double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

            // 3D Rotation and projection math
            // Final X position after rotations
            double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
            // Final Y position after rotations
            double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi) + circleY * cosA * cosB;
            // Final Z position (depth)
            double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
            
            // One over Z (inverse depth)
            double ooz = 1 / z;

            // Project to screen coordinates
            int screenX = toScreenX(screenWidth, x, ooz);
            int screenY = toScreenY(screenHeight, y, ooz);

            grid[i][j] = new PointAndDepth(screenX, screenY, ooz);
        }
    });
    return grid;
}
```
מחשבת את המיקומים של כל הנקודות על הטורוס במרחב התלת-ממדי ומטילה אותן לקואורדינטות מסך.

### חבילה: services

<a id="invitationservice"></a>
#### `public class InvitationService extends FirebaseMessagingService`
תפקיד המחלקה: אחראית על קבלת הודעות דחיפה (Push Notifications) מ-Firebase, ניהול הזמנות למשחק בזמן אמת והצגת התראות למשתמש.

שדות המחלקה:

```java
/** Tag used for logging. */
private static final String TAG = "InvitationService";
/** Notification channel ID for game invitations. */
private static final String CHANNEL_ID = "invitation_channel";
```

תכונות המחלקה: אין

פעולות המחלקה:

```java
@Override
public void onNewToken(@NonNull String token) {
    Log.d(TAG, "Refreshed token: " + token);
    if (FirebaseHandler.getAuth().getCurrentUser() != null){
        FirebaseHandler.getInstance().getUserRef().child("fcmToken").setValue(token);
    }
}
```
מתעדכנת כאשר נוצר אסימון (Token) חדש עבור ה-FCM של המכשיר. היא מעדכנת את האסימון החדש במסד הנתונים תחת פרטי המשתמש הנוכחי כדי לאפשר שליחת הודעות אליו.

```java
@Override
public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    Log.d(TAG, "From: " + remoteMessage.getFrom());

    Map<String, String> data = remoteMessage.getData();
    
    // Delete the invitation from the database now that it's received to avoid stale invites
    if (data.containsKey("invitationId")) {
        String invitationId = data.get("invitationId");
        deleteInvitation(invitationId);
    }

    // Check if message contains a notification payload.
    if (remoteMessage.getNotification() != null) {
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        showNotification(title, body, data);
    } else if (data.size() > 0) {
        // Handle data-only payload if notification block is missing
        String title = "New Game Invitation";
        String body = "Someone invited you to play Boggle!";
        showNotification(title, body, data);
    }
}
```
מתודה המופעלת כאשר מתקבלת הודעת FCM. היא מחלצת את נתוני ההזמנה, מוחקת את ההזמנה ממסד הנתונים (כדי שלא תישאר כ"פתוחה" לאחר שכבר הגיעה ליעד) ומציגה התראה למשתמש.

```java
private void deleteInvitation(String invitationId) {
    String currentUserId = FirebaseAuth.getInstance().getUid();
    if (currentUserId != null) {
        FirebaseHandler.getInstance().getRootRef()
                .child("invitations")
                .child(currentUserId)
                .child(invitationId)
                .removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Invitation deleted from DB: " + invitationId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete invitation", e));
    }
}
```
פעולת עזר המוחקת את ההזמנה הספציפית ממסד הנתונים של Firebase.

```java
private void showNotification(String title, String body, Map<String, String> data) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    
    // Pass room code and action if present to allow joining directly from notification
    if (data != null && data.containsKey("roomCode")) {
        intent.putExtra("roomCode", data.get("roomCode"));
        intent.putExtra("action", "join");
    }

    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

    NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // Create the NotificationChannel for Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Game Invitations",
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

    notificationManager.notify(0, notificationBuilder.build());
}
```
בונה ומציגה התראה במכשיר. היא מגדירה Intent שיוביל את המשתמש ישירות לחדר המשחק אם ההתראה מכילה קוד חדר, ומגדירה את ערוץ ההתראות עבור גרסאות אנדרואיד חדשות.


### חבילה: ui.mainmenu

<a id="mainactivity"></a>
#### `public class MainActivity extends AppCompatActivity`

מטרת מחלקה: המסך הראשי של האפליקציה. הוא משמש כנקודת הכניסה המרכזית, מנהל את התפריט הצידי (Navigation Drawer), את המעברים למצבי המשחק השונים (שחקן יחיד ומרובה שחקנים), את רשימת החברים, ואת הגישה למערכת ההזדהות (Login/Signup). בנוסף, הוא מטפל בקבלת הזמנות למשחק דרך Intent-ים.

שדות המחלקה:
```java
/** View binding for the activity layout. */
private ActivityMainBinding binding;

/** Listener for Firebase Authentication state changes. */
private FirebaseAuth.AuthStateListener authStateListener;

/**
 * Launcher for SingleplayerActivity to receive the final score when the game ends.
 */
private final ActivityResultLauncher<Intent> singleplayerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                int score = result.getData().getIntExtra(SingleplayerActivity.EXTRA_SCORE, 0);
                Toast.makeText(this, "Game finished! Your score: " + score, Toast.LENGTH_LONG).show();
            }
        }
);

/**
 * Launcher for requesting notification permissions (Android 13+).
 */
private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(this, "Notifications disabled. You won't receive game invites.", Toast.LENGTH_SHORT).show();
            }
        });
```

תכונות המחלקה: אין

פעולות המחלקה:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    
    // Enable edge-to-edge display
    EdgeToEdge.enable(this);
    
    // Handle window insets for both the main content and the navigation drawer
    ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });
    ViewCompat.setOnApplyWindowInsetsListener(binding.navView, (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });

    init();
    askNotificationPermission();
    setupAuthStateListener();
    handleIntent(getIntent());
}
```
מאתחלת את ה-View Binding, מגדירה תצוגה מקצה לקצה (EdgeToEdge), מגדירה מאזינים למרווחים של מערכת ההפעלה (Insets), ומפעילה פונקציות האתחול של המסך, ההרשאות ומצב המשתמש.

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
}
```
נקראת כאשר האקטיביטי כבר פתוחה ומקבלת Intent חדש (למשל מלחיצה על התראה). היא מעדכנת את ה-Intent של האקטיביטי ומפעילה את הטיפול בו.

```java
private void handleIntent(Intent intent) {
    if (intent != null && intent.hasExtra("roomCode")) {
        String roomCode = intent.getStringExtra("roomCode");
        if (roomCode != null && !roomCode.isEmpty()) {
            PlayerRole role = PlayerRole.host;
            if (intent.hasExtra("action") && "join".equals(intent.getStringExtra("action"))){
                role = PlayerRole.guest;
            }
            JoinOrCreateRoomFragment.newInstance(roomCode, role)
                    .show(getSupportFragmentManager(), JoinOrCreateRoomFragment.TAG);
        }
    }
}
```
בודקת אם ה-Intent מכיל קוד חדר (roomCode). אם כן, היא קובעת את תפקיד השחקן (מארח או אורח) ופותחת את הדיאלוג להצטרפות או יצירת חדר.

```java
private void init(){
    setSupportActionBar(binding.toolbar);

    // Navigation for Singleplayer
    binding.singleplayerButton.setOnClickListener(v -> {
        Intent intent = new Intent(this, SingleplayerActivity.class);
        singleplayerLauncher.launch(intent);
    });

    // Navigation for Multiplayer - requires login
    binding.multiplayerButton.setOnClickListener(v -> {
        if (FirebaseHandler.getAuth().getCurrentUser() != null){
            JoinOrCreateRoomFragment fragment = new JoinOrCreateRoomFragment();
            fragment.show(getSupportFragmentManager(), JoinOrCreateRoomFragment.TAG);
        } else {
            Toast.makeText(this, "Please sign in to play multiplayer", Toast.LENGTH_SHORT).show();
        }
    });

    // Navigation for Friend List
    binding.friendsListButton.setOnClickListener(v -> {
        Intent intent = new Intent(this, FriendListActivity.class);
        startActivity(intent);
    });

    // Easter Egg / Bonus feature
    binding.donutButton.setOnClickListener(v -> {
        Intent intent = new Intent(this, DonutActivity.class);
        startActivity(intent);
    });

    // Setup Drawer and Navigation View
    binding.navView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, binding.main, binding.toolbar, R.string.open_nav, R.string.close_nav);
    binding.main.addDrawerListener(toggle);
    toggle.syncState();
}
```
מגדירה את סרגל הכלים (Toolbar), את המאזינים לכפתורי הניווט (שחקן יחיד, מרובה שחקנים, רשימת חברים, וביצת ההפתעה), ואת התפריט הצידי.

```java
private void setupAuthStateListener() {
    authStateListener = firebaseAuth -> {
        updateUI();
        FirebaseHandler.getInstance().updateUserData();
    };
}

@Override
protected void onStart() {
    super.onStart();
    FirebaseHandler.getAuth().addAuthStateListener(authStateListener);
}

@Override
protected void onStop() {
    super.onStop();
    if (authStateListener != null) {
        FirebaseHandler.getAuth().removeAuthStateListener(authStateListener);
    }
}
```
מגדירה, רושמת ומסירה את המאזין לשינויים במצב ההתחברות של Firebase בהתאם למחזור החיים של האקטיביטי.

```java
void updateUI() {
    boolean isLoggedIn = FirebaseHandler.getInstance().getCurrentUser() != null;
    FirebaseUser user = FirebaseHandler.getInstance().getCurrentUser();

    // Update navigation menu visibility
    Menu menu = binding.navView.getMenu();
    MenuItem loginItem = menu.findItem(R.id.nav_login);
    MenuItem signupItem = menu.findItem(R.id.nav_signup);
    MenuItem logoutItem = menu.findItem(R.id.nav_logout);

    if (loginItem != null) loginItem.setVisible(!isLoggedIn);
    if (signupItem != null) signupItem.setVisible(!isLoggedIn);
    if (logoutItem != null) logoutItem.setVisible(isLoggedIn);

    // Update navigation header with user info
    if (binding.navView.getHeaderCount() > 0) {
        NavHeaderBinding headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));

        headerBinding.navHeaderTextViewName
                .setText(user != null ? user.getDisplayName() : "Not Logged In");
        headerBinding.navHeaderTextViewEmail
                .setText(user != null ? user.getEmail() : "");

        ImageView imageView = headerBinding.navHeaderImageView;
        if (user != null) {
            // Fetch additional user data (like profile image) from the database
            FirebaseHandler.getInstance().getUserRef().get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    User userData = task.getResult().getValue(User.class);
                    if (userData != null && userData.getProfileImageBase64() != null) {
                        Bitmap imageBitMap = ImageUtils.base64ToBitmap(userData.getProfileImageBase64());
                        imageView.setImageBitmap(imageBitMap);
                    } else {
                        imageView.setImageResource(R.drawable.ic_person);
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_person);
                }
            });
        } else {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }
}
```
מעדכנת את הניראות של פריטי התפריט (כניסה/הרשמה מול התנתקות) ואת פרטי המשתמש (שם, אימייל ותמונה) בראש התפריט הצידי על סמך המשתמש המחובר.

```java
private boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.nav_logout) {
        FirebaseHandler.getInstance().signOut();
    } else if (id == R.id.nav_login) {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.show(getSupportFragmentManager(), "LoginFragment");
    } else if (id == R.id.nav_signup) {
        SignUpFragment signUpFragment = new SignUpFragment();
        signUpFragment.show(getSupportFragmentManager(), "SignUpFragment");
    }

    binding.main.closeDrawer(GravityCompat.START);
    return true;
}
```
מטפלת בלחיצות על פריטים בתפריט הצידי, כמו התנתקות או פתיחת דיאלוגים של התחברות והרשמה.

```java
private void askNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
```
מבקשת מהמשתמש הרשאת התראות (עבור אנדרואיד 13 ומעלה) כדי שיוכל לקבל הזמנות למשחק.

<a id="loginfragment"></a>
#### `public class LoginFragment extends DialogFragment`
תפקיד המחלקה: `DialogFragment` המספק ממשק התחברות למשתמשים קיימים. הוא מטפל באימות מול Firebase, דיווח על שגיאות ועדכון ה-FCM token של המשתמש לאחר כניסה מוצלחת.

שדות המחלקה:
```java
/** View binding for the fragment layout. */
private FragmentLoginBinding binding;

/** Tag used for logging. */
private static final String TAG = "LoginFragment";

/** Input field for user email. */
private EditText ETEmail;
/** Input field for user password. */
private EditText ETPassword;
```

תכונות המחלקה: אין

פעולות המחלקה:

```java
private void init(){
    Button loginButton = binding.loginButton;
    ETPassword = binding.ETPassword;
    ETEmail = binding.ETEmail;

    loginButton.setOnClickListener(this::loginUser);
}
```
מאתחלת את רכיבי ה-UI ומגדירה מאזין לכפתור ההתחברות.

```java
private void loginUser(View view){
    String email = ETEmail.getText().toString();
    String password = ETPassword.getText().toString();
    
    if (email.isEmpty() || password.isEmpty()){
        Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
        return;
    }
    
    @SuppressWarnings("deprecation")
    ProgressDialog pd = new ProgressDialog(requireContext());
    pd.setTitle("Connecting");
    pd.setMessage("Logging in...");
    pd.show();
    
    FirebaseHandler.getAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity(), task -> {
                String toastMessage;
                pd.dismiss();
                if (task.isSuccessful()){
                    Log.i(TAG, "signInWithEmail:success");
                    
                    // Update FCM Token on successful login for push notifications
                    updateFcmToken();
                    
                    toastMessage = "User logged in successfully";
                    dismiss();
                } else {
                    // Map Firebase exceptions to user-friendly messages
                    toastMessage = switch (task.getException()){
                        case FirebaseAuthInvalidUserException ignored        -> "User does not exist";
                        case FirebaseAuthInvalidCredentialsException ignored -> "Invalid Password";
                        case FirebaseNetworkException ignored                -> "Network Error. Please check your connection";
                        case null, default                                   -> "An error occurred. Please try again later";
                    };
                }
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
            });
}
```
מנסה לבצע כניסה באמצעות Firebase Auth. היא מאמתת שכל השדות מלאים, מציגה תיבת התקדמות, ומטפלת בשגיאות אימות נפוצות (כמו משתמש לא קיים או סיסמה שגויה) עם הודעות ידידותיות למשתמש.

```java
private void updateFcmToken() {
    FirebaseHandler.getMessaging().getToken().addOnCompleteListener(task -> {
        if (task.isSuccessful() && task.getResult() != null) {
            String token = task.getResult();
            FirebaseHandler.getInstance().getUserRef().child("fcmToken").setValue(token)
                    .addOnSuccessListener(aVoid -> {
                        // Refresh local user data to include the new token
                        FirebaseHandler.getInstance().updateUserData();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token", e));
        } else {
            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
        }
    });
}
```
משיגה את ה-FCM token העדכני של המכשיר ושומרת אותו במסד הנתונים תחת המשתמש המחובר, מה שמאפשר לו לקבל הזמנות למשחק כהתראות.

<a id="signupfragment"></a>
#### `public class SignUpFragment extends DialogFragment`
תפקיד המחלקה: `DialogFragment` המספק ממשק הרשמה למשתמשים חדשים. הוא מטפל ביצירת חשבון ב-Firebase Authentication, בחירת תמונת פרופיל מהגלריה, ושמירת כל נתוני המשתמש (כולל ה-FCM token) במסד הנתונים בזמן אמת.

שדות המחלקה:
```java
/** View binding for the fragment layout. */
private FragmentSignUpBinding binding;

/** Tag used for logging. */
private static final String TAG = "SignUpFragment";

/** View for displaying the selected profile image. */
private ImageView IVProfileImage;
/** Input field for the display name. */
private TextInputEditText ETDisplayName;
/** Input field for the email address. */
private TextInputEditText ETEmail;
/** Input field for the password. */
private TextInputEditText ETPassword;
/** Uri of the profile image selected from the gallery. */
private Uri selectedImageUri;

/** Launcher for the system photo picker. */
private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                IVProfileImage.setImageURI(uri);
            } else {
                Log.d(TAG, "No media selected");
            }
        });
```

תכונות המחלקה: אין

פעולות המחלקה:

```java
private void init() {
    IVProfileImage = binding.IVProfileImage;
    Button btnSelectImage = binding.btnSelectImage;
    ETDisplayName = binding.ETDisplayName;
    ETEmail = binding.ETEmail;
    ETPassword = binding.ETPassword;
    Button signup_button = binding.signupButton;

    btnSelectImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build()));

    signup_button.setOnClickListener(v -> createUser());
}
```
מאתחלת את רכיבי המסך ומגדירה מאזינים לבחירת תמונה (באמצעות Photo Picker) ולתהליך ההרשמה.

```java
private void createUser() {
    String displayName = Objects.requireNonNull(ETDisplayName.getText()).toString().trim();
    String email = Objects.requireNonNull(ETEmail.getText()).toString().trim();
    String password = Objects.requireNonNull(ETPassword.getText()).toString().trim();

    if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        return;
    }

    ProgressDialog pd = new ProgressDialog(requireContext());
    pd.setTitle("Connecting");
    pd.setMessage("Creating User...");
    pd.setCancelable(false);
    pd.show();

    FirebaseHandler.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener((requireActivity()), task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseHandler.getAuth().getCurrentUser();
                    if (user != null) {
                        String base64Image = null;
                        if (selectedImageUri != null) {
                            try {
                                // Convert selected image to Base64 for database storage
                                base64Image = ImageUtils.uriToBase64(selectedImageUri, requireContext());
                            } catch (IOException e) {
                                Log.e(TAG, "Error converting image to Base64", e);
                                // Use default person icon if conversion fails
                                base64Image = ImageUtils.bitmapToBase64(BitmapFactory.decodeResource(getResources(), R.drawable.ic_person));
                            }
                        }
                        updateProfile(user, displayName, base64Image, pd);
                    }
                } else {
                    pd.dismiss();
                    String toastMessage = switch (task.getException()){
                        case FirebaseAuthWeakPasswordException ignored -> "Password is too weak";
                        case FirebaseAuthInvalidCredentialsException ignored -> "Invalid Email Address";
                        case FirebaseAuthUserCollisionException ignored -> "User already exists";
                        case FirebaseNetworkException ignored -> "Network Error. Please check your connection";
                        case null, default -> "An error occurred. Please try again later";
                    };
                    Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            });
}
```
מנהלת את תהליך יצירת המשתמש: אימות קלטים, יצירת החשבון ב-Firebase Auth, המרת התמונה הנבחרת לפורמט Base64 לשמירה יעילה, וטיפול בשגיאות נפוצות.

```java
private void updateProfile(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
    pd.setMessage("Updating Profile...");
    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build();

    user.updateProfile(profileUpdates)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fetchFcmTokenAndSaveUser(user, displayName, base64Image, pd);
                } else {
                    pd.dismiss();
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
}
```
מעדכנת את הפרופיל של המשתמש ב-Firebase Authentication עם שם התצוגה שנבחר.

```java
private void fetchFcmTokenAndSaveUser(FirebaseUser user, String displayName, String base64Image, ProgressDialog pd) {
    pd.setMessage("Fetching FCM Token...");
    FirebaseHandler.getMessaging().getToken().addOnCompleteListener(task -> {
        String token = null;
        if (task.isSuccessful()) {
            token = task.getResult();
        } else {
            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
        }
        saveUserToDatabase(user, displayName, base64Image, token, pd);
    });
}
```
משיגה את ה-FCM token של המכשיר לפני שמירת רשומת המשתמש המלאה, כדי להבטיח שהמשתמש מוכן לקבל התראות מיד עם סיום ההרשמה.

```java
private void saveUserToDatabase(FirebaseUser user, String displayName, String base64Image, String fcmToken, ProgressDialog pd) {
    pd.setMessage("Saving User Data...");
    User newUser = new User(user.getUid(), displayName, user.getEmail(), base64Image, fcmToken);

    DatabaseReference userRef = FirebaseHandler.getInstance().getRootRef().child("users").child(user.getUid());
    userRef.setValue(newUser)
            .addOnCompleteListener(task -> {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(requireContext(), "User created successfully!", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MainActivity mainActivity){
                        mainActivity.updateUI();
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                }
            });
}
```
יוצרת אובייקט `User` מלא ושומרת אותו במסד הנתונים של Firebase. בסיום מוצלח, היא מעדכנת את ממשק המשתמש במסך הראשי וסוגרת את הדיאלוג.

<a id="friendlistactivity"></a>
#### `public class FriendListActivity extends AppCompatActivity`

תפקיד המחלקה: ניהול רשימת החברים של המשתמש. מאפשרת חיפוש משתמשים לפי אימייל, הוספת חברים חדשים, צפייה ברשימה הקיימת ושליחת הזמנות למשחקים מרובי משתתפים.

שדות המחלקה:
```java
/** Tag used for logging. */
private static final String TAG = "FriendListActivity";
/** View binding for the activity layout. */
private ActivityFriendlistBinding binding;
/** Adapter for the friends list RecyclerView. */
private FriendAdapter adapter;
/** Local list of friend objects fetched from the database. */
private final List<User> friendsList = new ArrayList<>();
/** Singleton instance of the FirebaseHandler. */
private FirebaseHandler firebaseHandler;
/** Cached snapshot of all users for searching purposes. */
private DataSnapshot usersSnapshot;
```

תכונות המחלקה:
``` java
- List<User> friendsList
- FriendAdapter adapter
```

פעולות המחלקה:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_friendlist);
    firebaseHandler = FirebaseHandler.getInstance();

    setupRecyclerView();
    setupClickListeners();
    setupSearchInput();

    loadUsers();
}
```
מאתחלת את ה-Data Binding, את המאזינים לרכיבי ה-UI (חיפוש, כפתורים), ומפעילה את טעינת המשתמשים והחברים.

```java
private void loadUsers() {
    firebaseHandler.getRootRef().child("users").get().addOnSuccessListener(snapshot -> {
        usersSnapshot = snapshot;
    }).addOnFailureListener(e -> Log.e(TAG, "Failed to load users", e));
    loadFriends();
}
```
טוענת את רשימת כל המשתמשים הרשומים (לצורך חיפוש) ולאחר מכן טוענת את רשימת החברים הספציפית של המשתמש.

```java
private void showInviteDialog(User friend) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Invite " + friend.getDisplayName());
    builder.setMessage("Enter room code to invite them to play:");

    final EditText input = new EditText(this);
    input.setHint("Room Code");
    builder.setView(input);

    builder.setPositiveButton("Send", (dialog, which) -> {
        String roomCode = input.getText().toString().trim();
        if (!roomCode.isEmpty()) {
            sendInvitation(friend, roomCode);
            // After sending, transition the host to the MainActivity which will open the room
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("roomCode", roomCode);
            intent.putExtra("action", "host");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Room code cannot be empty", Toast.LENGTH_SHORT).show();
        }
    });
    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

    builder.show();
}
```
מציגה דיאלוג המאפשר למשתמש להזין קוד חדר ולהזמין חבר למשחק. לאחר השליחה, המשתמש מועבר למסך הראשי במצב מארח.

```java
private void sendInvitation(User friend, String roomCode) {
    String currentUserId = firebaseHandler.getCurrentUserId();
    User currentUser = firebaseHandler.getUserData();
    
    if (currentUserId == null || currentUser == null) {
        Toast.makeText(this, "Error: You must be logged in", Toast.LENGTH_SHORT).show();
        return;
    }

    DatabaseReference invitationsRef = firebaseHandler.getRootRef()
            .child("invitations")
            .child(friend.getUid())
            .push();

    Map<String, Object> invitation = new HashMap<>();
    invitation.put("senderId", currentUserId);
    invitation.put("senderName", currentUser.getDisplayName());
    invitation.put("message", "Join my Boggle game!");
    invitation.put("roomCode", roomCode);
    invitation.put("timestamp", ServerValue.TIMESTAMP);

    invitationsRef.setValue(invitation)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Invitation sent to " + friend.getDisplayName(), Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to send invitation", Toast.LENGTH_SHORT).show());
}
```
יוצרת רשומת הזמנה חדשה ב-Firebase תחת המזהה של החבר המוזמן. זה יפעיל את ה-Cloud Function שישלח הודעת דחיפה לחבר.

```java
private void setupSearchInput() {
    binding.friendEmailInput.addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (usersSnapshot == null) return;
            
            String query = s.toString().toLowerCase();
            List<User> filteredList = new ArrayList<>();
            for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                User user = userSnapshot.getValue(User.class);
                if (user != null && user.getEmail().toLowerCase().contains(query)) {
                    if (!user.getUid().equals(firebaseHandler.getCurrentUserId())) {
                        filteredList.add(user);
                    }
                }
            }
            filteredList.sort((u1, u2) -> u1.getDisplayName().compareToIgnoreCase(u2.getDisplayName()));
            binding.setSearchedUser(filteredList.isEmpty() ? null : filteredList.get(0));
        }
        // ...
    });
}
```
מגדירה מאזין לשינויי טקסט בשדה החיפוש שמסנן את רשימת המשתמשים בזמן אמת ומציג את התוצאה הראשונה המתאימה.

```java
private void loadFriends() {
    DatabaseReference userRef = firebaseHandler.getUserRef();
    if (userRef == null) return;

    DatabaseReference friendsRef = userRef.child("friends");
    friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            friendsList.clear();
            if (!snapshot.exists()) {
                adapter.submitList(new ArrayList<>(friendsList));
                return;
            }
            for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                String friendId = friendSnapshot.getKey();
                if (friendId != null) {
                    fetchFriendData(friendId);
                }
            }
        }
        // ...
    });
}
```
טוענת את רשימת מזהי החברים של המשתמש הנוכחי ומפעילה שליפה של הנתונים המלאים עבור כל אחד מהם.

<a id="friendadapter"></a>
#### `public class FriendAdapter extends ListAdapter<User, FriendAdapter.FriendViewHolder>`

תפקיד המחלקה: אדפטר ל-RecyclerView המציג את רשימת החברים. הוא אחראי על קישור נתוני המשתמשים (User objects) לתצוגה הגרפית ועל טיפול בלחיצות על כפתור ההזמנה. הוא משתמש ב-ListAdapter ו-DiffUtil לעדכונים יעילים.

שדות המחלקה:
```java
/** Callback for when the invite button is clicked for a specific friend. */
private final OnInviteClickListener inviteClickListener;
```

ממשק האזנה:
```java
public interface OnInviteClickListener {
    void onInviteClick(User friend);
}
```

תכונות המחלקה: אין

פעולות המחלקה:

```java
@Override
public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
    User friend = getItem(position);
    holder.binding.setFriend(friend);
    
    holder.binding.inviteButton.setOnClickListener(v -> {
        if (inviteClickListener != null) {
            inviteClickListener.onInviteClick(friend);
        }
    });
    
    holder.binding.executePendingBindings();
}
```
מקשרת בין אובייקט המשתמש ל-ViewHolder, מגדירה את המאזין לכפתור ההזמנה ומבצעת את הקישור (Binding) באופן מיידי למניעת ריצודים.

מחלקה פנימית: `private static class UserDiffCallback extends DiffUtil.ItemCallback<User>`
מחלקה פנימית המשמשת להשוואה יעילה בין רשימות חברים לצורך עדכון חלקי של ה-RecyclerView במקום ריענון של כל הרשימה. בודקת זהות לפי אימייל ותוכן לפי שם ותמונה.

### חבילה: ui.game.multiplayer

<a id="multiplayeractivity"></a>
#### `public class MultiplayerActivity extends AppCompatActivity`
תפקיד המחלקה: האקטיביטי המארחת את חוויית המשחק מרובת המשתתפים. היא מנהלת את המעבר בין הלובי למשחק הפעיל ומנקה את נתוני החדר ב-Firebase בסיום.

שדות המחלקה:
```java
/** Tag used for logging. */
public static final String TAG = "MultiplayerActivity";
/** Intent extra key for the room code. */
public static final String ARG_ROOM_CODE = "room_code";
/** Intent extra key for the player's role (HOST or GUEST). */
public static final String ARG_PLAYER_ROLE = "player_role";

/** View binding for the activity layout. */
private ActivityMultiplayerBinding binding;

/** The code of the current multiplayer room. */
private String roomCode;
/** The role of the local player in this session. */
private PlayerRole playerRole;
```

תכונות המחלקה:
``` java
- String roomCode
- PlayerRole playerRole
```

פעולות המחלקה:
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Enable edge-to-edge display
    EdgeToEdge.enable(this);
    
    binding = ActivityMultiplayerBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // Adjust layout for system bars
    ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });
    
    // Retrieve room details from the starting Intent
    if (getIntent() != null) {
        playerRole = getIntent().getSerializableExtra(ARG_PLAYER_ROLE, PlayerRole.class);
        roomCode = getIntent().getStringExtra(ARG_ROOM_CODE);
    }

    // Initialize by showing the LobbyFragment
    if (savedInstanceState == null && roomCode != null && playerRole != null) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(binding.main.getId(), LobbyFragment.newInstance(roomCode, playerRole), LobbyFragment.TAG)
                .commit();
    }
}
```
מאתחלת את ה-Activity ומציגה את ה-LobbyFragment.

```java
public void startGame(){
    if (roomCode != null) {
        getSupportFragmentManager().beginTransaction()
                .replace(binding.main.getId(), MultiplayerGameFragment.newInstance(playerRole, roomCode), MultiplayerGameFragment.TAG)
                .commit();
    }
}
```
עוברת למסך המשחק הפעיל (MultiplayerGameFragment).

```java
public void showGameResults(HashMap<String, String> solutions, HashMap<User, ArrayList<String>> playersWords) {
    MultiplayerOnGameEndFragment fragment = MultiplayerOnGameEndFragment.newInstance(solutions, playersWords);
    fragment.show(getSupportFragmentManager(), MultiplayerOnGameEndFragment.TAG);
}
```
מציגה את תוצאות המשחק הסופיות בדיאלוג.

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    // Cleanup: remove the player from the room or delete the room if empty
    if (roomCode != null) {
        String userId = FirebaseHandler.getInstance().getCurrentUserId();
        if (userId != null) {
            DatabaseReference roomRef = FirebaseHandler.getDatabase().getReference("rooms").child(roomCode);
            // Remove local player from the Firebase list
            roomRef.child("players").child(userId).removeValue().addOnCompleteListener(task -> {
                // Check if any players remain; if not, remove the entire room node
                roomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            // Housekeeping: remove empty room node
                            roomRef.removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }
    }
}
```
מסירה את השחקן מהחדר בשרת ומנקה חדרים ריקים.

<a id="joinorcreateroomfragment"></a>
#### `public class JoinOrCreateRoomFragment extends DialogFragment`
תפקיד המחלקה: דיאלוג המאפשר למשתמש ליצור חדר משחק חדש או להצטרף לחדר קיים באמצעות קוד.

שדות המחלקה:
``` java
/** Tag used for identifying this fragment in the FragmentManager. */
public static final String TAG = "JoinOrCreateRoomFragment";
/** Key for the initial room code passed in arguments. */
private static final String ARG_INITIAL_ROOM_CODE = "initial_room_code";
/** Key for the initial player role passed in arguments. */
private static final String ARG_INITIAL_PLAYER_ROLE = "initial_player_role";

/** View binding for fragment layout. */
private FragmentJoinOrCreateRoomBinding binding;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
public static JoinOrCreateRoomFragment newInstance(String roomCode, PlayerRole playerRole)
```
יוצרת מופע עם נתונים התחלתיים.

``` java
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
```
מאתחלת את הממשק ומטפלת בבקשות הצטרפות ישירות.

``` java
private void createRoom(View view)
```
יוצרת חדר חדש ועוברת למסך המולטיפלייר כמארח.

``` java
private void joinRoom(View view)
```
בודקת קיום חדר בשרת ומצטרפת כאורח.

``` java
private Intent makeIntent(PlayerRole role)
```
פונקציית עזר לבניית Intent המעבר.

<a id="lobbyfragment"></a>
#### `public class LobbyFragment extends Fragment`
תפקיד המחלקה: מציגה את חדר ההמתנה (Lobby), רשימת השחקנים המחוברים ומאפשרת למארח להתחיל את המשחק.

שדות המחלקה:
``` java
/** Tag used for identifying this fragment. */
public static final String TAG = "LobbyFragment";

/** View binding for fragment layout. */
private FragmentLobbyBinding binding;
/** Adapter for the player list RecyclerView. */
private PlayerAdapter playerAdapter;
/** Local list of users currently in the lobby. */
private final List<User> playerList = new ArrayList<>();

/** The unique code for the current game room. */
private String roomCode;
/** The role of the local player (HOST or GUEST). */
private PlayerRole playerRole;
/** The local player's user data. */
private User player;
/** Reference to the room's node in Firebase Realtime Database. */
private DatabaseReference roomRef;
/** Listener for player list and game start updates in Firebase. */
private ValueEventListener playerListener;
```

תכונות המחלקה:
``` java
- List<User> playerList
- String roomCode
```

פעולות המחלקה:
``` java
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
```
רושמת את השחקן בחדר ומגדירה את התצוגה.

``` java
private void listenForPlayers()
```
מעדכנת את הרשימה ומזהה מתי המשחק מתחיל בשרת.

``` java
private void startGame(View view)
```
מעדכנת בשרת שהמשחק התחיל (למארח בלבד).

<a id="multiplayergamefragment"></a>
#### `public class MultiplayerGameFragment extends Fragment`
תפקיד המחלקה: ניהול לוגיקת המשחק בזמן אמת במצב מרובה משתתפים, כולל סנכרון הלוח והמילים מול Firebase.

שדות המחלקה:
``` java
/** Tag used for logging and fragment identification. */
public static final String TAG = "MultiplayerGameFragment";

/** View binding for the fragment layout. */
private FragmentMultiplayerGameBinding binding;
/** The code of the current multiplayer room. */
private String roomCode;
/** The role of the local player (HOST or GUEST). */
private PlayerRole playerRole;

/** Reference to the room node in Firebase Realtime Database. */
private DatabaseReference roomRef;
/** Listener for the game board string in Firebase. */
private ValueEventListener boardListener;
/** Listener for the game end flag in Firebase. */
private ValueEventListener gameEndListener;
/** Listener to detect if the room is deleted from outside. */
private ChildEventListener gameDestroyedListener;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
```
המארח מייצר לוח; האורחים ממתינים לסנכרון הלוח.

``` java
private void listenForGameEnd()
```
מפעילה איסוף תוצאות כשדגל הסיום מתעדכן.

``` java
private void setupGame(BoggleGame game)
```
מסנכרנת מילים שנמצאו לשרת ומעדכנת סיום זמן.

``` java
private void collectResultsAndFinish()
```
אוספת את כל המילים של כלל השחקנים מה-Database.

<a id="multiplayerongameendfragment"></a>
#### `public class MultiplayerOnGameEndFragment extends DialogFragment`
תפקיד המחלקה: דיאלוג המציג את תוצאות המשחק הסופיות והשוואת המילים בין השחקנים.

שדות המחלקה:
``` java
/** Tag for identifying the fragment. */
public static final String TAG = "MultiplayerOnGameEndFragment";
/** Argument key for the map of players to their found words. */
public static final String ARG_PLAYERS_WORDS = "playersWords";
/** Argument key for the map of solution words to their paths. */
public static final String ARG_SOLUTIONS = "solutions";

/** View binding for the fragment layout. */
private FragmentMultiplayerOnGameEndBinding binding;
/** Adapter for displaying players' words in a list. */
private PlayersWordsAdapter playersWordsAdapter;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
public static MultiplayerOnGameEndFragment newInstance(HashMap<String, String> solutions, HashMap<User, ArrayList<String>> playersWords)
```
מעבירה את מפות הפתרונות והמילים שנמצאו.

``` java
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
```
מאתחלת את רשימת התוצאות המורחבת.

<a id="playeradapter"></a>
#### `public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>`
תפקיד המחלקה: אדפטר להצגת פרטי השחקנים (שם ותמונה) בלובי ההמתנה.

שדות המחלקה:
``` java
/** List of users currently in the lobby. */
private final List<User> playerList;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position)
```
מבצעת Data Binding של נתוני השחקן לתצוגה.

<a id="playerswordsadapter"></a>
#### `public class PlayersWordsAdapter extends RecyclerView.Adapter<PlayersWordsAdapter.ViewHolder>`
תפקיד המחלקה: אדפטר מורכב המציג את מילות השחקנים בסיום המשחק עם גלילה מסונכרנת.

שדות המחלקה:
``` java
/** List of players whose words are being displayed. */
private final List<User> players;
/** Map of each user to their list of found words. */
private final HashMap<User, ArrayList<String>> playersWords;
/** Map of all valid words on the board to their paths. */
private final HashMap<String, String> solutions;
/** Set of words that were found by more than one player. */
private final Set<String> commonWords;
/** Inflater for creating item views. */
private final LayoutInflater inflater;
/** Listener for word click events. */
private final WordsAdapter.OnWordClickListener onWordClickListener;

/** Set of child RecyclerViews to synchronize scrolling across. */
private final Set<RecyclerView> childRecyclerViews = Collections.newSetFromMap(new WeakHashMap<>());
/** Current horizontal scroll position to maintain consistency. */
private int currentScrollX = 0;

/**
 * Scroll listener attached to child RecyclerViews to synchronize their horizontal movement.
 */
private final RecyclerView.OnScrollListener syncScrollHandler = new RecyclerView.OnScrollListener()
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
private Set<String> findCommonWords(HashMap<User, ArrayList<String>> playersWords)
```
מזהה כפילויות במילים בין השחקנים.

``` java
public void onBindViewHolder(@NonNull ViewHolder holder, int position)
```
בונה רשימת מילים פנימית לכל שחקן ומחברת אותה למנגנון סנכרון הגלילה.

### חבילה: ui.game.singleplayer

<a id="singleplayeractivity"></a>
#### `public class SingleplayerActivity extends AppCompatActivity`
תפקיד המחלקה: האקטיביטי המארחת את חוויית המשחק לשחקן יחיד. היא מנהלת את לוגיקת המשחק, תוצאות המשחק בסיום והעלאתן ל-Firebase.

שדות המחלקה:
```java
/** Tag used for logging and debugging purposes. */
private static final String TAG = "SingleplayerActivity";

/** View binding instance for accessing layout components. */
ActivitySingleplayerBinding binding;

/** Key for passing the final score in an Intent result. */
public static final String EXTRA_SCORE = "extra_score";

/** The underlying game engine instance. */
private BoggleGame game;
/** Flag to track if the current game session has concluded. */
private boolean isGameEnded = false;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
protected void onCreate(Bundle savedInstanceState) {
    // ...
    game.addOnGameEndListener(() ->
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                isGameEnded = true;

                Intent data = new Intent();
                data.putExtra(EXTRA_SCORE, game.getScore());
                setResult(RESULT_OK, data);

                showGameEndDialog();
                uploadGameResults(game);
            }));

    game.addOnWordFoundListener(word -> {
        if (word.equalsIgnoreCase("donut")){
            game.stopTimer();
            Intent intent = new Intent(this, DonutActivity.class);
            startActivity(intent);
        }
    });
    // ...
}
```
מאתחלת את המשחק, מגדירה אירועים לסיום המשחק וגילוי מילות סתר (Easter Egg).

``` java
private void uploadGameResults(BoggleGame game) {
    FirebaseHandler handler = FirebaseHandler.getInstance();
    DatabaseReference userRef = handler.getUserRef();

    if (userRef != null) {
        GameResult result = new GameResult(
                game.getScore(), game.getFoundWords().size(),
                game.getSolutions().size(), game.getMaxScore(),
                game.getFoundWords(), String.valueOf(game.getBoard())
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        userRef.child("games").child(timestamp).setValue(result);
    }
}
```
מעלה את תוצאות המשחק למסד הנתונים בענן עבור המשתמש הנוכחי.

``` java
private void showGameEndDialog() {
    if (isDestroyed()) return;
    try {
        SingleplayerOnGameEndFragment fragment = SingleplayerOnGameEndFragment.newInstance(
                game.getSolutions().toMap(), game.getFoundWords(), game.getScore()
        );
        fragment.setOnWordClickListener((word, path) -> binding.boggleView.showSolution(path));

        getSupportFragmentManager().beginTransaction()
                .add(fragment, SingleplayerOnGameEndFragment.TAG)
                .commitAllowingStateLoss();
    } catch (Exception e) {
        Log.e(TAG, "Failed to show game end dialog", e);
    }
}
```
מציגה חלון סיכום עם המילים שנמצאו ואלו שפוספסו.

<a id="singleplayerongameendfragment"></a>
#### `public class SingleplayerOnGameEndFragment extends DialogFragment`
תפקיד המחלקה: דיאלוג המוצג בסיום משחק יחיד. מציג את הניקוד, המילים שנמצאו וכל הפתרונות האפשריים.

שדות המחלקה:
```java
/** Tag used for identifying the fragment. */
public static final String TAG = "SingleplayerOnGameEndFragment";

/** Key for the solutions argument. */
private static final String ARG_SOLUTIONS = "solutions";
/** Key for the found words argument. */
private static final String ARG_FOUND_WORDS = "found_words";
/** Key for the score argument. */
private static final String ARG_SCORE = "score";

/** View binding for the fragment layout. */
private FragmentSingleplayerOnGameEndBinding binding;
/** Callback listener for when a word is clicked in the list. */
private OnWordClickListener listener;
```

תכונות המחלקה: אין

פעולות המחלקה:
``` java
public static SingleplayerOnGameEndFragment newInstance(Map<String, String> solutions, List<String> foundWords, int score) {
    SingleplayerOnGameEndFragment fragment = new SingleplayerOnGameEndFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_SOLUTIONS, new HashMap<>(solutions));
    args.putStringArrayList(ARG_FOUND_WORDS, new ArrayList<>(foundWords));
    args.putInt(ARG_SCORE, score);
    fragment.setArguments(args);
    return fragment;
}
```
יוצרת מופע של הדיאלוג עם הנתונים של המשחק שהסתיים.

``` java
@NonNull
@Override
public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    // ...
    if (solutions != null && foundWords != null) {
        binding.wordsList.setAdapter(new WordsAdapter(solutions, foundWords, (word, path) -> {
            if (listener != null) {
                listener.onWordClick(word, path);
            }
            dismiss();
        }));
    }
    // ...
    return dialog;
}
```
מאתחלת את רשימת המילים ומקשרת אותה ל-Adapter שיציג את הפתרונות. (הקוד ב-onCreateDialog מחליף את onViewCreated).

``` java
public void setOnWordClickListener(OnWordClickListener listener) {
    this.listener = listener;
}
```
מאפשרת להגדיר מאזין ללחיצות על מילים, כדי שניתן יהיה להציג את המסלול שלהן על הלוח.

### חבילה: ui.shared

<a id="wordsadapter"></a>
#### `public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder>`
תפקיד המחלקה: אדפטר המשמש להצגת רשימת מילים, בדרך כלל במסכי סיכום המשחק. הוא מדגיש מילים שנמצאו על ידי השחקן, ואופציונלית מילים משותפות (שנמצאו על ידי מספר שחקנים), ומאפשר לחיצה על מילה כדי להציג את המסלול שלה על הלוח.

שדות המחלקה:
```java
/** Map of all valid words on the board to their hex-encoded paths. */
private final Map<String, String> solutions;
/** List of words found by the current player. */
private final List<String> playerWords;
/** Set of words found by more than one player (for multiplayer). */
private final Set<String> commonWords;
/** Callback listener for word click events. */
private final OnWordClickListener listener;
```

תכונות המחלקה: אין

פעולות המחלקה:
```java
public WordsAdapter(Map<String, String> solutions, List<String> playerWords, Set<String> commonWords, OnWordClickListener listener)
```
בנאי מלא לאדפטר, מקבל את הפתרונות, המילים שנמצאו, מילים משותפות ומאזין ללחיצות. משמש לתוצאות משחק מרובה משתתפים.

```java
public WordsAdapter(Map<String, String> solutions, List<String> playerWords, OnWordClickListener listener)
```
בנאי פשוט ללא מילים משותפות. משמש לתוצאות משחק לשחקן יחיד.

```java
public void onBindViewHolder(@NonNull ViewHolder holder, int position)
```
מקשר את הנתונים לתצוגה של פריט בודד. ממיין את הפתרונות אלפביתית, וצובע את המילים בהתאם למצבן: אדום למילה שנמצאה על ידי אחרים (משותפת), ירוק למילה שנמצאה על ידי השחקן הנוכחי, ושחור למילה שפוספסה. מגדיר גם את מאזין הלחיצות על הפריט.

### חבילה: ui

<a id="donutactivity"></a>
#### `public class DonutActivity extends AppCompatActivity`
תפקיד המחלקה: מסך (Activity) המציג דונאט תלת-ממדי מסתובב (ביצת הפתעה). מנהל את אירועי המגע (Touch Events) לסיבוב הדונאט ומחוות צביטה (Pinch-to-zoom) לשינוי הגודל/מרחק.

שדות המחלקה:
```java
/** The renderer responsible for drawing the 3D donut on the surface. */
private DonutRenderer renderer;
/** The SurfaceView where the donut is drawn. */
private SurfaceView surfaceView;
/** Detector for pinch-to-zoom gestures. */
private ScaleGestureDetector scaleDetector;

/** ID of the pointer currently being tracked for rotation. */
private int activePointerId = INVALID_POINTER_ID;
/** Last recorded X coordinate of the touch event. */
private float lastTouchX = 0;
/** Last recorded Y coordinate of the touch event. */
private float lastTouchY = 0;
```

תכונות המחלקה: אין

פעולות המחלקה:
```java
protected void onCreate(@Nullable Bundle savedInstanceState)
```
מאתחלת את ה-SurfaceView, את מזהה מחוות הצביטה, ורושמת Callback למחזור החיים של המשטח כדי להתחיל ולהפסיק את הרינדור מול `DonutRenderer`.

```java
public boolean onTouchEvent(MotionEvent event)
```
מטפלת באירועי מגע של המשתמש. מעבירה אירועים לזיהוי שינוי גודל (ScaleDetector), ומטפלת בסיבוב הדונאט על ידי חישוב ההפרש במיקום האצבע (dx, dy) לעדכון הזוויות ברנדרר. תומכת במספר אצבעות למניעת קפיצות כשמחליפים אצבע.

מחלקה פנימית: `private class OnScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener`
מאזינה לאירועי צביטה (זום).
```java
public boolean onScale(@NonNull ScaleGestureDetector detector)
```
מעדכנת את מרחק הדונאט (Zoom) ברנדרר בהתאם ליחס המתיחה (ScaleFactor).

### חבילה: views

<a id="boggleview"></a>
#### `public class BoggleView extends LinearLayout`
תפקיד המחלקה: רכיב UI מותאם אישית (Custom View) המייצג את לוח המשחק של בוגל ואת הממשק ההיקפי שלו (כפתור אישור, מד זמן, תצוגת ניקוד ומילה).

תכונות המחלקה:
``` java
- BoggleGame game
- TextView[] cells
- TextView lastSelected
- BadgeDrawable hintBadge
- GameMode gameMode
```

פעולות המחלקה:
```java
private void initView()
```
מאתחלת את הרכיב הגרפי, מחברת אותו ל-XML המותאם אישית שלו, מגדירה את מערך הקוביות (16 תאים) ומשייכת לכל תא וכפתור את המאזין המתאים לו. בסביבת שחקן יחיד היא גם מתחילה את המשחק אוטומטית.

```java
private void setupUI()
```
מסנכרנת את מצב רכיב ה-UI עם ה-`BoggleGame`. פורסת את האותיות על הקוביות בלוח (תוך הצגת 'Qu' במידת הצורך), מעדכנת ניקוד ומפעילה את מד הזמן בהתאם לתקתוקי הטיימר של מנוע המשחק.

```java
private void onClickSubmit(View v)
```
מטפלת בלחיצה על כפתור 'אישור מילה'. מגישה את המילה לבדיקה מול ה-`BoggleGame`, מספקת משוב למשתמש (האם המילה חוקית, קצרה מדי, או כבר נמצאה), מעדכנת את הניקוד במידה ונמצאה ומנקה את הבחירה מהלוח.

```java
public void showSolution(String path)
```
מקבלת נתיב משחק (בפורמט מחרוזת של אינדקסים) ומאירה את המילה הספציפית על הלוח באמצעות צביעת התאים הרלוונטיים בצבע מתאים (עם צבע ייעודי לתא האחרון).

```java
public void showHint()
```
צורכת רמז מסך הרמזים שזמינים למשתמש, מחפשת מילה חוקית שעדיין לא נמצאה – תוך התחשבות במסלול שהשחקן התחיל לבנות (אם קיים) – ומאירה חלק ממנה על הלוח באמצעות קריאה ל-`showSolution`.


<a id="squaretextview"></a>
#### `public class SquareTextView extends androidx.appcompat.widget.AppCompatTextView`
תפקיד המחלקה: רכיב UI של טקסט מותאם אישית השומר תמיד על יחס ממדים ריבועי (1:1). משמשת לייצוג קוביות המשחק ברשת של בוגל, כך שהן יהיו אחידות וריבועיות בכל מסך.

פעולות המחלקה:
```java
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
```
עוקפת את מתודת המדידה הסטנדרטית כדי לחשב את הגובה והרוחב של התצוגה, בודקת איזה ממד גדול יותר, ומחילה אותו על שני הצירים לקבלת ריבוע מושלם.


### חבילה: utils

<a id="imageutils"></a>
#### `public class ImageUtils`
תפקיד המחלקה: מחלקת עזר לטיפול בתמונות והמרת נתונים. היא מספקת מתודות סטטיות להמרה בין `Bitmap`, מחרוזות `Base64` (לצורך שמירה במסד נתונים), ו-`Uri`. בנוסף היא מכילה מתאם (Binding Adapter) עבור Data Binding.

שדות המחלקה:
```java
/** Tag used for logging. */
private final static String TAG = "ImageUtils";
```

תכונות המחלקה: אין

פעולות המחלקה:
```java
@BindingAdapter("imageBitmap")
static public void setImageBitmap(ImageView imageView, Bitmap bitmap)
```
מאפשרת להגדיר דרך ה-XML (Data Binding) אובייקט `Bitmap` שיוצג ב-`ImageView`. אם ה-`Bitmap` ריק (null), היא מציגה תמונת ברירת מחדל (אייקון של משתמש).

```java
static public String uriToBase64(Uri uri, Context context) throws IOException
```
מקבלת `Uri` (למשל מגלריית המכשיר), פותחת InputStream וקוראת אותו לתוך `Bitmap`, ואז קוראת לפעולת ההמרה ל-`Base64` כדי לאפשר שמירת תמונת משתמש בשרת.

```java
static public String bitmapToBase64(Bitmap bitmap)
```
ממירה אובייקט `Bitmap` למחרוזת `Base64` מקודדת בפורמט JPEG. הפונקציה משתמשת בדחיסה (70% איכות) כדי לאזן בין איכות התמונה לגודל שלה במסד הנתונים.

```java
static public Bitmap base64ToBitmap(String base64)
```
הפעולה ההפוכה: מקבלת מחרוזת `Base64` (למשל כזו שנמשכה מ-Firebase) וממירה אותה חזרה ל-`Bitmap` כדי להציג אותה בממשק המשתמש.

<a id="timer"></a>
#### `public class Timer implements Runnable`
תפקיד המחלקה: מחלקת עזר המנהלת ספירה לאחור עבור זמן המשחק. היא משתמשת ב-`Handler` כדי לרוץ על ה-Main Thread ולתזמן קריאות תקופתיות שיעדכנו את ה-UI.

שדות המחלקה:
```java
/** The total duration of the timer in milliseconds. */
private final long millisTime;
/** The system time when the timer was started or resumed. */
private final long millisTimeBegan;
/** Handler used to schedule the next periodic update on the main UI thread. */
private final Handler handler = new Handler(Looper.getMainLooper());
/** Listener to be notified when the timer reaches its duration. */
private final OnTimerEndListener onTimerEnd;
/** Listener to be notified on every tick (increment). */
private final OnTickListener onTick;
/** Flag to track if the timer has been stopped or paused. */
private boolean isStopped = false;
```

ממשקי האזנה:
```java
public interface OnTimerEndListener {
    void onTimerEnd();
}
public interface OnTickListener {
    void onTick(long elapsedTime);
}
```

תכונות המחלקה: אין

פעולות המחלקה:
```java
public Timer(long timeInMillis, OnTickListener onTick, OnTimerEndListener onTimerEnd)
```
מאתחלת טיימר עם זמן מוגדר מראש, ומאזינים (Callbacks) לעדכוני תקתוק ולסיום הזמן.

```java
@Override
public void run()
```
פעולת הליבה של הטיימר המופעלת על ידי ה-`Handler`. מחשבת את הזמן שעבר, קוראת ל-`onTick`, ואם הזמן תם קוראת ל-`onTimerEnd`. אם לא, היא מתזמנת את עצמה מחדש בצורה חכמה שמשלימה בדיוק לשנייה שלמה.

```java
public void start()
```
מתחילה (או ממשיכה) את הטיימר על ידי שליחת הקריאה ל-`Handler`.

```java
public void stop()
```
עוצרת את הטיימר באופן ידני ומנקה קריאות עתידיות הממתינות ב-`Handler`.

<a id="pointanddepth"></a>
#### `public record PointAndDepth(int screenX, int screenY, double ooz)`
תפקיד המחלקה: נתון (Record) פשוט לאחסון נקודה במישור הדו-מימדי על המסך, יחד עם ערך עומק `ooz` (One Over Z) לשימוש בחישובי תלת-ממד (למשל למיון לפי עומק ברנדור הדונאט).

<a id="quad"></a>
#### `public record Quad(Path path, @ColorInt int color, double avgOoz)`
תפקיד המחלקה: נתון (Record) פשוט המייצג פוליגון מרובע בודד (פאה של הדונאט התלת-ממדי), עם הנתיב (Path) לציירו על הקנבס, צבעו, והעומק הממוצע שלו לצורך המיון לפני הציור (Painter's Algorithm).
