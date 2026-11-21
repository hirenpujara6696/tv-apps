package com.example.vpn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.example.data.ShieldPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = ShieldPreferences(context.applicationContext)
                val settings = prefs.getSettingsOnce()
                if (settings.autoStartOnBoot && VpnService.prepare(context.applicationContext) == null) {
                    TvShieldVpnService.start(context.applicationContext)
                }
            } catch (_: Exception) {
                // Ignore if system prevents background start before user unlock
            } finally {
                pendingResult.finish()
            }
        }
    }
}
