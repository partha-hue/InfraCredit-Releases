package com.example.infracredit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VersionDto(
    val latestVersionCode: Int,
    val apkUrl: String
)
