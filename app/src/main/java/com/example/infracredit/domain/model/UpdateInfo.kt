package com.example.infracredit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val latestVersionCode: Int,
    val apkUrl: String
)
