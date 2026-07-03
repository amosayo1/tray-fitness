package com.gymsync.di

import com.gymsync.BuildConfig
import com.gymsync.data.api.GymSyncApi
import com.gymsync.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshInterceptorOkHttp

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @AuthInterceptorOkHttp
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = tokenManager.accessToken
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @RefreshInterceptorOkHttp
    fun provideRefreshInterceptor(
        tokenManager: TokenManager
    ): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            if (response.code == 401
                && tokenManager.refreshToken != null
                && !request.url.encodedPath.contains("auth/refresh")
            ) {
                response.close()

                val refreshSuccessful = runBlocking {
                    try {
                        val refreshApi = Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(OkHttpClient.Builder()
                                .addInterceptor(HttpLoggingInterceptor().apply {
                                    level = HttpLoggingInterceptor.Level.NONE
                                })
                                .connectTimeout(15, TimeUnit.SECONDS)
                                .readTimeout(15, TimeUnit.SECONDS)
                                .build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(GymSyncApi::class.java)

                        val refreshResult = refreshApi.refresh(
                            com.gymsync.data.model.request.RefreshRequest(
                                tokenManager.refreshToken ?: return@runBlocking false
                            )
                        )
                        if (refreshResult.isSuccessful) {
                            val body = refreshResult.body()
                            if (body != null && body.succeeded && body.data != null) {
                                tokenManager.accessToken = body.data.accessToken
                                tokenManager.refreshToken = body.data.refreshToken
                                true
                            } else {
                                tokenManager.clear()
                                false
                            }
                        } else {
                            tokenManager.clear()
                            false
                        }
                    } catch (_: Exception) {
                        false
                    }
                }

                if (refreshSuccessful) {
                    val newToken = tokenManager.accessToken ?: ""
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return@Interceptor chain.proceed(newRequest)
                }
            }

            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @AuthInterceptorOkHttp authInterceptor: Interceptor,
        @RefreshInterceptorOkHttp refreshInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(refreshInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideGymSyncApi(okHttpClient: OkHttpClient): GymSyncApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GymSyncApi::class.java)
    }
}