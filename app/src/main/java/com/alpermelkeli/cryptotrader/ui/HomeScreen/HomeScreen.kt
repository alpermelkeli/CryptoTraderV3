package com.alpermelkeli.cryptotrader.ui.HomeScreen

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.ActivityHomeScreenBinding
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private val REQUEST_FOREGROUND_PERMISSION = 1
    private val REQUEST_DATA_SYNC_PERMISSION = 2
    private val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestForegroundServicePermission()
        checkAndRequestBackgroundPermission()
        startBotServiceIfNotRunning()
        createBottomNavigation()
    }

    private fun createBottomNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navHostFragment.navController)
    }

    fun hideBottomNavigationView() {
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.GONE
    }

    fun showBottomNavigationView() {
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }

    private fun startBotServiceIfNotRunning() {
        if (!isServiceRunning(BotService::class.java)) {
            val intent = Intent(this, BotService::class.java).apply {
                action = "START_SERVICE"
            }
            startService(intent)
            Toast.makeText(applicationContext, "Servis başlatıldı bildirimden sonra botları kurmaya başlayabilirsiniz.", Toast.LENGTH_LONG).show()
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkAndRequestForegroundServicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (applicationContext.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE), REQUEST_FOREGROUND_PERMISSION)
            } else {
                checkAndRequestDataSyncPermission()
            }
        } else {
            checkAndRequestDataSyncPermission()
        }
    }

    private fun checkAndRequestDataSyncPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_DATA_SYNC_PERMISSION)
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val packageName = packageName
        val pm = getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun showPermissionExplanation() {
        AlertDialog.Builder(this)
            .setTitle("Arka Planda Çalışma İzni Gerekli")
            .setMessage("Bu uygulama, doğru çalışması için arka planda çalışma iznine ihtiyaç duymaktadır.")
            .setPositiveButton("İzin Ver") { _, _ -> requestIgnoreBatteryOptimizations() }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun checkAndRequestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations()) {
                showPermissionExplanation()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_FOREGROUND_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAndRequestDataSyncPermission()
                } else {
                    AlertDialog.Builder(applicationContext)
                        .setMessage("Foreground service permission is required to start the trading bot.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            REQUEST_DATA_SYNC_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Additional logic if needed
                } else {
                    AlertDialog.Builder(applicationContext)
                        .setMessage("Data sync permission is required to start the trading bot.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}
