//
//package com.ethandev.cafecontable.data.remote
//
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object RetrofitClient {
//
//    private const val BASE_URL =
//        "https://192.168.101.5:7079/"
//
//    val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//    val api: CafeContableApi by lazy {
//        retrofit.create(CafeContableApi::class.java)
//    }
//}



package com.ethandev.cafecontable.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://192.168.101.5:7079/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->

            val originalRequest = chain.request()

            val token = AuthTokenManager.obtenerToken()

            val requestBuilder =
                originalRequest.newBuilder()

            if (!token.isNullOrBlank() &&
                originalRequest.url().encodedPath() != "/api/auth/login"
            ) {
                requestBuilder.header(
                    "Authorization",
                    "Bearer $token"
                )
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

    val api: CafeContableApi by lazy {
        retrofit.create(CafeContableApi::class.java)
    }
}

