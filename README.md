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

#### `public abstract class Trie<T extends Trie<T>`
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

```
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

#### `public class FirebaseHandler'
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

#### `public class User`

ץפקיד המחלקה: לשמור את המידע של המשתמש מקומית, ולבסס את הצורה שהמידע של משתמשים נשמר, במיוחד באינראקציה עם המסד נתונים. 
בגלל שכל מה שהמחלקה עושה היא לשמור מידע,והיא לא תלוייה בשום דבר אחר, היא גם נקראת POJO (Plain Old Java Object)

שדות המחלקה:
``` java
/** The user's unique ID. */
private String uid;
/** The user's chosen display name. */
private String displayName;
/** The user's email address, used for authentication and identification. */
private String email;
/** A Base64 encoded string of the user's profile picture. */
private String profileImageBase64;
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

