package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.Companion.INSTANCE
import com.huanchengfly.tieba.post.toMD5
import com.huanchengfly.tieba.post.utils.helios.Base32
import com.huanchengfly.tieba.post.utils.helios.Hasher
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

object UIDUtil {
    val androidId: String
        get() = getAndroidId("")

    fun getAndroidId(defaultValue: String): String {
        return pseudoAndroidId.ifBlank { defaultValue }
    }

    fun getOAID(): String {
        if (App.Config.encodedOAID.isBlank()) return ""
        val raw = "A10-${App.Config.encodedOAID}-"
        val sign = Base32.encode(Hasher.hash(raw.toByteArray()))
        return "$raw$sign"
    }

    fun getAid(): String {
        val raw = "com.helios" + getAndroidId("000000000") + uUID
        val bytes = getSHA1(raw)
        val encoded = Base32.encode(bytes)
        val rawAid = "A00-$encoded-"
        val sign = Base32.encode(Hasher.hash(rawAid.toByteArray()))
        return "$rawAid$sign"
    }

    private fun getSHA1(str: String): ByteArray {
        var sha1: ByteArray = "".toByteArray()
        try {
            val digest = MessageDigest.getInstance("SHA1")
            sha1 = digest.digest(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sha1
    }

    val newCUID: String
        get() = "baidutiebaapp$uUID"

    private val pseudoAndroidId: String
        get() = stableHash("android:$uUID").take(16).lowercase()

    private val pseudoImei: String
        get() = stableDigits("imei:$uUID", 15)

    val cUID: String
        get() {
            val raw = "com.baidu$pseudoAndroidId|$pseudoImei|$uUID"
            return raw.toMD5().uppercase()
        }

    val finalCUID: String
        get() = cUID + "|" + pseudoImei.reversed()

    @get:SuppressLint("ApplySharedPref")
    val uUID: String
        get() {
            var uuid = SharedPreferencesUtil.get(INSTANCE, SharedPreferencesUtil.SP_APP_DATA)
                .getString("uuid", null)
            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
                SharedPreferencesUtil.get(INSTANCE, SharedPreferencesUtil.SP_APP_DATA)
                    .edit()
                    .putString("uuid", uuid)
                    .apply()
            }
            return uuid
        }

    private fun stableHash(seed: String): String =
        getSHA1(seed).joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun stableDigits(seed: String, length: Int): String {
        val digits = buildString(length) {
            var counter = 0
            while (this.length < length) {
                stableHash("$seed:$counter").forEach { ch ->
                    if (this.length >= length) return@forEach
                    append(
                        if (ch.isDigit()) ch
                        else ('0'.code + ((ch.code - 'a'.code) % 10)).toChar()
                    )
                }
                counter++
            }
        }
        return digits.ifBlank { "0".repeat(length) }
    }
}
