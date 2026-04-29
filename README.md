<h1 align=center>מבוא</h1>

## רקע

## מחקר

## ניהול נתונים בפרוייקט

<h1 align=center>מבנה / ארכיטקטורה</h1>

## קבצי הפרוייקט

| קבצי קוד | קבצי משאב |
| :---: | :---: |
| ![code files](https://github.com/user-attachments/assets/e715ff5f-a94b-4a83-89f1-e172fc92c3ce) | ![res files](https://github.com/user-attachments/assets/b1503308-0418-4e0a-a403-ef0054967a96) |



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

### חבילה: data

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

תפקיד המחלקה: לשמור את המידע של המשתמש מקומית, ולבסס את הצורה שהמידע של משתמשים נשמר, במיוחד באינראקציה עם המסד נתונים. 
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

### חבילה: engine

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

##### `class GameSolverTask extends RecursiveAction`
מחלקה פנימית המבצעת את החיפוש הרקורסיבי. היא משתמשת ב-`RecursiveAction` כדי להתחלק למשימות משנה המבוצעות במקביל.

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

#### `public class DonutRenderer`
תפקיד המחלקה: ביצת הפתעה. מחלקה האחראית על רינדור תלת-ממדי בזמן אמת של צורת טורוס (דונאט) מסתובבת על גבי `SurfaceView`. המימוש הושרא מהקוד המפורסם `Donut.c` של `a1k0n`.

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
