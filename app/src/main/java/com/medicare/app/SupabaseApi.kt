package com.medicare.app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SupabaseApi {
    @POST("/auth/v1/token?grant_type=password")
    fun login(@Body request: AuthRequest): Call<AuthResponse>
    
    @POST("/auth/v1/signup")
    fun signup(@Body request: AuthRequest): Call<AuthResponse>

    @POST("/rest/v1/medicines")
    fun addMedicine(@Body medicine: MedicineRequest): Call<Void>

    @retrofit2.http.GET("/rest/v1/medicines?select=*")
    fun getMedicines(@retrofit2.http.Query("user_id") userId: String = "eq.USER_ID_PLACEHOLDER"): Call<List<MedicineResponse>>

    @retrofit2.http.DELETE("/rest/v1/medicines")
    fun deleteMedicine(@retrofit2.http.Query("id") id: String): Call<Void>
}

data class MedicineResponse(
    val id: String,
    val name: String,
    val dosage: String,
    val instructions: String,
    val stock: Int,
    val reminder_time: String?,
    val created_at: String
)

data class MedicineRequest(
    val user_id: String,
    val name: String,
    val dosage: String,
    val instructions: String,
    val stock: Int,
    val reminder_time: String
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: User?
)

data class User(
    val id: String,
    val aud: String,
    val role: String,
    val email: String,
    val created_at: String
)
