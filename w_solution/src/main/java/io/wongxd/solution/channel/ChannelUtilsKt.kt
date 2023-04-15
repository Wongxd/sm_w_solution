
package io.wongxd.solution.channel

import android.content.Context
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

object ChannelUtilsKt {
    private const val CENTRAL_DIRECTORY_END_SIGN = 0x06054b50
    private const val SIGNATURE_MAGIC_NUMBER = "APK Sig Block 42"
    private const val CHANNEL_KV_ID = 0x010101
    private var sChannelInited = false
    private var sChannel = "baidu"

    @JvmOverloads
    fun getChannel(context: Context, default: String = sChannel): String {
        return getSignatureInfo(context, default)
    }

    private fun getSignatureInfo(context: Context, default: String): String {
        if (sChannelInited) {
            return sChannel
        }
        sChannel = default
        val applicationInfo = context.applicationInfo
        val sourceDir = applicationInfo.sourceDir
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(sourceDir)
            val zipComment = zipFile.comment
            var commentLength = 0
            if (!TextUtils.isEmpty(zipComment)) {
                commentLength = zipComment.toByteArray().size
            }
            val file = File(sourceDir)
            val fileLength = file.length()
            val centralEndSignBytes = readReserveData(file, fileLength - 22, 4)
            val centralEndSign = ByteBuffer.wrap(centralEndSignBytes).int
            if (centralEndSign != CENTRAL_DIRECTORY_END_SIGN) {
                sChannel = "unknown1"
                sChannelInited = true
                return sChannel
            }
            val eoCdrLength = (commentLength + 22).toLong()
            val eoCdrOffset = file.length() - eoCdrLength
            val pointer = eoCdrOffset + 16
            val pointerBuffer = readReserveData(file, pointer, 4)
            val centralDirectoryOffset = ByteBuffer.wrap(pointerBuffer).int
            val buffer = readDataByOffset(file, (centralDirectoryOffset - 16).toLong(), 16)
            val checkV2Signature = String(buffer, StandardCharsets.UTF_8)
            if (!TextUtils.equals(checkV2Signature, SIGNATURE_MAGIC_NUMBER)) {
                sChannel = "unknown_v2_error"
                sChannelInited = true
                return sChannel
            }
            val signBlockEnd = (centralDirectoryOffset - 24).toLong()
            val sigSizeInEndBuffer = readReserveData(file, signBlockEnd, 8)
            val sigSizeInEnd = ByteBuffer.wrap(sigSizeInEndBuffer).long
            val signBlockStart = signBlockEnd - sigSizeInEnd + 16
            val sigSizeInStartBuffer = readReserveData(file, signBlockStart, 8)
            val sigSizeInStart = ByteBuffer.wrap(sigSizeInStartBuffer).long
            if (sigSizeInEnd != sigSizeInStart) {
                sChannel = "unknown_sigSize_error"
                sChannelInited = true
                return sChannel
            }
            var curKvOffset = signBlockStart + 8
            for (i in 0..4) {
                val kvSizeBytes = readReserveData(file, curKvOffset, 8)
                val kvSize = ByteBuffer.wrap(kvSizeBytes).long
                val idBuffer = readReserveData(file, curKvOffset + 8, 4)
                val id = ByteBuffer.wrap(idBuffer).int
                if (id == CHANNEL_KV_ID) {
                    val channelSize = (kvSize - 4).toInt()
                    val channelBytes = readDataByOffset(file, curKvOffset + 12, channelSize)
                    sChannel = String(channelBytes, StandardCharsets.UTF_8)
                    sChannelInited = true
                    return sChannel
                }
                curKvOffset += 8 + kvSize
                if (curKvOffset >= signBlockEnd) {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                zipFile?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sChannel
    }

    @Throws(Exception::class)
    private fun readDataByOffset(file: File, offset: Long, length: Int): ByteArray {
        val `is`: InputStream = FileInputStream(file)
        val skipResult = `is`.skip(offset)
        val buffer = ByteArray(length)
        val readResult = `is`.read(buffer, 0, length)
        `is`.close()
        return buffer
    }

    @Throws(Exception::class)
    private fun readReserveData(file: File, offset: Long, length: Int): ByteArray {
        val buffer = readDataByOffset(file, offset, length)
        reserveByteArray(buffer)
        return buffer
    }

    private fun reserveByteArray(bytes: ByteArray) {
        val length = bytes.size
        for (i in 0 until length / 2) {
            val temp = bytes[i]
            bytes[i] = bytes[length - 1 - i]
            bytes[length - 1 - i] = temp
        }
    }
}