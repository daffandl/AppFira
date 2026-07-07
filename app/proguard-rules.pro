# ============================================================
# ProGuard / R8 Rules — FinanceAI (AppFira)
# ============================================================

# --- Keep line numbers for stack traces (crash debugging) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Kotlin
# ============================================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ============================================================
# Coroutines
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ============================================================
# Jetpack Compose
# ============================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================================
# Room Database
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}
-dontwarn androidx.room.**

# ============================================================
# Koin Dependency Injection
# ============================================================
-keep class org.koin.** { *; }
-keepnames class org.koin.** { *; }
-dontwarn org.koin.**

# ============================================================
# Google Play Services Auth / Sign-In
# ============================================================
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ============================================================
# Google API Client / Sheets / Drive
# ============================================================
-keep class com.google.api.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.** { *; }
-keep class com.google.api.services.sheets.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.api.client.**

# Keep all model classes used for JSON deserialization (Gson)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================
# Gson
# ============================================================
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.Expose <fields>;
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# ============================================================
# Google HTTP Client
# ============================================================
-keep class com.google.http.** { *; }
-dontwarn com.google.http.**
-dontwarn com.google.auth.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient

# ============================================================
# Coil (Image Loading)
# ============================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================================
# QR Code library
# ============================================================
-keep class io.github.g0dkar.** { *; }
-dontwarn io.github.g0dkar.**

# ============================================================
# App Models (Room entities, data classes)
# ============================================================
-keep class com.appfira.financeai.model.** { *; }
-keep class com.appfira.financeai.data.** { *; }

# ============================================================
# Prevent stripping serialization-related members
# ============================================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================================================
# General
# ============================================================
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ============================================================
# PENTING: Keep attributes yang diperlukan Google API Client
# ============================================================
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# ============================================================
# Google Sign-In — KRITIS untuk release build
# Tanpa ini, GoogleSignInAccount dan ApiException akan ter-obfuscate
# dan menyebabkan DEVELOPER_ERROR atau ClassCastException
# ============================================================
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.auth.api.credentials.** { *; }
-keep class com.google.android.gms.common.api.ApiException { *; }
-keep class com.google.android.gms.common.api.Status { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Keep GoogleAccountCredential (OAuth2 untuk Sheets/Drive API)
-keep class com.google.api.client.googleapis.extensions.android.gms.auth.** { *; }
-keep class com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential { *; }

# ============================================================
# Prevent stripping of app's own classes (Koin, etc.)
# ============================================================
-keep class com.appfira.financeai.** { *; }
-keepclassmembers class com.appfira.financeai.** { *; }
