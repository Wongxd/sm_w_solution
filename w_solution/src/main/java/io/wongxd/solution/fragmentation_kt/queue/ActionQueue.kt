
package io.wongxd.solution.fragmentation_kt.queue

import android.os.Handler
import android.os.Looper
import io.wongxd.solution.fragmentation_kt.queue.Action
import java.util.*

class ActionQueue(private val mMainHandler: Handler) {
    private val mQueue: Queue<Action> = LinkedList()

    fun enqueue(action: Action) {
        if (isThrottleBACK(action)) return
        if (action.action == Action.ACTION_LOAD && mQueue.isEmpty()
            && Thread.currentThread() === Looper.getMainLooper().thread
        ) {
            action.run()
            return
        }
        mMainHandler.post { enqueueAction(action) }
    }

    private fun enqueueAction(action: Action) {
        mQueue.add(action)
        if (mQueue.size == 1) {
            handleAction()
        }
    }

    private fun handleAction() {
        if (mQueue.isEmpty()) return
        val action = mQueue.peek()!!
        action.run()
        executeNextAction(action)
    }

    private fun executeNextAction(action: Action?) {
        if (action!!.action == Action.ACTION_POP) {
            action.duration = Action.DEFAULT_POP_TIME
        }
        mMainHandler.postDelayed({
            mQueue.poll()
            handleAction()
        }, action.duration)
    }

    private fun isThrottleBACK(action: Action): Boolean {
        if (action.action == Action.ACTION_BACK) {
            val head = mQueue.peek()
            return head != null && head.action == Action.ACTION_POP
        }
        return false
    }
}