# Keep Google Play Billing 8.0.0 classes
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.api.** { *; }

# Keep Google Mobile Ads (AdMob) classes
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.errorprone.annotations.**

# AdMob Mediation Adapters (Unity Ads & Vungle)
-keep class com.google.ads.mediation.unity.** { *; }
-dontwarn com.unity3d.**
-dontwarn com.google.ads.mediation.unity.**

-keep class com.google.ads.mediation.vungle.** { *; }
-dontwarn com.vungle.**
-dontwarn com.google.ads.mediation.vungle.**
