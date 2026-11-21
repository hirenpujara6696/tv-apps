package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BilingualStrings
import com.example.ui.MainViewModel
import com.example.ui.TvShieldApp
import com.example.ui.theme.TvShieldTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            val vpnPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.startVpnService()
                } else {
                    val strings = BilingualStrings.forLanguage(uiState.settings.isHindi)
                    viewModel.showMessage(strings.vpnDeniedMessage)
                }
            }

            fun requestVpnAndStart() {
                val prepareIntent = VpnService.prepare(context)
                if (prepareIntent != null) {
                    vpnPermissionLauncher.launch(prepareIntent)
                } else {
                    viewModel.startVpnService()
                }
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) {
                // Proceed to start VPN whether notification permission was granted or denied
                requestVpnAndStart()
            }

            fun onToggleShield() {
                if (uiState.vpn.isRunning) {
                    viewModel.stopVpnService()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        requestVpnAndStart()
                    }
                }
            }

            TvShieldTheme {
                TvShieldApp(
                    state = uiState,
                    onToggleShield = ::onToggleShield,
                    onSelectDns = viewModel::setUpstreamDns,
                    onToggleTvAds = viewModel::setBlockTvAds,
                    onToggleAdNetworks = viewModel::setBlockAdNetworks,
                    onToggleStreamingAds = viewModel::setBlockStreamingAds,
                    onToggleTrackers = viewModel::setBlockTrackers,
                    onToggleAutoStart = viewModel::setAutoStartOnBoot,
                    onToggleStrictBlock = viewModel::setStrictZeroIpBlock,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onSetHindi = viewModel::setHindi,
                    onAddRule = viewModel::addCustomRule,
                    onDeleteRule = viewModel::deleteCustomRule,
                    onClearLogs = viewModel::clearLogs,
                    onTestDomain = viewModel::testDomain
                )
            }
        }
    }
}
