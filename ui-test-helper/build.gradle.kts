plugins { id("com.android.application") }
android {
    namespace = "com.xiaoyu.awake.uitest"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.xiaoyu.awake.uitest"
        minSdk = 26
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
