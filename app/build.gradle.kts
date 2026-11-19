plugins { id("com.android.application") }
android {
 namespace = "com.zamcan.codelab"
 compileSdk = 35
 defaultConfig { applicationId = "com.zamcan.codelab"; minSdk = 26; targetSdk = 35; versionCode = 6; versionName = "0.5.1" }
     compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    testOptions { unitTests { isIncludeAndroidResources = true } }
 buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
 // 23.1.0 is the newest Appwrite Android SDK compatible with compileSdk 35 and AGP 8.6.1.
 // Browser OAuth and verified session access use the Android SDK.
 implementation("io.appwrite:sdk-for-android:23.1.0")
 implementation("androidx.appcompat:appcompat:1.6.1")
 implementation("androidx.activity:activity:1.9.3")
 implementation("com.google.android.gms:play-services-ads:24.7.0")
 implementation("com.google.android.ump:user-messaging-platform:3.2.0")
     testImplementation("junit:junit:4.13.2")
    // Real SQLite behaviour for offline progress, migrations and the upload queue.
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
}
