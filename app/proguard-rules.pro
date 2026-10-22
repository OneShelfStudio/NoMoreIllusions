# Keep Google Play Billing 8.0.0 classes
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.api.** { *; }

# Keep Google Mobile Ads (AdMob) classes
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.errorprone.annotations.**
