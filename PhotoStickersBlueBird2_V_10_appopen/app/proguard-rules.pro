# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\ivcha\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}
-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}
-dontwarn android.security.NetworkSecurityPolicy
-keep public class com.google.android.gms.** {
    *;
}
-keep public class com.google.ads.** {
    public *;
}

-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# unity ADS
-keepattributes SourceFile,LineNumberTable
-keepattributes JavascriptInterface
-keep class android.webkit.JavascriptInterface {
   *;
}
-keep class com.unity3d.ads.** {
   *;
}
-dontwarn android.webkit.**
-keep public class com.unity3d.ads.android.**
-keepclassmembers class com.unity3d.ads.android.** {
   public *;
}
-keep public class com.unity3d.ads.android.**$*
-keepclassmembers class com.unity3d.ads.android.**$* {
   public *;
}
-keep public class com.applifier.impact.android.ApplifierImpact
-keepclassmembers class com.applifier.impact.android.ApplifierImpact {
   public *;
}
-keep public interface com.applifier.impact.android.IApplifierImpactListener
-keepclassmembers interface com.applifier.impact.android.IApplifierImpactListener {
   public *;
}
-keep public enum com.unity3d.ads.android.view.UnityAdsMainView$UnityAdsMainViewAction {
   **[] $VALUES;
   public *;
}
-keep public enum com.unity3d.ads.android.campaign.UnityAdsCampaign$UnityAdsCampaignStatus {
   **[] $VALUES;
   public *;
}

#AdColony
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn android.app.Activity
-ignorewarnings
#Ad Mob
-keep class com.google.ads.** # Don't proguard AdMob classes
-dontwarn com.google.ads.** # Temporary workaround for v6.2.1. It gives a warning that you can ignor
#AppLovin
-dontwarn com.applovin.**
-keep class com.adcolony.** { *; }
-keep class com.applovin.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }

#ChartBoost
-keep class com.chartboost.** { *; }
#Facebook
-keep class com.facebook.ads.** { *; }