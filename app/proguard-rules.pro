# Proguard rules for WhatSpent
-dontobfuscate

# Room Database & Entities / Tuples
-keep class com.whatspent.app.data.local.** { *; }
-keep class com.whatspent.app.data.model.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Compose
-keepclassmembers class * extends androidx.compose.runtime.State {
    <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# DataStore
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    <fields>;
    <methods>;
}
