package io.wongxd.solution.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import java.io.*
import java.util.*
import kotlin.math.max
import kotlin.math.min


object BitmapUtilW {

    @Throws(FileNotFoundException::class)
    fun loadBitmap(picturePath: String): Bitmap? {
        val opt = BitmapFactory.Options()
        opt.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(picturePath, opt)
        // 获取到这个图片的原始宽度和高度
        val picWidth = opt.outWidth
        val picHeight = opt.outHeight
        // 获取画布中间方框的宽度和高度
        val screenWidth: Int = 1080
        val screenHeight: Int = 1920
        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opt.inSampleSize = 1
        // 根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > screenWidth) opt.inSampleSize = picWidth / screenWidth
        } else {
            if (picHeight > screenHeight) opt.inSampleSize = picHeight / screenHeight
        }
        // 生成有像素经过缩放了的bitmap
        opt.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(picturePath, opt)
        if (bitmap == null) {
            throw FileNotFoundException("Couldn't open $picturePath")
        }
        return bitmap
    }

    /**
     *
     * 对图片压缩
     *
     */
    fun compressPicture(imgPath: String?): Bitmap? {
        imgPath ?: return null

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imgPath, options)
        options.inSampleSize =
            calculateInSampleSize(options, 1080, 1920)
        options.inJustDecodeBounds = false
        val afterCompressBm = BitmapFactory.decodeFile(imgPath, options)
        //      //默认的图片格式是Bitmap.Config.ARGB_8888
        return afterCompressBm
    }


    fun blurBitmap(context: Context?, bitmap: Bitmap): Bitmap? {
        //用需要创建高斯模糊bitmap创建一个空的bitmap
        val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // 初始化Renderscript，该类提供了RenderScript context，创建其他RS类之前必须先创建这个类，其控制RenderScript的初始化，资源管理及释放
        val rs = RenderScript.create(context)
        // 创建高斯模糊对象
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        // 创建Allocations，此类是将数据传递给RenderScript内核的主要方 法，并制定一个后备类型存储给定类型
        val allIn = Allocation.createFromBitmap(rs, bitmap)
        val allOut = Allocation.createFromBitmap(rs, outBitmap)
        //设定模糊度(注：Radius最大只能设置25.f)
        blurScript.setRadius(15f)
        // Perform the Renderscript
        blurScript.setInput(allIn)
        blurScript.forEach(allOut)
        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap)
        // recycle the original bitmap
        // bitmap.recycle();
        // After finishing everything, we destroy the Renderscript.
        rs.destroy()
        return outBitmap
    }

    fun viewToImageFilePath(view: View): String {
        val bitmap = loadBitmapFromView(view)
        return bitmap2File(bitmap, view.context.cacheDir)
    }

    fun bitmap2File(
        bitmap: Bitmap,
        parentFile: File?,
        recycle: Boolean = true
    ): String {
        var fos: FileOutputStream? = null
        try {
            val file =
                File(
                    parentFile,
                    Calendar.getInstance().timeInMillis.toString() + ".jpeg"
                )
            if (file.exists()) {
                file.delete()
            }
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            return file.path
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {

            }
            if (recycle)
                bitmap.recycle()
        }
    }

    fun loadBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
//        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 绘制已经测量过的View
     */
    private fun getBitmapFromMeasureView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 先测量和布局，再生成Bitmap
     */
    fun getBitmap(view: View): Bitmap? {
        // 测量
        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(
                view.context.resources.displayMetrics.widthPixels,
                View.MeasureSpec.AT_MOST
            )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            view.context.resources.displayMetrics.heightPixels,
            View.MeasureSpec.AT_MOST
        )
        view.measure(widthSpec, heightSpec)
        // 布局
        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        view.layout(0, 0, measuredWidth, measuredHeight)
        // 绘制
        val width = view.width
        val height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 将用户输入的图片从上到下拼接到一起
     * 如果图片宽度不一致，则进行等比缩放后再拼接
     */
    fun concat(vararg oriBitmaps: Bitmap): Bitmap? {
        val bitmaps = mutableListOf<Bitmap>()
        bitmaps.addAll(oriBitmaps)
        // 获取图片的最大宽度
        var maxWidth = bitmaps[0].width
        for (bitmap in bitmaps) {
            maxWidth = Math.max(maxWidth, bitmap.width)
        }
        // 对图片进行缩放并计算拼接后的图片高度
        var totalHeight = 0
        for (i in 0 until bitmaps.size) {
            val bitmap = bitmaps[i]
            val width = bitmap.width
            val height = bitmap.height
            val scale = maxWidth * 1f / width
            val scaleHeight = (height * scale).toInt()
            totalHeight += scaleHeight
            bitmaps[i] = Bitmap.createScaledBitmap(bitmap, maxWidth, scaleHeight, false)
        }
        // 从上到下依次拼接
        val newBitmap = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(newBitmap)
        var offsetTop = 0
        for (bitmap in bitmaps) {
            val height = bitmap.height
            canvas.drawBitmap(bitmap, 0f, offsetTop.toFloat(), null)
            offsetTop += height
        }
        return newBitmap
    }

    /**
     * 读取图片尺寸和类型
     */
    private fun getBitmapOptionsOnlyWidthAndHeight(imagePath: String): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        //设置为true之后，decode**方法将会不分配内存，但是可以获取图片的宽高，类型
        options.inJustDecodeBounds = true //just decode bounds意味这只加载边界
        BitmapFactory.decodeFile(imagePath, options)
        return options
    }

    /**
     * 获取图片应该被缩小的倍数
     */
    private fun getScaleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val imageWidth = options.outWidth
        val imageHeight = options.outHeight
        var scaleSize = 1

        //如果图片的实际尺寸大于了实际需要的，就计算缩放倍数
        if (imageWidth > reqWidth || imageHeight > reqHeight) {
            val halfWidth = imageWidth / 2
            val halfHeight = imageHeight / 2
            while (halfWidth / scaleSize > reqWidth && halfHeight / scaleSize > reqHeight) {
                scaleSize *= 2
            }
        }
        return scaleSize
    }

    fun decodeSampledBitmapFromResource(
        res: Resources?,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }


    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        var height = options.outHeight
        var width = options.outWidth
        var inSampleSize = 1
        while (height > maxHeight || width > maxWidth) {
            height = height shr 1
            width = width shr 1
            inSampleSize = inSampleSize shl 1
        }
        return inSampleSize
    }


    private fun compressImage(image: Bitmap): Bitmap? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (baos.toByteArray().size / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() //重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos) //这里压缩options%，把压缩后的数据存放到baos中
            options -= 10 //每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray()) //把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null)
    }

    fun getImageWidthAndHeightFromFilePath(filePath: String): String {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val decodeFile = BitmapFactory.decodeFile(filePath, options)
        val size =
            "${if (options.outWidth <= 0) 0 else options.outWidth}x${if (options.outHeight <= 0) 0 else options.outHeight}"
        decodeFile?.recycle()
        return size
    }

    fun getVideoWidthAndHeightFromFilePath(filePath: String): String {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(filePath)
            val videoWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                    ?: 640
            val videoHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                    ?: 640
            val orientation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val min = min(videoWidth, videoHeight)
            val max = max(videoWidth, videoHeight)
            return if (orientation == "90") {
                "${min}x${max}"
            } else {
                "${max}x${min}"
            }

        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            retriever.release()
        }
        return "0x0"

    }

}