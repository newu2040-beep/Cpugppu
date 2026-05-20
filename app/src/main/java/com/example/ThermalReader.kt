package com.example

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File

object ThermalReader {
    fun getBatteryTemperature(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f // It comes in tenths of a degree Celsius
    }

    fun getCpuTemperature(): Float? {
        // Attempt to read from sysfs thermal zones
        try {
            val dir = File("/sys/class/thermal/")
            if (dir.exists()) {
                val zones = dir.listFiles { file -> file.name.startsWith("thermal_zone") }
                zones?.forEach { zone ->
                    val typeFile = File(zone, "type")
                    val tempFile = File(zone, "temp")
                    if (typeFile.exists() && tempFile.exists()) {
                        val type = typeFile.readText().trim().lowercase()
                        if (type.contains("cpu") || type.contains("tsens") || type.contains("bcl")) {
                            val currentTemp = tempFile.readText().trim().toFloatOrNull()
                            if (currentTemp != null) {
                                // Some output in millidegrees, some in degrees
                                return if (currentTemp > 1000) currentTemp / 1000f else currentTemp
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
