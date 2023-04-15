//package w.solution.event
//
//import android.app.Application
//import android.util.Log
//import androidx.lifecycle.*
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.flow.collect
//
//class FlowEventBus : ViewModel() {
//    companion object {
//        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { FlowEventBus() }
//    }
//
//    //正常事件
//    private val events = mutableMapOf<String, Event<*>>()
//
//    //粘性事件
//    private val stickyEvents = mutableMapOf<String, Event<*>>()
//
//    fun with(key: String, isSticky: Boolean = false): Event<Any> {
//        return with(key, Any::class.java, isSticky)
//    }
//
//    fun <T> with(eventType: Class<T>, isSticky: Boolean = false): Event<T> {
//        return with(eventType.name, eventType, isSticky)
//    }
//
//    @Synchronized
//    fun <T> with(key: String, type: Class<T>?, isSticky: Boolean): Event<T> {
//        val flows = if (isSticky) stickyEvents else events
//        if (!flows.containsKey(key)) {
//            flows[key] = Event<T>(key, isSticky)
//        }
//        return flows[key] as Event<T>
//    }
//
//
//    class Event<T>(private val key: String, isSticky: Boolean) {
//
//        // private mutable shared flow
//        private val _events = MutableSharedFlow<T>(
//            replay = if (isSticky) 1 else 0,
//            extraBufferCapacity = Int.MAX_VALUE
//        )
//
//        // publicly exposed as read-only shared flow
//        val events = _events.asSharedFlow()
//
//        /**
//         * need main thread execute
//         */
//        fun observeEvent(
//            lifecycleOwner: LifecycleOwner,
//            dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
//            minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
//            action: (t: T) -> Unit
//        ) {
//            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
//                override fun onDestroy(owner: LifecycleOwner) {
//                    super.onDestroy(owner)
//                    Log.d("FlowEventBus", "EventBus.onDestroy:remove key=$key")
//                    val subscriptCount = _events.subscriptionCount.value
//                    if (subscriptCount <= 0)
//                        instance.events.remove(key)
//                }
//            })
//            lifecycleOwner.lifecycleScope.launch(dispatcher) {
//                lifecycleOwner.lifecycle.whenStateAtLeast(minActiveState) {
//                    events.collect {
//                        try {
//                            action(it)
//                        } catch (e: Exception) {
//                            Log.d("FlowEventBus", "ker=$key , error=${e.message}")
//                        }
//                    }
//                }
//            }
//        }
//
//        /**
//         * send value
//         */
//        suspend fun setValue(
//            event: T,
//            dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
//        ) {
//            withContext(dispatcher) {
//                _events.emit(event)
//            }
//
//        }
//    }
//}
//
////
////lifecycleScope.launch(Dispatchers.IO) {
////    observeEvent {
////        LjyLogUtil.d("FlowBus.register1:${GsonUtils.toJson(it)}_${Thread.currentThread().name}")
////    }
////    observeEvent(Dispatchers.IO) {
////        LjyLogUtil.d("FlowBus.register2:${GsonUtils.toJson(it)}_${Thread.currentThread().name}")
////    }
////
////    observeEvent(Dispatchers.Main) {
////        LjyLogUtil.d("FlowBus.register3:${GsonUtils.toJson(it)}_${Thread.currentThread().name}")
////    }
////}
////
////lifecycleScope.launch(Dispatchers.IO) {
////    delay(1000)
////    postValue(EventMessage(100))
////    postValue(EventMessage(101), 1000)
////    postValue(EventMessage(102, "bbb"), dispatcher = Dispatchers.IO)
////    val event3 = EventMessage(103, "ccc")
////    event3.put("key1", 123)
////    event3.put("key2", "abc")
////    postValue(event3, 2000, dispatcher = Dispatchers.Main)
////}
////
//open class FlowEventMessage
//
////利用扩展函数
//fun LifecycleOwner.observeFlowEvent(
//    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
//    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
//    isSticky: Boolean = false,
//    action: (t: FlowEventMessage) -> Unit
//) {
//    ApplicationScopeViewModelProvider
//        .getApplicationScopeViewModel(FlowEventBus::class.java)
//        .with(FlowEventMessage::class.java, isSticky = isSticky)
//        .observeEvent(this, dispatcher, minActiveState, action)
//}
//
//fun postFlowEvent(
//    event: FlowEventMessage,
//    delayTimeMillis: Long = 0,
//    isSticky: Boolean = false,
//    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
//) {
//
//    ApplicationScopeViewModelProvider
//        .getApplicationScopeViewModel(FlowEventBus::class.java)
//        .viewModelScope
//        .launch(dispatcher) {
//            delay(delayTimeMillis)
//            ApplicationScopeViewModelProvider
//                .getApplicationScopeViewModel(FlowEventBus::class.java)
//                .with(FlowEventMessage::class.java, isSticky = isSticky)
//                .setValue(event)
//        }
//}
//
//private object ApplicationScopeViewModelProvider : ViewModelStoreOwner {
//
//    private val eventViewModelStore: ViewModelStore = ViewModelStore()
//
//    override fun getViewModelStore(): ViewModelStore {
//        return eventViewModelStore
//    }
//
//    private val mApplicationProvider: ViewModelProvider by lazy {
//        ViewModelProvider(
//            ApplicationScopeViewModelProvider,
//            ViewModelProvider.AndroidViewModelFactory.getInstance(FlowEventBusInitializer.application)
//        )
//    }
//
//    fun <T : ViewModel> getApplicationScopeViewModel(modelClass: Class<T>): T {
//        return mApplicationProvider[modelClass]
//    }
//
//}
//
//object FlowEventBusInitializer {
//    lateinit var application: Application
//
//    //在Application中初始化
//    fun init(application: Application) {
//        FlowEventBusInitializer.application = application
//    }
//}