
package io.wongxd.solution.compose.composeTheme


enum class AppTheme(val type: Int) {

    Light(0), Dark(1);

    fun isDarkTheme(): Boolean {
        return this == Dark
    }

    fun nextTheme(): AppTheme {
        return when (this) {
            Light -> {
                Dark
            }
            Dark -> {
                Light
            }
        }
    }

}