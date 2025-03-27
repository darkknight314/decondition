package com.social.media.decondition

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.util.Log

/**
 * VPN service that monitors network traffic to detect access to monitored domains.
 * When a monitored domain is detected, it uses AppPreferencesManager to launch
 * the appropriate puzzle activity.
 */
class TrafficMonitorVpnService : VpnService() {
    private val TAG = "TrafficMonitorVpnService"

    // Managers
    private lateinit var preferencesManager: AppPreferencesManager
    private lateinit var vpnHelper: VpnConnectionHelper

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TrafficMonitorVpnService created")

        // Initialize the preferences manager
        preferencesManager = AppPreferencesManager.getInstance(this)

        // Initialize VPN helper with preferences manager
        vpnHelper = VpnConnectionHelper(this, preferencesManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "TrafficMonitorVpnService started")

        // Stop service if intent is null or action is stop
        if (intent == null || intent.action == "STOP") {
            stopVpnAndService()
            return START_NOT_STICKY
        }

        // Create notification for the VPN service
        val configureIntent = Intent(this, SudokuPuzzleActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, configureIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Start the VPN
        vpnHelper.startVpn(pendingIntent)

        return START_STICKY
    }

    /**
     * Stop VPN and service.
     */
    private fun stopVpnAndService() {
        vpnHelper.stopVpn()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnHelper.stopVpn()
        Log.d(TAG, "TrafficMonitorVpnService destroyed")
    }
}