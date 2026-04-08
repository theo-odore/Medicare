package com.medicare.app

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {

    fun getApi(context: android.content.Context): SupabaseApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val startRequest = chain.request()
                val builder = startRequest.newBuilder()
                    .addHeader("apikey", Constants.SUPABASE_KEY)
                    .addHeader("Content-Type", "application/json")
                
                val token = SessionManager.getToken(context)
                if (!token.isNullOrEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                } else {
                    builder.addHeader("Authorization", "Bearer ${Constants.SUPABASE_KEY}")
                }
                
                chain.proceed(builder.build())
            }
            .build()
            
        return Retrofit.Builder()
            .baseUrl(Constants.SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
    
    // Legacy support for non-context calls if any (should migrate all)
    val api: SupabaseApi by lazy {
         Retrofit.Builder()
            .baseUrl(Constants.SUPABASE_URL)
            .client(OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("apikey", Constants.SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer ${Constants.SUPABASE_KEY}") // Default Anon
                        .build()
                    chain.proceed(request)
                }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
