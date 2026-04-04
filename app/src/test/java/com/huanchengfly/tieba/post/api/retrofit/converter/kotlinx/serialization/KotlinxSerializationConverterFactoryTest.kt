package com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization

import com.huanchengfly.tieba.post.update.AppUpdateJson
import com.huanchengfly.tieba.post.update.GitHubReleaseSummary
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET

class KotlinxSerializationConverterFactoryTest {
    @Test
    fun converterFactorySupportsListResponseTypesWithoutFallbackConverter() = runBlocking {
        val api = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(AppUpdateJson.asConverterFactory())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(
                                """
                                [
                                  {
                                    "tag_name": "v4.0.0-recovery.12",
                                    "prerelease": true,
                                    "assets": []
                                  }
                                ]
                                """.trimIndent().toResponseBody("application/json".toMediaType())
                            )
                            .build()
                    }
                    .build()
            )
            .build()
            .create(TestGitHubReleaseApi::class.java)

        val releases = api.listReleases()

        assertEquals(1, releases.size)
        assertEquals("v4.0.0-recovery.12", releases.first().tagName)
        assertTrue(releases.first().prerelease)
    }

    private interface TestGitHubReleaseApi {
        @GET("releases")
        suspend fun listReleases(): List<GitHubReleaseSummary>
    }
}
