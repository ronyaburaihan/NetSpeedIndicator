package com.englesoft.netspeedindicator.data.mapper

import com.englesoft.netspeedindicator.data.local.entity.UsageEntity
import com.englesoft.netspeedindicator.domain.model.UsageInfo

/**
 * Mapper functions to convert between data and domain layers
 * Maintains separation of concerns
 */

fun UsageEntity.toDomain(): UsageInfo {
    return UsageInfo(
        date = date,
        wifiRxBytes = wifiRxBytes,
        wifiTxBytes = wifiTxBytes,
        mobileRxBytes = mobileRxBytes,
        mobileTxBytes = mobileTxBytes
    )
}

fun UsageInfo.toEntity(): UsageEntity {
    return UsageEntity(
        date = date,
        wifiRxBytes = wifiRxBytes,
        wifiTxBytes = wifiTxBytes,
        mobileRxBytes = mobileRxBytes,
        mobileTxBytes = mobileTxBytes,
        lastUpdated = System.currentTimeMillis()
    )
}
