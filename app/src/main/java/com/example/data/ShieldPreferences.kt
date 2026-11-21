package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.vpn.UpstreamDnsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.shieldDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tv_shield_prefs"
)

data class ShieldSettings(
    val upstreamDnsId: String = UpstreamDnsProvider.ADGUARD.id,
    val blockTvAds: Boolean = true,
    val blockAdNetworks: Boolean = true,
    val blockStreamingAds: Boolean = true,
    val blockTrackers: Boolean = true,
    val autoStartOnBoot: Boolean = true,
    val strictZeroIpBlock: Boolean = true,
    val isHindi: Boolean = false
) {
    val activeProvider: UpstreamDnsProvider
        get() = UpstreamDnsProvider.fromId(upstreamDnsId)
}

class ShieldPreferences(private val context: Context) {

    private object Keys {
        val UPSTREAM_DNS_ID = stringPreferencesKey("upstream_dns_id")
        val BLOCK_TV_ADS = booleanPreferencesKey("block_tv_ads")
        val BLOCK_AD_NETWORKS = booleanPreferencesKey("block_ad_networks")
        val BLOCK_STREAMING_ADS = booleanPreferencesKey("block_streaming_ads")
        val BLOCK_TRACKERS = booleanPreferencesKey("block_trackers")
        val AUTO_START_ON_BOOT = booleanPreferencesKey("auto_start_on_boot")
        val STRICT_ZERO_IP_BLOCK = booleanPreferencesKey("strict_zero_ip_block")
        val IS_HINDI = booleanPreferencesKey("is_hindi")
    }

    val settingsFlow: Flow<ShieldSettings> = context.shieldDataStore.data.map { prefs ->
        ShieldSettings(
            upstreamDnsId = prefs[Keys.UPSTREAM_DNS_ID] ?: UpstreamDnsProvider.ADGUARD.id,
            blockTvAds = prefs[Keys.BLOCK_TV_ADS] ?: true,
            blockAdNetworks = prefs[Keys.BLOCK_AD_NETWORKS] ?: true,
            blockStreamingAds = prefs[Keys.BLOCK_STREAMING_ADS] ?: true,
            blockTrackers = prefs[Keys.BLOCK_TRACKERS] ?: true,
            autoStartOnBoot = prefs[Keys.AUTO_START_ON_BOOT] ?: true,
            strictZeroIpBlock = prefs[Keys.STRICT_ZERO_IP_BLOCK] ?: true,
            isHindi = prefs[Keys.IS_HINDI] ?: false
        )
    }

    suspend fun getSettingsOnce(): ShieldSettings = settingsFlow.first()

    suspend fun setUpstreamDns(id: String) {
        context.shieldDataStore.edit { it[Keys.UPSTREAM_DNS_ID] = id }
    }

    suspend fun setBlockTvAds(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.BLOCK_TV_ADS] = enabled }
    }

    suspend fun setBlockAdNetworks(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.BLOCK_AD_NETWORKS] = enabled }
    }

    suspend fun setBlockStreamingAds(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.BLOCK_STREAMING_ADS] = enabled }
    }

    suspend fun setBlockTrackers(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.BLOCK_TRACKERS] = enabled }
    }

    suspend fun setAutoStartOnBoot(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.AUTO_START_ON_BOOT] = enabled }
    }

    suspend fun setStrictZeroIpBlock(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.STRICT_ZERO_IP_BLOCK] = enabled }
    }

    suspend fun setIsHindi(enabled: Boolean) {
        context.shieldDataStore.edit { it[Keys.IS_HINDI] = enabled }
    }
}
