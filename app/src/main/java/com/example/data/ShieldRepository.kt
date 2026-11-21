package com.example.data

import com.example.vpn.DomainFilterEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class ShieldRepository(
    private val dao: ShieldDao,
    private val preferences: ShieldPreferences,
    val filterEngine: DomainFilterEngine
) {
    val settingsFlow: Flow<ShieldSettings> = preferences.settingsFlow
    val customRulesFlow: Flow<List<CustomDomainRule>> = dao.observeRules()
    val recentLogsFlow: Flow<List<DnsQueryLog>> = dao.observeRecentLogs()
    val totalBlockedFlow: Flow<Int> = dao.observeTotalBlockedCount()
    val totalQueriesFlow: Flow<Int> = dao.observeTotalQueryCount()

    fun observeBlockedToday(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.observeBlockedSince(cal.timeInMillis)
    }

    val syncedEngineFlow: Flow<Pair<ShieldSettings, List<CustomDomainRule>>> =
        combine(settingsFlow, customRulesFlow) { settings, rules ->
            filterEngine.updateConfiguration(settings, rules)
            settings to rules
        }

    suspend fun refreshEngineSnapshot(): ShieldSettings {
        val settings = preferences.getSettingsOnce()
        val rules = dao.getAllRulesOnce()
        filterEngine.updateConfiguration(settings, rules)
        return settings
    }

    suspend fun addCustomRule(rawDomain: String, isAllowlist: Boolean): Boolean {
        val normalized = DomainFilterEngine.normalizeDomain(rawDomain)
        if (normalized.isEmpty() || !normalized.contains('.')) return false
        dao.upsertRule(
            CustomDomainRule(
                domain = normalized,
                isAllowlist = isAllowlist
            )
        )
        refreshEngineSnapshot()
        return true
    }

    suspend fun deleteRule(id: Long) {
        dao.deleteRuleById(id)
        refreshEngineSnapshot()
    }

    suspend fun recordDnsQuery(domain: String, blocked: Boolean, category: String) {
        dao.insertLog(
            DnsQueryLog(
                domain = domain,
                blocked = blocked,
                category = category
            )
        )
    }

    suspend fun trimOldLogs() {
        dao.trimOldLogs()
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun setUpstreamDns(id: String) {
        preferences.setUpstreamDns(id)
    }

    suspend fun setBlockTvAds(enabled: Boolean) {
        preferences.setBlockTvAds(enabled)
        refreshEngineSnapshot()
    }

    suspend fun setBlockAdNetworks(enabled: Boolean) {
        preferences.setBlockAdNetworks(enabled)
        refreshEngineSnapshot()
    }

    suspend fun setBlockStreamingAds(enabled: Boolean) {
        preferences.setBlockStreamingAds(enabled)
        refreshEngineSnapshot()
    }

    suspend fun setBlockTrackers(enabled: Boolean) {
        preferences.setBlockTrackers(enabled)
        refreshEngineSnapshot()
    }

    suspend fun setAutoStartOnBoot(enabled: Boolean) {
        preferences.setAutoStartOnBoot(enabled)
    }

    suspend fun setStrictZeroIpBlock(enabled: Boolean) {
        preferences.setStrictZeroIpBlock(enabled)
    }

    suspend fun setIsHindi(enabled: Boolean) {
        preferences.setIsHindi(enabled)
    }
}
