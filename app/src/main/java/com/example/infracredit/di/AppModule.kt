package com.example.infracredit.di

import com.example.infracredit.data.local.TokenManager
import com.example.infracredit.data.local.dao.CustomerDao
import com.example.infracredit.data.local.dao.TransactionDao
import com.example.infracredit.data.remote.AuthInterceptor
import com.example.infracredit.data.remote.InfracreditApi
import com.example.infracredit.data.remote.UpdateService
import com.example.infracredit.data.repository.AuthRepositoryImpl
import com.example.infracredit.data.repository.CustomerRepositoryImpl
import com.example.infracredit.data.repository.DashboardRepositoryImpl
import com.example.infracredit.data.repository.TransactionRepositoryImpl
import com.example.infracredit.data.repository.UpdateRepositoryImpl
import com.example.infracredit.domain.repository.AuthRepository
import com.example.infracredit.domain.repository.CustomerRepository
import com.example.infracredit.domain.repository.DashboardRepository
import com.example.infracredit.domain.repository.TransactionRepository
import com.example.infracredit.domain.repository.UpdateRepository
import com.example.infracredit.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true 
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideInfracreditApi(client: OkHttpClient, json: Json): InfracreditApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(InfracreditApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(InfracreditApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUpdateService(json: Json): UpdateService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://example.com/") // Base URL is required but overwritten by @Url
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(UpdateService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: InfracreditApi, tokenManager: TokenManager): AuthRepository {
        return AuthRepositoryImpl(api, tokenManager)
    }

    @Provides
    @Singleton
    fun provideUpdateRepository(updateService: UpdateService): UpdateRepository {
        return UpdateRepositoryImpl(updateService)
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(api: InfracreditApi, customerDao: CustomerDao): CustomerRepository {
        return CustomerRepositoryImpl(api, customerDao)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(api: InfracreditApi, transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(api, transactionDao)
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(api: InfracreditApi): DashboardRepository {
        return DashboardRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideThemeManager(): ThemeManager {
        return ThemeManager()
    }
}
