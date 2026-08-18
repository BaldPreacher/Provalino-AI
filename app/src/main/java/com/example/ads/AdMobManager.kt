package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdMobManager manages initialization, preloading, and safe presentation of AdMob ads.
 * It provides a graceful fallback: if an ad fails to load or the device is offline,
 * user actions continue uninterrupted.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    private var isInitialized = false
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false

    private val _isRewardedAdReady = MutableStateFlow(false)
    val isRewardedAdReady: StateFlow<Boolean> = _isRewardedAdReady.asStateFlow()

    /**
     * Initializes the Google Mobile Ads SDK asynchronously.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob SDK Initialized: ${initializationStatus.adapterStatusMap}")
                isInitialized = true
                preloadRewardedAd(context)
                preloadInterstitialAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob: ${e.message}", e)
        }
    }

    /**
     * Preloads a Rewarded Video Ad in advance.
     */
    fun preloadRewardedAd(context: Context) {
        if (!AdConfig.ADS_ENABLED || isLoadingRewarded || rewardedAd != null) return

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            AdConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                    rewardedAd = ad
                    isLoadingRewarded = false
                    _isRewardedAdReady.value = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                    rewardedAd = null
                    isLoadingRewarded = false
                    _isRewardedAdReady.value = false
                }
            }
        )
    }

    /**
     * Preloads an Interstitial Ad in advance.
     */
    fun preloadInterstitialAd(context: Context) {
        if (!AdConfig.ADS_ENABLED || isLoadingInterstitial || interstitialAd != null) return

        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad.
     * @param activity Hosting Activity.
     * @param onRewardEarned Invoked when the user earns the reward (or on fallback if ad unavailable).
     * @param onDismissed Invoked when the ad is closed or dismissed.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null && AdConfig.ADS_ENABLED) {
            var rewardGranted = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    rewardedAd = null
                    _isRewardedAdReady.value = false
                    preloadRewardedAd(activity.applicationContext)
                    if (rewardGranted) {
                        onRewardEarned()
                    }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Failed to show rewarded ad: ${adError.message}")
                    rewardedAd = null
                    _isRewardedAdReady.value = false
                    preloadRewardedAd(activity.applicationContext)
                    // Fallback gracefully so teacher is never blocked
                    onRewardEarned()
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad displayed on screen.")
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardGranted = true
            }
        } else {
            // If ad is not ready or disabled, gracefully grant action and preload next
            Log.d(TAG, "Rewarded ad not ready. Graceful bypass executed.")
            preloadRewardedAd(activity.applicationContext)
            onRewardEarned()
            onDismissed()
        }
    }

    /**
     * Shows an Interstitial Ad.
     * @param activity Hosting Activity.
     * @param onFinished Invoked when the ad finishes or on fallback.
     */
    fun showInterstitialAd(
        activity: Activity,
        onFinished: () -> Unit
    ) {
        val ad = interstitialAd
        if (ad != null && AdConfig.ADS_ENABLED) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    preloadInterstitialAd(activity.applicationContext)
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Failed to show interstitial ad: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitialAd(activity.applicationContext)
                    onFinished()
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitialAd(activity.applicationContext)
            onFinished()
        }
    }
}
