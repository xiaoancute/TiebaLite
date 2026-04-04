@file:OptIn(ExperimentalSerializationApi::class)

package com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class Factory(
    private val serializer: Serializer,
) : Converter.Factory() {
    @Suppress("RedundantNullableReturnType") // Retaining interface contract.
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        val loader = runCatching { serializer.serializer(type) }.getOrNull()
        if (loader != null) {
            return Converter { body: ResponseBody ->
                serializer.fromResponseBody(loader, body)
            }
        }

        val delegate = runCatching {
            retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        }.getOrNull() ?: return null

        Log.d("Serializer", "Falling back to delegate converter for $type.")
        return Converter { body: ResponseBody -> delegate.convert(body) }
    }
}

@JvmName("create")
fun StringFormat.asConverterFactory(): Converter.Factory {
    return Factory(Serializer.FromString(this))
}
