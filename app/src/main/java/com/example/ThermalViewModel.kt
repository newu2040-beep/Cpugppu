package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThermalViewModel(application: Application) : AndroidViewModel(application) {

    private val _cpuTemp = MutableStateFlow(0f)
    val cpuTemp: StateFlow<Float> = _cpuTemp

    private val _batteryTemp = MutableStateFlow(0f)
    val batteryTemp: StateFlow<Float> = _batteryTemp

    private val _threshold = MutableStateFlow(45f) // Default 45°C
    val threshold: StateFlow<Float> = _threshold

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo

    private val notificationHelper = NotificationHelper(application)
    private var hasAlerted = false

    init {
        _deviceInfo.value = DeviceUtils.getDeviceInfo(application)
        viewModelScope.launch {
            while (true) {
                updateTemperatures()
                checkThresholds()
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    private suspend fun updateTemperatures() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val battery = ThermalReader.getBatteryTemperature(getApplication())
            val cpu = ThermalReader.getCpuTemperature() ?: 0f
            _batteryTemp.value = battery
            _cpuTemp.value = cpu
        }
    }

    private fun checkThresholds() {
        val currentTemp = if (_cpuTemp.value > 0) _cpuTemp.value else _batteryTemp.value
        if (currentTemp >= _threshold.value) {
            if (!hasAlerted) {
                notificationHelper.showThermalAlert(currentTemp)
                hasAlerted = true
            }
        } else {
            hasAlerted = false // Reset when it cools down
        }
    }

    fun setThreshold(value: Float) {
        _threshold.value = value
        hasAlerted = false // Reset alert state when threshold changes
    }
}
