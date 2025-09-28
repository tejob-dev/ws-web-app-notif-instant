# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebRTC or other native libraries, you may need to
# preserve their native method names:
# -keepclasseswithmembernames class * {
#     native <methods>;
# }

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

# Keep WebSocket classes
-keep class okhttp3.** { *; }
-keep class org.json.** { *; }

# Keep notification service classes
-keep class com.notificationapp.kotlin.service.** { *; }

# Keep model classes
-keep class com.notificationapp.kotlin.model.** { *; }

# Keep configuration classes
-keep class com.notificationapp.kotlin.config.** { *; }
