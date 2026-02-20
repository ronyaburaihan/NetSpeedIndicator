package com.englesoft.netspeedindicator.domain.model

data class UsageInfo(
    val date: String = "", // Format: yyyy-MM-dd
    val wifiRxBytes: Long = 0L, // WiFi received bytes
    val wifiTxBytes: Long = 0L, // WiFi transmitted bytes
    val mobileRxBytes: Long = 0L, // Mobile data received bytes
    val mobileTxBytes: Long = 0L, // Mobile data transmitted bytes
) {
    val totalBytes: Long
        get() = wifiRxBytes + wifiTxBytes + mobileRxBytes + mobileTxBytes

    val wifiTotalBytes: Long
        get() = wifiRxBytes + wifiTxBytes

    val mobileTotalBytes: Long
        get() = mobileRxBytes + mobileTxBytes
}
