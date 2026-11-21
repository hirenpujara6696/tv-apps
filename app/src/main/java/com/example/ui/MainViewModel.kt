package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TvShieldApplication
import com.example.data.CustomDomainRule
import com.example.data.DnsQueryLog
import com.example.data.ShieldSettings
import com.example.vpn.FilterDecision
import com.example.vpn.TvShieldVpnService
import com.example.vpn.VpnRuntimeState
import com.example.vpn.VpnStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DomainTestResult(
    val domain: String,
    val decision: FilterDecision
)

data class ShieldUiState(
    val vpn: VpnRuntimeState = VpnRuntimeState(),
    val settings: ShieldSettings = ShieldSettings(),
    val customRules: List<CustomDomainRule> = emptyList(),
    val recentLogs: List<DnsQueryLog> = emptyList(),
    val blockedToday: Int = 0,
    val totalBlocked: Int = 0,
    val totalQueries: Int = 0,
    val activeRuleCount: Int = 0,
    val testResult: DomainTestResult? = null,
    val bannerMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TvShieldApplication).repository

    private val _testResult = MutableStateFlow<DomainTestResult?>(null)
    private val _bannerMessage = MutableStateFlow<String?>(null)

    private val statsFlow = combine(
        repository.observeBlockedToday(),
        repository.totalBlockedFlow,
        repository.totalQueriesFlow
    ) { today, totalBlocked, totalQueries ->
        Triple(today, totalBlocked, totalQueries)
    }

    private val coreDataFlow = combine(
        VpnStateHolder.state,
        repository.syncedEngineFlow,
        repository.recentLogsFlow,
        statsFlow
    ) { vpnState, (settings, rules), logs, (today, totalBlocked, totalQueries) ->
        ShieldUiState(
            vpn = vpnState,
            settings = settings,
            customRules = rules,
            recentLogs = logs,
            blockedToday = today,
            totalBlocked = totalBlocked,
            totalQueries = totalQueries,
            activeRuleCount = repository.filterEngine.activeRuleCount()
        )
    }

    val uiState: StateFlow<ShieldUiState> = combine(
        coreDataFlow,
        _testResult.asStateFlow(),
        _bannerMessage.asStateFlow()
    ) { core, test, message ->
        core.copy(
            testResult = test,
            bannerMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShieldUiState(
            activeRuleCount = repository.filterEngine.activeRuleCount()
        )
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshEngineSnapshot()
        }
    }

    fun startVpnService() {
        _bannerMessage.value = null
        TvShieldVpnService.start(getApplication())
    }

    fun stopVpnService() {
        TvShieldVpnService.stop(getApplication())
    }

    fun showMessage(message: String) {
        _bannerMessage.value = message
    }

    fun clearMessage() {
        _bannerMessage.value = null
    }

    fun setUpstreamDns(id: String) {
        viewModelScope.launch {
            repository.setUpstreamDns(id)
            TvShieldVpnService.reloadDns(getApplication())
        }
    }

    fun setBlockTvAds(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockTvAds(enabled) }
    }

    fun setBlockAdNetworks(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockAdNetworks(enabled) }
    }

    fun setBlockStreamingAds(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockStreamingAds(enabled) }
    }

    fun setBlockTrackers(enabled: Boolean) {
        viewModelScope.launch { repository.setBlockTrackers(enabled) }
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoStartOnBoot(enabled) }
    }

    fun setStrictZeroIpBlock(enabled: Boolean) {
        viewModelScope.launch { repository.setStrictZeroIpBlock(enabled) }
    }

    fun toggleLanguage() {
        val next = !uiState.value.settings.isHindi
        viewModelScope.launch { repository.setIsHindi(next) }
    }

    fun setHindi(enabled: Boolean) {
        viewModelScope.launch { repository.setIsHindi(enabled) }
    }

    fun addCustomRule(domain: String, isAllowlist: Boolean) {
        viewModelScope.launch {
            val added = repository.addCustomRule(domain, isAllowlist)
            if (!added) {
                showMessage("Enter a valid domain like ads.example.com")
            }
        }
    }

    fun deleteCustomRule(id: Long) {
        viewModelScope.launch { repository.deleteRule(id) }
    }

    fun clearLogs() {
        viewModelScope.launch { repository.clearLogs() }
    }

    fun testDomain(rawDomain: String) {
        val trimmed = rawDomain.trim()
        if (trimmed.isEmpty()) return
        val decision = repository.filterEngine.evaluate(trimmed)
        _testResult.value = DomainTestResult(
            domain = trimmed,
            decision = decision
        )
    }
}
