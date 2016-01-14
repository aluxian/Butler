# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-dontobfuscate

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# App
-keep class com.aluxian.butler.database.xml.* { *; }
-keep class com.aluxian.butler.recycler.**.*$ViewHolder { *; }
-keep class com.aluxian.butler.**.* implements java.io.Serializable { *; }

# Stanford CoreNLP
-dontwarn org.ejml.**
-dontwarn org.joda.**
-dontwarn javax.json.**
-dontwarn javax.swing.**
-dontwarn javax.xml.**
-dontwarn de.jollyday.**
-dontwarn nu.xom.**
-dontwarn edu.stanford.nlp.time.JollyDayHolidays$MyXMLManager

# ActiveAndroid
-keep class com.activeandroid.** { *; }
-keep class * extends com.activeandroid.Model { *; }
-keep class * extends com.activeandroid.serializer.TypeSerializer { *; }
-keepattributes Column, Table

# New Relic
-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses

# Mixpanel
-dontwarn com.mixpanel.**

# Crashlytics
-keepattributes SourceFile, LineNumberTable

# ButterKnife
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

# Retrofit, Picasso, OkHttp
-keep class retrofit.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-dontwarn com.google.appengine.api.urlfetch.*
-dontwarn com.squareup.okhttp.**
-dontwarn java.nio.file.*
-dontwarn org.codehaus.**
-dontwarn rx.**

# Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class sun.misc.Unsafe { *; }
-keepattributes Expose, SerializedName, Since, Until
-keepclasseswithmembers class * { @com.google.gson.annotations.Expose <fields>; }

# Play Services
-keep class * extends java.util.ListResourceBundle { protected Object[][] getContents(); }
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepnames class * implements android.os.Parcelable { public static final ** CREATOR; }
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }

# Simple XML
-keep class org.simpleframework.xml.** { *; }
-keepattributes *Annotation*, Signature
