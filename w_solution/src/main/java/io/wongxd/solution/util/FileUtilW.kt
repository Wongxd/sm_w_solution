package io.wongxd.solution.util

import android.app.Activity
import android.content.*
import android.content.res.AssetManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object FileUtilW {

    private const val tempDirName = "fb_solution"

    private val tag = FileUtilW::class.java.name

    fun getFileUri(ctx: Context, filePath: String): Uri {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", File(filePath))
        } else {
            Uri.fromFile(File(filePath))
        }
        return uri
    }

    @Throws(IOException::class)
    fun readAssetFileUtf8String(assetManager: AssetManager, filename: String): String {
        val bytes = readAssetFileContent(assetManager, filename)
        return String(bytes, Charsets.UTF_8)
    }

    @Throws(IOException::class)
    private fun readAssetFileContent(assetManager: AssetManager, filename: String): ByteArray {
        Log.i(tag, " try to read asset file :$filename")
        val `is` = assetManager.open(filename)
        val size = `is`.available()
        val buffer = ByteArray(size)
        val realSize = `is`.read(buffer)
        if (realSize != size) {
            throw IOException("realSize is not equal to size: $realSize : $size")
        }
        `is`.close()
        return buffer
    }

    /**
     * 向指定的文件中写入指定的数据
     */
    fun writeFileDataAll(context: Context, filename: String, content: String) {
        try {
            val fos = context.openFileOutput(filename, Context.MODE_PRIVATE) //获得FileOutputStream
            //将要写入的字符串转换为byte数组
            val bytes = content.toByteArray()
            fos.write(bytes) //将byte数组写入文件
            fos.close() //关闭文件输出流
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打开指定文件，读取其数据，返回字符串对象
     */
    fun readFileDataAll(context: Context, fileName: String): String {
        var result = ""
        try {
            val fis = context.openFileInput(fileName)
            //获取文件长度
            val length = fis.available()
            val buffer = ByteArray(length)
            fis.read(buffer)
            //将byte数组转换成指定格式的字符串
            result = String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun getAppVideoPath(folderName: String = tempDirName): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // full path
            "${Environment.getExternalStorageDirectory().absolutePath}/" +
                    "${Environment.DIRECTORY_MOVIES}/$folderName/"
        } else {
            // relative path
            "${Environment.DIRECTORY_MOVIES}/$folderName/"
        }
    }

    fun getAppPicturePath(folderName: String = tempDirName): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // full path
            "${Environment.getExternalStorageDirectory().absolutePath}/" +
                    "${Environment.DIRECTORY_PICTURES}/$folderName/"
        } else {
            // relative path
            "${Environment.DIRECTORY_PICTURES}/$folderName/"
        }
    }

    fun getAppDownloadPath(folderName: String = tempDirName): String =
        "${Environment.DIRECTORY_DOWNLOADS}/$folderName/"

    private fun saveBitmap2PicturePublicFolder(
        ctx: Context,
        bitmap: Bitmap,
        displayName: String = "${System.currentTimeMillis()}.jpg",
        mimeType: String = "image/jpeg",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Uri? {
        val uri =
            ctx.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                generatorSavePicToPublicFolderContentValues(displayName, mimeType)
            )
        uri?.also {
            val outputStream = ctx.contentResolver.openOutputStream(it)
            outputStream?.also { os ->
                bitmap.compress(compressFormat, 100, os)
                os.close()
                Toast.makeText(ctx, "添加图片成功", Toast.LENGTH_SHORT).show()
                return uri
            }
        }
        return null
    }

    /**
     * 将在线GIF图片保存到本地
     * 与文件下载保存十分相似
     */
    private fun saveNetworkGIFToPicturePublicFolder(
        ctx: Context,
        photoUrl: String,
        photoName: String
    ) {
        val uri =
            ctx.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                generatorSavePicToPublicFolderContentValues(photoName, "image/gif")
            )
        thread {
            val url = URL(photoUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val inputStream = connection.inputStream
            val bis = BufferedInputStream(inputStream)
            uri?.also {
                val outputStream = ctx.contentResolver.openOutputStream(uri) ?: return@thread
                val bos = BufferedOutputStream(outputStream)
                val buffer = ByteArray(1024)
                var bytes = bis.read(buffer)
                while (bytes >= 0) {
                    bos.write(buffer, 0, bytes)
                    bos.flush()
                    bytes = bis.read(buffer)
                }
                bos.close()
            }
            bis.close()
        }
    }

    private fun generatorSavePicToPublicFolderContentValues(
        displayName: String,
        mimeType: String
    ): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        val path = getAppPicturePath()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        } else {
            val fileDir = File(path)
            if (!fileDir.exists()) {
                fileDir.mkdir()
            }
            contentValues.put(MediaStore.MediaColumns.DATA, path + displayName)
        }
        return contentValues
    }

    /**
     * 将图片添加到相册
     *
     *  val displayName = "${System.currentTimeMillis()}.jpg"
     *  val mimeType = "image/jpeg"
     *  val compressFormat = Bitmap.CompressFormat.JPEG
     *
     */
    fun addBitmapToAlbum(
        context: Context,
        bitmap: Bitmap,
        displayName: String = "${System.currentTimeMillis()}.jpg",
        mimeType: String = "image/jpeg",
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ) {
        val values = generatorSavePicToPublicFolderContentValues(displayName, mimeType)
        val uri =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream)
                outputStream.close()
            }
        }
    }

    /**
     * 将图片添加到相册
     */
    fun writeInputStreamToAlbum(
        context: Context,
        inputStream: InputStream,
        displayName: String = "${System.currentTimeMillis()}.jpg",
        mimeType: String = "image/jpeg",
    ) {
        val values = generatorSavePicToPublicFolderContentValues(displayName, mimeType)
        val bis = BufferedInputStream(inputStream)
        val uri =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val bos = BufferedOutputStream(outputStream)
                val buffer = ByteArray(1024)
                var bytes = bis.read(buffer)
                while (bytes >= 0) {
                    bos.write(buffer, 0, bytes)
                    bos.flush()
                    bytes = bis.read(buffer)
                }
                bos.close()
            }
        }
        bis.close()
    }

    /**
     * 将视频添加到相册
     */
    fun writeVideoInputStreamToAlbum(
        context: Context,
        inputStream: InputStream,
        displayName: String = "${System.currentTimeMillis()}.mp4",
        mimeType: String = "video/mp4",
    ) {
        val values = generatorSaveVideoToPublicFolderContentValues(displayName, mimeType)
        val bis = BufferedInputStream(inputStream)
        val uri =
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val bos = BufferedOutputStream(outputStream)
                val buffer = ByteArray(1024)
                var bytes = bis.read(buffer)
                while (bytes >= 0) {
                    bos.write(buffer, 0, bytes)
                    bos.flush()
                    bytes = bis.read(buffer)
                }
                bos.close()
            }
        }
        bis.close()
    }

    private fun generatorSaveVideoToPublicFolderContentValues(
        displayName: String,
        mimeType: String
    ): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.Video.VideoColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        val path = getAppVideoPath()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Video.VideoColumns.RELATIVE_PATH, path)
        } else {
            val fileDir = File(path)
            if (!fileDir.exists()) {
                fileDir.mkdir()
            }
            contentValues.put(MediaStore.Video.VideoColumns.DATA, path + displayName)
        }
        return contentValues
    }

    fun videoSaveToNotifyGalleryToRefreshWhenVersionGreaterQ(context: Context, destFile: File) {
        val values = ContentValues()
        val uriSavedVideo: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Folder")
            values.put(MediaStore.Video.Media.TITLE, destFile.name)
            values.put(MediaStore.Video.Media.DISPLAY_NAME, destFile.name)
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            val collection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            context.contentResolver.insert(collection, values)
        } else {
            values.put(MediaStore.Video.Media.TITLE, destFile.name)
            values.put(MediaStore.Video.Media.DISPLAY_NAME, destFile.name)
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            values.put(MediaStore.Video.Media.DATA, destFile.absolutePath)
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val pfd: ParcelFileDescriptor?
        try {
            pfd = context.contentResolver.openFileDescriptor(uriSavedVideo!!, "w")
            val out = FileOutputStream(pfd!!.fileDescriptor)
            val `in` = FileInputStream(destFile)
            val buf = ByteArray(8192)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`.close()
            pfd.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(uriSavedVideo!!, values, null, null)
        }
    }

    /**
     * 下载文件到Download目录
     *
     * 只能在Android 10或更高的系统版本上运行，因为MediaStore.Downloads是Android 10中新增的API。
     * 至于Android 9及以下的系统版本，请你仍然使用之前的代码来进行文件下载
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadFileForQ(context: Context, fileUrl: String, fileName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(
                context,
                "You must use device running Android 10 or higher",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val inputStream = connection.inputStream
                val bis = BufferedInputStream(inputStream)
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, getAppDownloadPath())
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        val bos = BufferedOutputStream(outputStream)
                        val buffer = ByteArray(1024)
                        var bytes = bis.read(buffer)
                        while (bytes >= 0) {
                            bos.write(buffer, 0, bytes)
                            bos.flush()
                            bytes = bis.read(buffer)
                        }
                        bos.close()
                    }
                }
                bis.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private const val PICK_FILE = 1

    /**
     * 读取SD卡上非图片、音频、视频类的文件，比如说打开一个PDF文件
     * 这个时候就不能再使用MediaStore API了，而是要使用文件选择器。
     * 但是，我们不能再像之前的写法那样，自己写一个文件浏览器，然后从中选取文件，而是必须要使用手机系统中内置的文件选择器
     */
    fun pickFile(aty: FragmentActivity, pickResult: (InputStream) -> Unit) {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"

        SimpleOnActivityResult.simpleForResult(aty)
            .startForResult(intent) { requestCode: Int, resultCode: Int, data: Intent? ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val inputStream = aty.contentResolver.openInputStream(uri)
                        // 执行文件读取操作
                        inputStream?.let(pickResult)
                    }
                }
            }
    }

    /**
     * 编写一个文件复制功能，将Uri对象所对应的文件复制到应用程序的关联目录下
     */
    fun copyUriToExternalFilesDir(context: Context, uri: Uri, fileName: String) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempDir = context.getExternalFilesDir("temp")
        if (inputStream != null && tempDir != null) {
            val file = File("$tempDir/$fileName")
            val fos = FileOutputStream(file)
            val bis = BufferedInputStream(inputStream)
            val bos = BufferedOutputStream(fos)
            val byteArray = ByteArray(1024)
            var bytes = bis.read(byteArray)
            while (bytes > 0) {
                bos.write(byteArray, 0, bytes)
                bos.flush()
                bytes = bis.read(byteArray)
            }
            bos.close()
            fos.close()
        }
    }

    fun getFilePathByUri(context: Context, uri: Uri): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriToFileApiQ(context, uri)?.absolutePath ?: ""
        } else {
            uriToFileUnderApiQ(context, uri) ?: ""
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun uriToFileApiQ(context: Context, uri: Uri?): File? {
        if (uri == null) return null
        var file: File? = null
        //android10以上转换
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            file = File(uri.path!!)
        } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //把文件复制到沙盒目录
            val contentResolver = context.contentResolver
            //            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
//                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
            val displayName = "${System.currentTimeMillis()}_uriToFileApiQTempFile.${
                MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
            }"

//            注释掉的方法可以获取到原文件的文件名，但是比较耗时
//            Cursor cursor = contentResolver.query(uri, null, null, null, null);
//            if (cursor.moveToFirst()) {
//                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));}
            try {
                val `is` = contentResolver.openInputStream(uri)
                val cache = File(context.externalCacheDir!!.absolutePath, displayName)
                if (cache.exists()) cache.delete()
                val fos = FileOutputStream(cache)
                FileUtils.copy(`is`!!, fos)
                file = cache
                fos.close()
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    private fun uriToFileUnderApiQ(context: Context, uri: Uri): String? {

        fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor =
                    context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        var path: String? = null
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            path = uri.path
            return path
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT == uri.scheme && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (columnIndex > -1) {
                        path = cursor.getString(columnIndex)
                    }
                }
                cursor.close()
            }
            return path
        }
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT == uri.scheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        return path
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    path = getDataColumn(context, contentUri, null, null)
                    return path
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    path = getDataColumn(context, contentUri, selection, selectionArgs)
                    return path
                }
            }
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}