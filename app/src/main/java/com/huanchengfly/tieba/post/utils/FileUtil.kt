package com.huanchengfly.tieba.post.utils

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.text.TextUtils
import android.webkit.URLUtil
import android.webkit.MimeTypeMap
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.toastShort
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object FileUtil {
    const val FILE_TYPE_DOWNLOAD = 0
    const val FILE_TYPE_VIDEO = 1
    const val FILE_TYPE_AUDIO = 2
    const val FILE_FOLDER = "TiebaLite"
    private const val URI_IMPORT_FOLDER = "uri-imports"

    fun deleteAllFiles(root: File) {
        val files = root.listFiles()
        if (files != null) for (f in files) {
            if (f.isDirectory) { // 判断是否为文件夹
                deleteAllFiles(f)
                try {
                    f.delete()
                } catch (e: Exception) {
                }
            } else {
                if (f.exists()) { // 判断是否存在
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    /**
     * @param context 上下文对象
     * @param dir     存储目录
     * @return
     */
    fun getFilePath(context: Context, dir: String): String {
        var directoryPath = ""
        //判断SD卡是否可用
        directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(dir)!!.absolutePath
        } else {
            context.filesDir.toString() + File.separator + dir
        }
        val file = File(directoryPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        return directoryPath
    }

    @JvmStatic
    fun resolveToLocalFile(context: Context, uri: Uri?): File? {
        if (uri == null) {
            return null
        }
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            return uri.path?.let(::File)?.takeIf { it.exists() }
        }
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
            return uri.path?.let(::File)?.takeIf { it.exists() }
        }
        return copyContentUriToCache(context, uri)
    }

    @JvmStatic
    fun getRealPathFromUri(context: Context, contentUri: Uri?): String {
        return resolveToLocalFile(context, contentUri)?.absolutePath.orEmpty()
    }

    fun downloadBySystem(context: Context, fileType: Int, url: String?) {
        val fileName = URLUtil.guessFileName(url, null, null)
        downloadBySystem(context, fileType, url, fileName)
    }

    private fun downloadBySystemToPublicDirectory(
        context: Context,
        fileType: Int,
        url: String?,
        fileName: String,
    ) {
        // 指定下载地址
        val request = DownloadManager.Request(Uri.parse(url))
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner()
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true)
        // 允许漫游时下载
        request.setAllowedOverRoaming(false)
        // 设置下载文件保存的路径和文件名
        val directory: String
        directory = when (fileType) {
            FILE_TYPE_VIDEO -> Environment.DIRECTORY_MOVIES
            FILE_TYPE_AUDIO -> Environment.DIRECTORY_PODCASTS
            FILE_TYPE_DOWNLOAD -> Environment.DIRECTORY_DOWNLOADS
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        request.setDestinationInExternalPublicDir(
            directory,
            FILE_FOLDER + File.separator + fileName
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // 添加一个下载任务
        downloadManager.enqueue(request)
    }

    private fun downloadBySystemToAppDirectory(
        context: Context,
        fileType: Int,
        url: String?,
        fileName: String,
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(true)
        request.setAllowedOverRoaming(false)
        val directory = when (fileType) {
            FILE_TYPE_VIDEO -> Environment.DIRECTORY_MOVIES
            FILE_TYPE_AUDIO -> Environment.DIRECTORY_PODCASTS
            FILE_TYPE_DOWNLOAD -> Environment.DIRECTORY_DOWNLOADS
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        request.setDestinationInExternalFilesDir(
            context,
            directory,
            FILE_FOLDER + File.separator + fileName
        )
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        val targetDir = File(
            context.getExternalFilesDir(directory),
            FILE_FOLDER
        ).absolutePath
        context.toastShort(context.getString(R.string.toast_download_app_private_path, targetDir))
    }

    fun downloadBySystem(context: Context, fileType: Int, url: String?, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadBySystemToPublicDirectory(context, fileType, url, fileName)
            return
        }
        downloadBySystemToAppDirectory(context, fileType, url, fileName)
    }

    @JvmStatic
    fun readFile(file: File?): String? {
        if (file == null || !file.exists() || !file.canRead()) {
            return null
        }
        try {
            val `is`: InputStream = FileInputStream(file)
            val length = `is`.available()
            val buffer = ByteArray(length)
            `is`.read(buffer)
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun writeFile(file: File?, content: String, append: Boolean): Boolean {
        if (file == null || !file.exists() || !file.canWrite()) {
            return false
        }
        try {
            val fos = FileOutputStream(file)
            fos.write(content.toByteArray())
            fos.flush()
            fos.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun writeFile(file: File, inputStream: InputStream): Boolean {
        if (!file.exists() || !file.canWrite()) {
            return false
        }
        try {
            val fos = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var byteCount: Int
            while (inputStream.read(buffer).also { byteCount = it } != -1) {
                fos.write(buffer, 0, byteCount)
            }
            fos.flush()
            fos.close()
            inputStream.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    //修改文件扩展名
    fun changeFileExtension(fileName: String, newExtension: String): String {
        if (TextUtils.isEmpty(fileName)) {
            return fileName
        }
        val index = fileName.lastIndexOf(".")
        return if (index == -1) {
            fileName + newExtension
        } else fileName.substring(0, index) + newExtension
    }

    private fun copyContentUriToCache(context: Context, uri: Uri): File? {
        val cacheDir = File(context.cacheDir, URI_IMPORT_FOLDER)
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            return null
        }
        val fileName = createImportFileName(context, uri)
        val targetFile = createUniqueFile(cacheDir, fileName)
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null
            targetFile
        }.getOrNull()
    }

    private fun createImportFileName(context: Context, uri: Uri): String {
        val displayName = queryDisplayName(context, uri)
        val mimeType = context.contentResolver.getType(uri)
        val guessedName = when {
            !displayName.isNullOrBlank() -> displayName
            !mimeType.isNullOrBlank() -> {
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (extension.isNullOrBlank()) {
                    "imported_${System.currentTimeMillis()}"
                } else {
                    "imported_${System.currentTimeMillis()}.$extension"
                }
            }

            else -> URLUtil.guessFileName(uri.toString(), null, mimeType)
        }
        return sanitizeFileName(guessedName)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex == -1) null else cursor.getString(columnIndex)
            }
        }.getOrNull()
    }

    private fun sanitizeFileName(fileName: String): String {
        val sanitized = fileName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
        return if (sanitized.isBlank()) {
            "imported_${System.currentTimeMillis()}"
        } else {
            sanitized
        }
    }

    private fun createUniqueFile(directory: File, fileName: String): File {
        val extensionIndex = fileName.lastIndexOf('.')
        val prefix = if (extensionIndex == -1) fileName else fileName.substring(0, extensionIndex)
        val suffix = if (extensionIndex == -1) "" else fileName.substring(extensionIndex)
        var candidate = File(directory, fileName)
        var index = 1
        while (candidate.exists()) {
            candidate = File(directory, "${prefix}_$index$suffix")
            index++
        }
        return candidate
    }
}
