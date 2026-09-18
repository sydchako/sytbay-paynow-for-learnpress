plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val admobAppId = providers.gradleProperty("ADMOB_APP_ID").orElse("ca-app-pub-3940256099942544~3347511713")
val admobBannerId = providers.gradleProperty("ADMOB_BANNER_ID").orElse("ca-app-pub-3940256099942544/6300978111")

android {
    namespace = "zw.co.sytbay.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "zw.co.sytbay.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "0.3.0"
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId.get()
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${admobBannerId.get()}\"")
    }
    buildFeatures { buildConfig = true }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
}