package io.wongxd.solution.util.ext

import io.wongxd.solution.util.BigDecimalUtil

fun Double.addHigh(d: Number) = BigDecimalUtil.add(this, d.toDouble())

fun Double.subHigh(d: Number) = BigDecimalUtil.sub(this, d.toDouble())

fun Double.mulHigh(d: Number, decimalPoint: Int = BigDecimalUtil.DECIMAL_POINT_NUMBER) = BigDecimalUtil.mul(this, d.toDouble(), decimalPoint)

fun Double.divHigh(d: Number) = BigDecimalUtil.div(this, d.toDouble())