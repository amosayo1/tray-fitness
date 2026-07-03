package com.gymsync.data.api

import com.gymsync.BuildConfig
import com.gymsync.data.local.TokenManager
import com.gymsync.data.model.request.AiChatRequest
import com.gymsync.data.model.response.AiChatResponse
import com.gymsync.data.model.response.ApiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        authToken?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(request.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GymSyncApi = retrofit.create(GymSyncApi::class.java)

    private val aiScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun sendPetChat(
        message: String,
        petType: Int,
        petName: String,
        userName: String,
        context: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        aiScope.launch {
            try {
                val request = AiChatRequest(
                    message = message,
                    petType = petType,
                    petName = petName,
                    userName = userName,
                    context = context
                )
                val response = api.aiChat(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val reply = body?.data?.message ?: "Woof! 🐕"
                    withContext(Dispatchers.Main) { onSuccess(reply) }
                } else {
                    withContext(Dispatchers.Main) { onError() }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onError() }
            }
        }
    }
}
