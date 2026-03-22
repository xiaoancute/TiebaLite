package com.huanchengfly.tieba.post.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions.getExtensionVersion
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.registerPickMediasLauncher(callback: (PickMediasResult) -> Unit): ActivityResultLauncher<PickMediasRequest> {
    return registerForActivityResult(
        PickMediasContract
    ) {
        callback(it)
    }
}

fun isPhotoPickerAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getExtensionVersion(Build.VERSION_CODES.R) >= 2
    } else {
        false
    }
}

fun shouldUsePhotoPicker(): Boolean {
    return isPhotoPickerAvailable()
}

fun Intent.getClipDataUris(): List<Uri> {
    // Use a LinkedHashSet to maintain any ordering that may be
    // present in the ClipData
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return emptyList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return resultSet.toList()
}

data class PickMediasRequest(
    val id: String = "",
    val maxItems: Int = 1,
    val mediaType: MediaType = ImageAndVideo
) {
    sealed interface MediaType

    data object ImageOnly : MediaType

    data object VideoOnly : MediaType

    data object ImageAndVideo : MediaType

    data class SingleMimeType(val mimeType: String) : MediaType

    companion object {
        internal fun getMimeType(input: MediaType): String? {
            return when (input) {
                is ImageOnly -> "image/*"
                is VideoOnly -> "video/*"
                is SingleMimeType -> input.mimeType
                is ImageAndVideo -> null
            }
        }

        internal fun getMimeTypes(input: MediaType): Array<String>? {
            return when (input) {
                is ImageOnly -> arrayOf("image/*")
                is VideoOnly -> arrayOf("video/*")
                is SingleMimeType -> arrayOf(input.mimeType)
                is ImageAndVideo -> arrayOf("image/*", "video/*")
            }
        }
    }
}

data class PickMediasResult(
    val id: String,
    val uris: List<Uri>
)

object PickMediasContract : ActivityResultContract<PickMediasRequest, PickMediasResult>() {
    private var curRequestId: String? = null

    val hasCurrentRequest: Boolean
        get() = curRequestId != null

    @SuppressLint("InlinedApi")
    override fun createIntent(context: Context, input: PickMediasRequest): Intent {
        curRequestId = input.id
        if (shouldUsePhotoPicker()) {
            return Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = PickMediasRequest.getMimeType(input.mediaType)
                if (input.maxItems > 1) {
                    putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, input.maxItems)
                }
            }
        }
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = PickMediasRequest.getMimeType(input.mediaType) ?: "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, PickMediasRequest.getMimeTypes(input.mediaType))
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, input.maxItems > 1)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PickMediasResult {
        val id = curRequestId + ""
        curRequestId = null
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return PickMediasResult(id, emptyList())
        }
        return PickMediasResult(id, intent.getClipDataUris())
    }
}
