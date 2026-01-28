package com.example.infracredit.data.remote

import com.example.infracredit.data.remote.dto.*
import retrofit2.http.*

interface InfracreditApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("auth/profile")
    suspend fun getProfile(): ProfileDto

    @PUT("auth/profile")
    suspend fun updateProfile(@Body profile: ProfileDto): ProfileDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Query("phone") phone: String): GenericResponse

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericResponse

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummaryDto

    @GET("customers")
    suspend fun getCustomers(
        @Query("ownerPhone") ownerPhone: String,
        @Query("deleted") deleted: Boolean = false
    ): List<CustomerDto>

    @POST("customers")
    suspend fun addCustomer(@Body customer: CustomerDto): CustomerDto

    @GET("customers/{id}")
    suspend fun getCustomer(@Path("id") id: String): CustomerDto

    @PUT("customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: CustomerDto): CustomerDto

    @DELETE("customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String): GenericResponse

    @GET("customers/{id}/transactions")
    suspend fun getTransactions(
        @Path("id") customerId: String,
        @Query("ownerPhone") ownerPhone: String
    ): List<TransactionDto>

    @POST("transactions")
    suspend fun addTransaction(@Body transaction: TransactionDto): TransactionDto

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: String, @Body transaction: TransactionDto): TransactionDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): GenericResponse

    companion object {
        const val BASE_URL = "https://infracredit-backend.onrender.com/v1/"
    }
}
