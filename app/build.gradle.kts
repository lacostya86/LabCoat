import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.apollographql.apollo")
    id("io.fabric") apply false
    id("com.google.gms.google-services") apply false
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.commit451.gitlab"
        minSdkVersion(21)
        targetSdkVersion(29)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = BuildHelper.appVersionCode()
        versionName = BuildHelper.appVersionName()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    signingConfigs {
        create("release") {
            storeFile = BuildHelper.keystoreFile(project)
            storePassword = project.propertyOrEmpty("KEYSTORE_PASSWORD")
            keyAlias = "commit451"
            keyPassword = project.propertyOrEmpty("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro", getDefaultProguardFile("proguard-android.txt"))
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro", getDefaultProguardFile("proguard-android.txt"))
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

apply { from("experimental.gradle") }

val retrofitVersion = "2.9.0"
val okHttpVersion = "4.7.2"

val adapterLayout = "1.2.0"
val materialDialogsVersion = "0.9.6.0"
val addendumVersion = "2.1.1"
val moshiVersion = "1.9.3"
val autodisposeVersion = "2.0.0"

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    implementation("androidx.core:core-ktx:1.3.0")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.browser:browser:1.2.0")

    implementation("com.google.android.material:material:1.2.0-alpha06")

    implementation("com.squareup.okio:okio:2.6.0")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-simplexml:$retrofitVersion") {
        exclude(group = "xpp3", module = "xpp3")
        exclude(group = "stax", module = "stax-api")
        exclude(group = "stax", module = "stax")
    }
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
    implementation("com.squareup.retrofit2:adapter-rxjava3:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")

    val apolloVersion = "2.2.0"
    implementation("com.apollographql.apollo:apollo-runtime:$apolloVersion")
    implementation("com.apollographql.apollo:apollo-rx3-support:$apolloVersion")

    implementation("io.coil-kt:coil:0.11.0")
    implementation("com.github.Commit451:CoilImageGetter:1.1.0")

    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.4")

    implementation("org.greenrobot:eventbus:3.2.0")

    implementation("io.reactivex.rxjava3:rxjava:3.0.4")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.0")

    implementation("com.uber.autodispose2:autodispose-android:$autodisposeVersion")
    implementation("com.uber.autodispose2:autodispose-androidx-lifecycle:$autodisposeVersion")

    implementation("com.github.Commit451:ElasticDragDismissLayout:1.0.4")
    implementation("com.github.Commit451.AdapterLayout:adapterlayout:$adapterLayout")
    implementation("com.github.Commit451.AdapterLayout:adapterflowlayout:$adapterLayout") {
        exclude(group = "com.wefika", module = "flowlayout")
    }
    //https://github.com/blazsolar/FlowLayout/issues/31
    implementation("com.wefika:flowlayout:0.4.1") {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation("com.github.Commit451:Easel:3.1.0")
    implementation("com.github.Commit451:Gimbal:3.0.0")
    implementation("com.github.Commit451:Teleprinter:3.0.0")
    implementation("com.github.Commit451:Jounce:1.0.2")
    implementation("com.github.Commit451:ForegroundViews:2.5.0")
    implementation("com.github.Commit451:MorphTransitions:2.0.0")
    implementation("com.github.Commit451:Alakazam:2.1.0")
    implementation("com.github.Commit451:Lift:2.0.1")
    implementation("com.github.Commit451:okyo:3.0.2")
    implementation("com.github.Commit451:Aloy:1.2.0")
    implementation("com.github.Commit451.Addendum:addendum:$addendumVersion")
    implementation("com.github.Commit451.Addendum:addendum-recyclerview:$addendumVersion")
    implementation("com.github.Commit451.Addendum:addendum-design:$addendumVersion")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")

    implementation("com.github.Jawnnypoo:PhysicsLayout:3.0.0")

    implementation("com.github.ivbaranov:materiallettericon:0.2.3")

    implementation("com.github.alorma:diff-textview:1.1.0") {
        @Suppress("UnstableApiUsage")
        because("Don't upgrade this. Anything above it is bad. Will cause crashes")
    }

    implementation("com.wdullaer:materialdatetimepicker:4.2.3")

    implementation("com.github.novoda:simple-chrome-custom-tabs:0.1.6")

    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:commons:$materialDialogsVersion")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.vdurmont:emoji-java:5.1.1") {
        exclude(group = "org.json", module = "json")
    }

    implementation("com.github.jkwiecien:EasyImage:2.1.1")

    implementation("com.atlassian.commonmark:commonmark:0.15.1")

    implementation(project(":firebaseshim"))
    if (BuildHelper.firebaseEnabled(project)) {
        implementation("com.google.firebase:firebase-core:17.4.1")
        implementation("com.crashlytics.sdk.android:crashlytics:2.10.1")
    }

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.4")

    testImplementation("junit:junit:4.13")
    testImplementation("org.threeten:threetenbp:1.4.4") {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }
}

if (BuildHelper.firebaseEnabled(project)) {
    apply(mapOf("plugin" to "com.google.gms.google-services"))
    apply(mapOf("plugin" to "io.fabric"))
}
