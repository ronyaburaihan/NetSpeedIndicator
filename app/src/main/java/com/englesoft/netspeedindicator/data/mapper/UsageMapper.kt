package com.englesoft.netspeedindicator.data.mapper

import com.englesoft.netspeedindicator.data.local.entity.UsageEntity
import com.englesoft.netspeedindicator.domain.model.UsageModel

/**
 * Mapper functions to convert between data and domain layers
 * Maintains separation of concerns
 */

fun UsageEntity.toDomain(): UsageModel {
    return UsageModel(
        date = date,
        wifiRxBytes = wifiRxBytes,
        wifiTxBytes = wifiTxBytes,
        mobileRxBytes = mobileRxBytes,
        mobileTxBytes = mobileTxBytes
    )
}

fun UsageModel.toEntity(): UsageEntity {
    return UsageEntity(
        date = date,
        wifiRxBytes = wifiRxBytes,
        wifiTxBytes = wifiTxBytes,
        mobileRxBytes = mobileRxBytes,
        mobileTxBytes = mobileTxBytes,
        lastUpdated = System.currentTimeMillis()
    )
}
