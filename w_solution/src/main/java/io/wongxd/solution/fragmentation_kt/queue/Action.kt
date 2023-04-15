package io.wongxd.solution.fragmentation_kt.queue

import androidx.fragment.app.FragmentManager

abstract class Action {
    var fragmentManager: FragmentManager? = null

    @JvmField
    var action = ACTION_NORMAL

    @JvmField
    var duration: Long = 0

    constructor()

    constructor(action: Int) : this() {
        this.action = action
    }

    constructor(action: Int, fragmentManager: FragmentManager?) : this(action) {
        this.fragmentManager = fragmentManager
    }

    abstract fun run()

    companion object {
        const val DEFAULT_POP_TIME = 320L
        const val ACTION_NORMAL = 0
        const val ACTION_POP = 1
        const val ACTION_BACK = 2
        const val ACTION_LOAD = 3
    }
}