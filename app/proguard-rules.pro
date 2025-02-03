# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# Keep generic signatures; needed for correct type resolution
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses,EnclosingMethod

# Keep the BouncyCastle classes
-keep class org.bouncycastle.** { *; }
-keep class org.bouncycastle.jsse.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }

# Keep javax.naming classes
-keep class javax.naming.** { *; }
-dontwarn javax.naming.**

# Keep Retrofit
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# Keep WorkManager
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Keep Android classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep Java serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep all your model classes
-keep class com.ahsan.watertrackplus.** { *; }

# Keep JSSE classes
-keep class javax.net.ssl.** { *; }
-dontwarn javax.net.ssl.**
-keep class java.security.** { *; }
-dontwarn java.security.**

# Keep XML-related classes
-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**

# Keep any native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all public constructors of serializable classes
-keepclassmembers class * implements java.io.Serializable {
    public <init>();
}

# Keep models
-keep class com.ahsan.watertrackplus.models.** { *; }
-keep class com.ahsan.watertrackplus.NewsResponse { *; }
-keep class com.ahsan.watertrackplus.Article { *; }

# Keep adapters
-keep class com.ahsan.watertrackplus.adapters.** { *; }
-keep class com.ahsan.watertrackplus.adapters.ArticleAdapter { *; }
-keepclassmembers class com.ahsan.watertrackplus.adapters.ArticleAdapter {
    public void addArticles(java.util.List);
    public void clearArticles();
    public void setHasMoreItems(boolean);
    public int getItemCount();
    public boolean getHasMoreItems();
}

# Keep models and responses
-keep class com.ahsan.watertrackplus.models.** { *; }
-keep class com.ahsan.watertrackplus.NewsResponse { *; }
-keep class com.ahsan.watertrackplus.Article { *; }
-keep class com.ahsan.watertrackplus.adapters.ArticleAdapter$* { *; }

# Keep interfaces
-keep interface com.ahsan.watertrackplus.adapters.ArticleAdapter$OnArticleClickListener { *; }