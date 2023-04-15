package io.wongxd.solution.util

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object FlowBus {
    /**
     * private mutable shared flow
     */
    private val mutableSharedFlow = MutableSharedFlow<FlowEvent>()

    /**
     * publicly exposed as read-only shared flow
     */
    private val asSharedFlow = mutableSharedFlow.asSharedFlow()

    val eventBus: SharedFlow<FlowEvent>
        get() = asSharedFlow

    init {
        GlobalScope.launch {
            //日志打印当前订阅的订阅者数量
            mutableSharedFlow.subscriptionCount.collect {
                Log.d("flow", "subscriptionCount $it")
            }
        }
    }

    /**
     * 发布事件
     * Launches a new coroutine without blocking the current thread and returns a reference to the coroutine as a [Job].
     * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
     */
    fun <T : FlowEvent> LifecycleOwner.produceEvent(event: T): Job {
        // suspends until all subscribers receive it
        return lifecycleScope.produceEvent(event)
    }

    fun <T : FlowEvent> CoroutineScope.produceEvent(event: T): Job {
        return this.launch {
            mutableSharedFlow.emit(event)
        }
    }

    /**
     * 在GlobalScope中发布
     */
    fun <T : FlowEvent> produceEventGlobal(event: T) {
        // suspends until all subscribers receive it
        GlobalScope.launch {
            mutableSharedFlow.emit(event)
        }
    }

    /**
     * Launches and runs the given block when the [Lifecycle] controlling this
     * [LifecycleCoroutineScope] is at least in [Lifecycle.State.CREATED] state.
     *
     * The returned [Job] will be cancelled when the [Lifecycle] is destroyed.
     */
    fun <T : FlowEvent> LifecycleOwner.produceEventWhenCreated(event: T): Job {
        // suspends until all subscribers receive it
        return lifecycleScope.launchWhenCreated {
            mutableSharedFlow.emit(event)
        }
    }

    /**
     * Launches and runs the given block when the [Lifecycle] controlling this
     * [LifecycleCoroutineScope] is at least in [Lifecycle.State.STARTED] state.
     *
     * The returned [Job] will be cancelled when the [Lifecycle] is destroyed.
     */
    fun <T : FlowEvent> LifecycleOwner.produceEventWhenStared(event: T): Job {
        // suspends until all subscribers receive it
        return lifecycleScope.launchWhenStarted {
            mutableSharedFlow.emit(event)
        }
    }

    /**
     * Launches and runs the given block when the [Lifecycle] controlling this
     * [LifecycleCoroutineScope] is at least in [Lifecycle.State.RESUMED] state.
     *
     * The returned [Job] will be cancelled when the [Lifecycle] is destroyed.
     */
    fun <T : FlowEvent> LifecycleOwner.produceEventWhenResumed(event: T): Job {
        // suspends until all subscribers receive it
        return lifecycleScope.launchWhenResumed {
            mutableSharedFlow.emit(event)
        }
    }

    /**
     * subscribe event
     * The returned [Job] can be cancelled
     */
    inline fun <reified T : FlowEvent> CoroutineScope.subscribeLastEvent(
        debounce: Long = 500L,
        crossinline predicate: suspend (e: FlowEvent) -> Boolean = { e -> e is T },
        crossinline action: suspend (e: T) -> Unit,
    ): Job {
        return launch {
            eventBus
                .filter { predicate.invoke(it) }
                .debounce(debounce)
                .collectLatest {
                    if (it is T) action.invoke(it)
                }
        }
    }

    /**
     * subscribe event
     * The returned [Job] can be cancelled
     */
    inline fun <reified T : FlowEvent> LifecycleOwner.subscribeEvent(
        debounce: Long = 500L,
        crossinline predicate: suspend (e: FlowEvent) -> Boolean = { e -> e is T },
        crossinline action: suspend (e: T) -> Unit,
    ): Job {
        return eventBus
            .filter { predicate.invoke(it) }
            .debounce(debounce)
            .onEach {
                if (it is T) action.invoke(it)
            }.cancellable()
            .launchIn(lifecycleScope)
    }
}

open class FlowEvent(open val key: String = "FlowEvent")