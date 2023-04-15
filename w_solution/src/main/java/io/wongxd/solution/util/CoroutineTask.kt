package io.wongxd.solution.util

import io.wongxd.solution.logger.WLogUtil
import kotlinx.coroutines.*

open class CoroutineTask : CoroutineScope by MainScope() {

    fun runMainThread(block: suspend CoroutineScope.() -> Unit): Job {
        return launchOnUi { tryCatch(block, {}, {}, true) }
    }

    fun runIoThread(block: suspend CoroutineScope.() -> Unit): Job {
        return runIoThreadTryCatch(block, {})
    }

    private fun launchOnUi(block: suspend CoroutineScope.() -> Unit): Job {
        return launch { block() }
    }

    private suspend fun tryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ) {
        coroutineScope {
            try {
                tryBlock()
            } catch (e: Throwable) {
                if (e !is CancellationException || handleCancellationExceptionManually) {
                    WLogUtil.errorTrace(e)
                    catchBlock(e)
                }
            } finally {
                finallyBlock()
            }
        }
    }

    fun runMainThreadTryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ): Job {
        return launchOnUi { tryCatch(tryBlock, {}, {}, handleCancellationExceptionManually) }
    }

    fun runMainThreadTryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ): Job {
        return launchOnUi {
            tryCatch(
                tryBlock,
                catchBlock,
                {},
                handleCancellationExceptionManually
            )
        }
    }

    fun runMainThreadTryCatchFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ): Job {
        return launchOnUi {
            tryCatch(
                tryBlock,
                catchBlock,
                finallyBlock,
                handleCancellationExceptionManually
            )
        }
    }

    fun runIoThreadTryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ): Job {
        return launch(Dispatchers.IO) {
            tryCatch(
                tryBlock,
                catchBlock,
                {},
                handleCancellationExceptionManually
            )
        }
    }

    fun runIoThreadTryCatchFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ): Job {
        return launch(Dispatchers.IO) {
            tryCatch(
                tryBlock,
                catchBlock,
                finallyBlock,
                handleCancellationExceptionManually
            )
        }
    }

    fun runIoThreadTryCatchPrintTrack(tryBlock: suspend CoroutineScope.() -> Unit): Job {
        return runIoThreadTryCatch(tryBlock, { error -> WLogUtil.errorTrace(error) })
    }

    fun runIoThreadTryCatchCompletable(
        deferred: CompletableDeferred<*>,
        tryBlock: suspend CoroutineScope.() -> Unit
    ): Job {
        return runIoThreadTryCatch(
            tryBlock,
            { error -> WLogUtil.errorTrace(error);deferred.completeExceptionally(error) })
    }

    suspend fun switchMain(block: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.Main) { block() }
    }

    suspend fun switchIo(block: suspend CoroutineScope.() -> Unit) {
        withContext(Dispatchers.IO) { block }
    }

}

object CoroutineTaskHelper : CoroutineTask() {}