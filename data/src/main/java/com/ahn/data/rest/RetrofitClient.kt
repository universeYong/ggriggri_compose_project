package com.ahn.data.rest

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.35.231:8080/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}