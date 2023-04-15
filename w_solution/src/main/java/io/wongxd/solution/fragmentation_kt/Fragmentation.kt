
package io.wongxd.solution.fragmentation_kt

import androidx.annotation.IntDef
import io.wongxd.solution.R


class Fragmentation private constructor(builder: FragmentationBuilder) {
    var isDebug: Boolean = builder.debug

    var mode: Int = if (isDebug) {
        builder.mode
    } else {
        NONE
    }

    var targetFragmentEnter: Int = builder.targetFragmentEnter

    var targetFragmentExit: Int = builder.targetFragmentExit

    var currentFragmentPopEnter: Int = builder.currentFragmentPopEnter

    var currentFragmentPopExit: Int = builder.currentFragmentPopExit


    @IntDef(NONE, SHAKE, BUBBLE)
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class StackViewMode

    class FragmentationBuilder {
        var debug = false
            private set
        var mode = NONE
            private set
        var targetFragmentEnter = R.anim.w_solution_v_fragment_enter
            private set
        var targetFragmentExit = R.anim.w_solution_v_fragment_exit
            private set
        var currentFragmentPopEnter = R.anim.w_solution_v_fragment_pop_enter
            private set
        var currentFragmentPopExit = R.anim.w_solution_v_fragment_pop_exit
            private set


        /**
         * 实际场景建议.debug(BuildConfig.DEBUG)
         * @param debug Suppressed Exception("Can not perform this action after onSaveInstanceState!") when debug=false
         */
        fun debug(debug: Boolean): FragmentationBuilder {
            this.debug = debug
            return this
        }

        fun animation(
            targetFragmentEnter: Int,
            targetFragmentExit: Int,
            currentFragmentPopExit: Int,
            currentFragmentPopEnter: Int
        ): FragmentationBuilder {
            this.targetFragmentEnter = targetFragmentEnter
            this.currentFragmentPopExit = currentFragmentPopExit
            this.currentFragmentPopEnter = currentFragmentPopEnter
            this.targetFragmentExit = targetFragmentExit
            return this
        }

        /**
         *
         * 设置 栈视图 模式为 （默认）悬浮球模式   SHAKE: 摇一摇唤出  NONE：隐藏， 仅在Debug环境生效
         *
         * Sets the mode to display the stack view
         *
         *
         * None if debug(false).
         *
         *
         * Default:NONE
         */
        fun stackViewMode(@StackViewMode mode: Int): FragmentationBuilder {
            this.mode = mode
            return this
        }

        fun install(): Fragmentation? {
            INSTANCE = Fragmentation(this)
            return INSTANCE
        }
    }

    companion object {
        /**
         * Dont display stack view.
         */
        const val NONE = 0

        /**
         * Shake it to display stack view.
         */
        const val SHAKE = 1

        /**
         * As a bubble display stack view.
         */
        const val BUBBLE = 2

        @Volatile
        private var INSTANCE: Fragmentation? = null

        @JvmStatic
        val default: Fragmentation?
            get() {
                if (INSTANCE == null) {
                    synchronized(Fragmentation::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = Fragmentation(FragmentationBuilder())
                        }
                    }
                }
                return INSTANCE
            }

        fun builder(): FragmentationBuilder {
            return FragmentationBuilder()
        }
    }
}