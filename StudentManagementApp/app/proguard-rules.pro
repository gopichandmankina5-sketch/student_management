# Add project specific ProGuard rules here.

# Keep Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Gson model classes
-keep class com.example.studentmanagement.model.** { *; }

# Keep Room entities
-keep class com.example.studentmanagement.database.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.** { *; }
-keepnames class kotlinx.coroutines.** { *; }
