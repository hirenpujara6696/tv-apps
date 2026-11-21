package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.TvShieldApplication
import com.example.data.ShieldSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

class TvShieldVpnService : VpnService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnelJob: Job? = null
    private var settingsObserverJob: Job? = null
    private val writeMutex = Mutex()
    private val logWriteCounter = AtomicInteger(0)

    @Volatile
    private var currentSettings: ShieldSettings = ShieldSettings()

    private val repository by lazy {
        (application as TvShieldApplication).repository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopShield()
                return START_NOT_STICKY
            }
            ACTION_RELOAD_DNS -> {
                serviceScope.launch {
                    val refreshed = repository.refreshEngineSnapshot()
                    currentSettings = refreshed
                    VpnStateHolder.updateDns(refreshed.activeProvider)
                }
                return START_STICKY
            }
            else -> {
                startShield()
                return START_STICKY
            }
        }
    }

    private fun startShield() {
        if (vpnInterface != null && tunnelJob?.isActive == true) return

        createNotificationChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        settingsObserverJob?.cancel()
        settingsObserverJob = serviceScope.launch {
            repository.syncedEngineFlow.collect { (settings, _) ->
                currentSettings = settings
                VpnStateHolder.updateDns(settings.activeProvider)
            }
        }

        tunnelJob?.cancel()
        tunnelJob = serviceScope.launch {
            try {
                val initialSettings = repository.refreshEngineSnapshot()
                currentSettings = initialSettings

                val builder = Builder()
                    .setSession("TV Shield DNS")
                    .setMtu(1500)
                    .addAddress(VIRTUAL_CLIENT_IP, 32)
                    .addDnsServer(VIRTUAL_DNS_IP)
                    .addRoute(VIRTUAL_DNS_IP, 32)
                    .setBlocking(true)

                try {
                    builder.addDisallowedApplication(packageName)
                } catch (_: Exception) {
                    // Safe fallback if package exclusion is unsupported on a custom ROM
                }

                val pfd = builder.establish()
                if (pfd == null) {
                    VpnStateHolder.markStopped("VPN permission required")
                    stopSelf()
                    return@launch
                }

                vpnInterface = pfd
                VpnStateHolder.markStarted(initialSettings.activeProvider)

                runPacketLoop(pfd)
            } catch (t: Throwable) {
                Log.e(TAG, "VPN tunnel loop ended with error", t)
                if (isActive) {
                    VpnStateHolder.markStopped(t.message ?: "VPN stopped")
                }
            } finally {
                closeInterfaceQuietly()
            }
        }
    }

    private suspend fun runPacketLoop(pfd: ParcelFileDescriptor) {
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val outputStream = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(4096)

        while (serviceScope.isActive && vpnInterface != null) {
            val bytesRead = try {
                inputStream.read(buffer)
            } catch (_: Exception) {
                break
            }
            if (bytesRead <= 0) continue

            val request = DnsPacketCodec.parseIpv4UdpDnsRequest(buffer, bytesRead) ?: continue

            val decision = repository.filterEngine.evaluate(request.domain)
            VpnStateHolder.recordDecision(request.domain, decision.blocked)

            if (decision.blocked) {
                val blockedPayload = DnsPacketCodec.buildBlockedDnsPayload(
                    request = request,
                    zeroIpMode = currentSettings.strictZeroIpBlock
                )
                val responsePacket = DnsPacketCodec.buildIpv4UdpResponsePacket(
                    request = request,
                    dnsResponsePayload = blockedPayload
                )
                writeToTunnel(outputStream, responsePacket)
                persistLogAsync(request.domain, blocked = true, category = decision.category.key)
            } else {
                val provider = currentSettings.activeProvider
                serviceScope.launch {
                    val upstreamReply = forwardDnsQuery(request.dnsPayload, provider)
                    if (upstreamReply != null) {
                        val responsePacket = DnsPacketCodec.buildIpv4UdpResponsePacket(
                            request = request,
                            dnsResponsePayload = upstreamReply
                        )
                        writeToTunnel(outputStream, responsePacket)
                    }
                }
                persistLogAsync(request.domain, blocked = false, category = decision.category.key)
            }
        }
    }

    private suspend fun writeToTunnel(outputStream: FileOutputStream, packet: ByteArray) {
        writeMutex.withLock {
            try {
                outputStream.write(packet)
            } catch (e: Exception) {
                Log.w(TAG, "Failed writing response packet to tunnel", e)
            }
        }
    }

    private fun forwardDnsQuery(dnsPayload: ByteArray, provider: UpstreamDnsProvider): ByteArray? {
        return queryUpstream(dnsPayload, provider.primaryIp)
            ?: queryUpstream(dnsPayload, provider.secondaryIp)
    }

    private fun queryUpstream(dnsPayload: ByteArray, serverIp: String): ByteArray? {
        var socket: DatagramSocket? = null
        return try {
            socket = DatagramSocket()
            if (!protect(socket)) {
                return null
            }
            socket.soTimeout = 2500
            val address = InetAddress.getByName(serverIp)
            val sendPacket = DatagramPacket(dnsPayload, dnsPayload.size, address, 53)
            socket.send(sendPacket)

            val responseBuffer = ByteArray(2048)
            val receivePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            socket.receive(receivePacket)
            responseBuffer.copyOf(receivePacket.length)
        } catch (_: Exception) {
            null
        } finally {
            socket?.close()
        }
    }

    private fun persistLogAsync(domain: String, blocked: Boolean, category: String) {
        serviceScope.launch {
            try {
                repository.recordDnsQuery(domain, blocked, category)
                if (logWriteCounter.incrementAndGet() % 100 == 0) {
                    repository.trimOldLogs()
                }
            } catch (_: Exception) {
                // Keep VPN loop unaffected by transient DB issues
            }
        }
    }

    private fun stopShield() {
        tunnelJob?.cancel()
        settingsObserverJob?.cancel()
        closeInterfaceQuietly()
        VpnStateHolder.markStopped()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun closeInterfaceQuietly() {
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        } finally {
            vpnInterface = null
        }
    }

    override fun onRevoke() {
        stopShield()
        super.onRevoke()
    }

    override fun onDestroy() {
        closeInterfaceQuietly()
        serviceScope.cancel()
        VpnStateHolder.markStopped()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.vpn_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.vpn_notification_channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TvShieldVpnService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.vpn_notification_title))
            .setContentText(getString(R.string.vpn_notification_text))
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openAppIntent)
            .addAction(0, getString(R.string.vpn_notification_action_stop), stopIntent)
            .build()
    }

    companion object {
        private const val TAG = "TvShieldVpnService"
        private const val CHANNEL_ID = "tv_shield_vpn_channel"
        private const val NOTIFICATION_ID = 4041
        private const val VIRTUAL_CLIENT_IP = "10.120.0.1"
        private const val VIRTUAL_DNS_IP = "10.120.0.2"

        const val ACTION_START = "com.example.tvshield.ACTION_START"
        const val ACTION_STOP = "com.example.tvshield.ACTION_STOP"
        const val ACTION_RELOAD_DNS = "com.example.tvshield.ACTION_RELOAD_DNS"

        fun start(context: Context) {
            val intent = Intent(context, TvShieldVpnService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TvShieldVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun reloadDns(context: Context) {
            if (!VpnStateHolder.state.value.isRunning) return
            val intent = Intent(context, TvShieldVpnService::class.java).apply {
                action = ACTION_RELOAD_DNS
            }
            context.startService(intent)
        }
    }
}
