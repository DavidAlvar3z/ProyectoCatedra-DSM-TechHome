package com.techhome.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

data class BestBuyResponse(
    val products: List<Product> = emptyList(),
    val total: Int = 0,
    val from: Int = 0,
    val to: Int = 0
)

data class Product(
    val sku: String = "",
    val name: String = "",
    val salePrice: Double = 0.0,
    val regularPrice: Double = 0.0,
    val image: String = "",
    val url: String = ""
)

interface BestBuyApiService {

    @GET
    fun getProductsByCategory(
        @Url url: String
    ): Call<BestBuyResponse>

    companion object {
        private const val BASE_URL = "https://api.bestbuy.com/v1/"
        const val API_KEY = "jykGYMAH7bCoUPhJJGkmo0NS"

        fun create(): BestBuyApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(BestBuyApiService::class.java)
        }

        // ✅ CON PAGINACIÓN
        fun buildCategoryUrl(categoryId: String, page: Int = 1, pageSize: Int = 20): String {
            return "products(categoryPath.id=$categoryId)?format=json&show=sku,name,salePrice,regularPrice,image,url&page=$page&pageSize=$pageSize&apiKey=$API_KEY"
        }
    }
}