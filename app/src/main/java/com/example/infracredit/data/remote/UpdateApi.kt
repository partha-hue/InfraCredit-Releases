package com.example.infracredit.data.remote

import com.example.infracredit.data.remote.dto.VersionDto
import retrofit2.http.GET
import retrofit2.http.Url

interface UpdateApi {
    @GET
    suspend fun checkUpdate(@Url url: String): VersionDto
}
