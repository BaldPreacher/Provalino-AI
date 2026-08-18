package com.example.ads

/**
 * Centralized configuration for Google AdMob advertising units.
 *
 * NOTE: Currently configured with official Google AdMob Test IDs.
 * When your AdMob account is ready for production, simply replace the test IDs
 * below (and the App ID in AndroidManifest.xml) with your production IDs.
 */
object AdConfig {
    // Flag to enable/disable ads app-wide if needed
    const val ADS_ENABLED = true

    // Official Google Test Ad Unit IDs
    // Production: Replace with your Rewarded Ad Unit ID (e.g., "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Production: Replace with your Interstitial Ad Unit ID (e.g., "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Production: Replace with your Banner Ad Unit ID (e.g., "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy")
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}
