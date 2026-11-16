# R8 rules for the release build. Default rules come from proguard-android-optimize.txt
# (JNI native methods, enums, Parcelable, View constructors) and from each library's
# consumer rules; this file covers what the app does by reflection.

# Readable crash stacks in Sentry/Play (upload mapping.txt per release).
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Gson maps JSON keys to field names, and about half of the API/storage models have no
# @SerializedName. Jackson models (@JsonProperty on constructor params) are built through
# their constructor by reflection. Keep field names and constructors in app code; classes
# and methods are still renamed.
-keepclassmembers class com.asfoundation.wallet.**, com.appcoins.wallet.**, com.asf.**, com.wallet.** {
  <fields>;
  <init>(...);
}
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken

# Retrofit 2.8 has no embedded rules: keep the annotated service methods and the generic
# return types the Gson/RxJava/coroutine adapters read through reflection.
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class io.reactivex.Single
-keep,allowobfuscation,allowshrinking class io.reactivex.Observable
-keep,allowobfuscation,allowshrinking class io.reactivex.Completable
-keep,allowobfuscation,allowshrinking class io.reactivex.Maybe
-keep,allowobfuscation,allowshrinking class io.reactivex.Flowable
-dontwarn retrofit2.KotlinExtensions*
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Called by name from the checkout web pages.
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }

# Crypto and Ethereum stacks resolve providers, ABI types and JSON-RPC models by reflection.
-keep class org.web3j.** { *; }
-keep class org.spongycastle.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class ethereumj.** { *; }
-keep class org.kethereum.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-dontwarn org.web3j.**
-dontwarn org.spongycastle.**
-dontwarn org.bouncycastle.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn okio.**

# Optional dependencies of the VK SDK that the app does not ship. They are missing from
# the non-minified APK too; R8 only reports them.
-dontwarn com.vk.**
-dontwarn com.my.tracker.**
-dontwarn com.huawei.hms.**
-dontwarn ru.mail.**
-dontwarn pub.devrel.easypermissions.**

-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.crypto.tink.** { *; }
