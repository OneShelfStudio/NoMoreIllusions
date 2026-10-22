package com.oneshelfstudio.nomoreillusions

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Dedicated Google AdMob Manager for No More Illusions
 * App ID: ca-app-pub-7651037308656903~2738129304
 * Banner (Main_Banner): ca-app-pub-7651037308656903/2083523188
 * Interstitial (Level_Interstitial): ca-app-pub-7651037308656903/7798884296
 * Rewarded (Revive_Rewarded): ca-app-pub-7651037308656903/5252612003
 */
class AdManager(private val context: Context) {
    companion object {
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val PROD_BANNER_ID = "ca-app-pub-7651037308656903/2083523188"

        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val PROD_INTERSTITIAL_ID = "ca-app-pub-7651037308656903/7798884296"

        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        const val PROD_REWARDED_ID = "ca-app-pub-7651037308656903/5252612003"

        // Locked to TRUE across all builds (Debug & Release/Internal Testing) during Artist Testing
        // so neither Ethan nor testers can ever trigger live AdMob impressions or clicks.
        @Volatile
        var forceSafeTestAds: Boolean = true

        val bannerAdUnitId: String
            get() = if (BuildConfig.DEBUG || forceSafeTestAds) TEST_BANNER_ID else PROD_BANNER_ID

        val interstitialAdUnitId: String
            get() = if (BuildConfig.DEBUG || forceSafeTestAds) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

        val rewardedAdUnitId: String
            get() = if (BuildConfig.DEBUG || forceSafeTestAds) TEST_REWARDED_ID else PROD_REWARDED_ID
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    init {
        loadRewardedAd()
        loadInterstitialAd()
    }

    private fun loadRewardedAd() {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardedAdUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                isLoadingRewarded = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoadingRewarded = false
            }
        })
    }

    private fun loadInterstitialAd() {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
                isLoadingInterstitial = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isLoadingInterstitial = false
            }
        })
    }

    fun isAdAvailable(): Boolean = rewardedAd != null || BuildConfig.DEBUG || forceSafeTestAds

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    if (BuildConfig.DEBUG || forceSafeTestAds) {
                        onRewardEarned()
                    }
                    onAdDismissed()
                }
            }
            ad.show(activity) { _ ->
                onRewardEarned()
            }
        } ?: run {
            loadRewardedAd()
            if (BuildConfig.DEBUG || forceSafeTestAds) {
                onRewardEarned()
            }
            onAdDismissed()
        }
    }

    fun showInterstitialIfReady(activity: Activity, onComplete: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onComplete()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onComplete()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd()
            onComplete()
        }
    }
}
