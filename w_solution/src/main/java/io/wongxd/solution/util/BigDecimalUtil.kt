package io.wongxd.solution.util

import java.math.BigDecimal

/**
 * 加、减、乘、除 高精度计算工具类
 *
 */
object BigDecimalUtil {

    // 需要精确至小数点后几位
    const val DECIMAL_POINT_NUMBER: Int = 30

    const val ROUND_MODE = BigDecimal.ROUND_HALF_EVEN

    /**
     * 加法运算
     */
    @JvmStatic
    fun add(d1: Double, d2: Double): Double =
        BigDecimal(d1.toString())
            .add(BigDecimal(d2.toString()))
            .setScale(DECIMAL_POINT_NUMBER, ROUND_MODE)
            .toDouble()

    /**
     * 减法运算
     */
    @JvmStatic
    fun sub(d1: Double, d2: Double): Double =
        BigDecimal(d1.toString())
            .subtract(BigDecimal(d2.toString()))
            .setScale(DECIMAL_POINT_NUMBER, ROUND_MODE)
            .toDouble()

    /**
     * 乘法运算
     */
    @JvmStatic
    fun mul(d1: Double, d2: Double, decimalPoint: Int): Double =
        BigDecimal(d1.toString())
            .multiply(BigDecimal(d2.toString()))
            .setScale(decimalPoint, ROUND_MODE)
            .toDouble()

    /**
     * 除法运算
     */
    @JvmStatic
    fun div(d1: Double, d2: Double): Double =
        BigDecimal(d1.toString())
            .divide(BigDecimal(d2.toString()), DECIMAL_POINT_NUMBER, ROUND_MODE)
            .toDouble()

}
 