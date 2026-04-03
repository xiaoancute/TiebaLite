package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization.asConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.interceptors.CommonHeaderInterceptor
import com.huanchengfly.tieba.post.api.retrofit.interfaces.GitHubReleaseApi
import com.huanchengfly.tieba.post.update.AppUpdateJson
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object GitHubApi {
    private val connectionPool = ConnectionPool()

    val releaseService: GitHubReleaseApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(AppUpdateJson.asConverterFactory())
            .client(
                OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                        CommonHeaderInterceptor(
                            Header.USER_AGENT to { System.getProperty("http.agent") },
                        )
                    )
                    .connectionPool(connectionPool)
                    .build()
            )
            .build()
            .create(GitHubReleaseApi::class.java)
    }
}
