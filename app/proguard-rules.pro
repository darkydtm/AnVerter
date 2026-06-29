# kotlinx.serialization - keep generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.anverter.app.data.remote.dto.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.anverter.app.data.remote.dto.**$$serializer { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
