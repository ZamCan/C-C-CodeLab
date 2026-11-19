plugins { id("com.android.application") }
android {
 namespace = "com.zamcan.codelab"
 compileSdk = 35
 defaultConfig { applicationId = "com.zamcan.codelab"; minSdk = 26; targetSdk = 35; versionCode = 4; versionName = "0.4.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
 // 23.1.0 is the newest Appwrite Android SDK compatible with compileSdk 35 and AGP 8.6.1.
 // The app currently uses only the SDK's OAuth CallbackActivity; login/session code is still pending.
 implementation("io.appwrite:sdk-for-android:23.1.0")
 implementation("androidx.appcompat:appcompat:1.6.1")
 implementation("com.google.android.gms:play-services-ads:24.7.0")
 implementation("com.google.android.ump:user-messaging-platform:3.2.0")
 testImplementation("junit:junit:4.13.2")
}
