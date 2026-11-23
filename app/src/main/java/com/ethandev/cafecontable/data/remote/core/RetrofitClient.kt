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



package com.ethandev.cafecontable.data.remote.core

import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.dispositivo.DispositivoApi
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


            val ruta = originalRequest.url().encodedPath()

            val requiereTokenAutomatico =
                ruta != "/api/auth/login" &&
                        ruta != "/api/auth/refresh" &&
                        ruta != "/api/auth/refresh-firmado" &&
                        ruta != "/api/auth/refresh-firmado/recuperar"

            if (
                !token.isNullOrBlank() &&
                requiereTokenAutomatico &&
                originalRequest.header("Authorization") == null
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


    val dispositivoApi: DispositivoApi by lazy {
        retrofit.create(DispositivoApi::class.java)
    }

}

