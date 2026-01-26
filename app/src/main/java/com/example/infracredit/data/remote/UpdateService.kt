package com.example.infracredit.data.remote

import com.example.infracredit.domain.model.UpdateInfo
import retrofit2.http.GET
import retrofit2.http.Url

interface UpdateService {
    @GET
    suspend fun getUpdateInfo(@Url url: String): UpdateInfo
}
