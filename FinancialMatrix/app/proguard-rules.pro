# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class * @androidx.room.Entity
-keep class * @androidx.room.Dao
-keep class * @androidx.room.TypeConverter
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Financial Precision (BigDecimal)
-keep class java.math.BigDecimal { *; }

# Vico Charting
-keep class com.patrykandpatrick.vico.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile