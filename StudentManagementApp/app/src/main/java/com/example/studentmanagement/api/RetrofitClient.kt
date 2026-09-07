package com.example.studentmanagement.api

import com.example.studentmanagement.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit singleton.
 *
 * CONFIGURATION:
 * ──────────────────────────────────────────────────────
 * To point to a different backend, change Constants.BASE_URL.
 * Do NOT change the URL here — keep this file environment-agnostic.
 *
 * Logging is enabled at BODY level. For a production release,
 * wrap the interceptor addition in a BuildConfig.DEBUG check.
 * ──────────────────────────────────────────────────────
 */
object RetrofitClient {

    /** OkHttpClient with logging + timeout configuration. */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Log full request/response body during development.
            // Change to NONE or BASIC before releasing to production.
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /** Retrofit instance pointed at Constants.BASE_URL. */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            // ← Change BASE_URL in Constants.kt; do NOT edit here.
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** The API service implementation created by Retrofit. */
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
