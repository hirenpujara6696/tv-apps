package com.example.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VpnRuntimeState(
    val isRunning: Boolean = false,
    val startedAtMillis: Long? = null,
    val sessionBlockedCount: Int = 0,
    val sessionAllowedCount: Int = 0,
    val activeDnsName: String = UpstreamDnsProvider.ADGUARD.displayName,
    val activeDnsIp: String = UpstreamDnsProvider.ADGUARD.primaryIp,
    val lastBlockedDomain: String? = null,
    val lastError: String? = null
)

object VpnStateHolder {
    private val _state = MutableStateFlow(VpnRuntimeState())
    val state: StateFlow<VpnRuntimeState> = _state.asStateFlow()

    fun markStarted(provider: UpstreamDnsProvider) {
        _state.value = VpnRuntimeState(
            isRunning = true,
            startedAtMillis = System.currentTimeMillis(),
            sessionBlockedCount = 0,
            sessionAllowedCount = 0,
            activeDnsName = provider.displayName,
            activeDnsIp = provider.primaryIp,
            lastBlockedDomain = null,
            lastError = null
        )
    }

    fun updateDns(provider: UpstreamDnsProvider) {
        _state.update {
            it.copy(
                activeDnsName = provider.displayName,
                activeDnsIp = provider.primaryIp
            )
        }
    }

    fun recordDecision(domain: String, blocked: Boolean) {
        _state.update { current ->
            if (blocked) {
                current.copy(
                    sessionBlockedCount = current.sessionBlockedCount + 1,
                    lastBlockedDomain = domain
                )
            } else {
                current.copy(
                    sessionAllowedCount = current.sessionAllowedCount + 1
                )
            }
        }
    }

    fun markStopped(error: String? = null) {
        _state.update {
            it.copy(
                isRunning = false,
                startedAtMillis = null,
                lastError = error
            )
        }
    }
}
