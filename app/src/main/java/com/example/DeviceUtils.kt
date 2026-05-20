package com.example

import android.app.ActivityManager
import android.content.Context
import android.os.Build

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val sdkInt: Int,
    val cpuCores: Int,
    val totalRamGb: Float
)

object DeviceUtils {
    fun getDeviceInfo(context: Context): DeviceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        
        val totalRamGb = memInfo.totalMem.toFloat() / (1024 * 1024 * 1024)

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalRamGb = totalRamGb
        )
    }
}
