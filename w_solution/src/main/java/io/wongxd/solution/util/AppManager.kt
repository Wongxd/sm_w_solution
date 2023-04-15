package io.wongxd.solution.util

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import java.lang.ref.WeakReference
import java.util.*

class AppManager {
    companion object {
        val activityStack: Stack<WeakReference<Activity>> = Stack()

        fun finishActivity(clazz: Class<out Any>) {
            val listIterator = activityStack.iterator()
            try {
                while (listIterator.hasNext()) {
                    val activity = listIterator.next().get()
                    if (activity == null) {
                        listIterator.remove()
                        continue
                    }
                    if (activity.javaClass == clazz) {
                        listIterator.remove()
                        activity.finish()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun finishAllActivity() {
            val listIterator = activityStack.iterator()
            try {
                while (listIterator.hasNext()) {
                    val activity = listIterator.next().get()
                    if (activity == null) {
                        listIterator.remove()
                        continue
                    }
                    listIterator.remove()
                    activity.finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 应用是否处于前台
         */
        fun isAppOnForeground(context: Context): Boolean {
            val activityManager =
                context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val packageName = context.applicationContext.packageName
            val appProcessList = activityManager.runningAppProcesses
            var result = false
            appProcessList.forEach {
                if (it.processName == packageName && it.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    result = true
                    return@forEach
                }
            }
            return result
        }

        fun getCurrentActivityName(): String? {
            return activityStack.peek().get()?.javaClass?.name
        }

        fun getCurrentActivity(): Activity? {
            return activityStack.peek().get()
        }

        /**
         * 添加Activity到堆栈
         *
         * @param activity activity实例
         */
        fun addActivity(activity: Activity) {
            activityStack.push(WeakReference(activity))
        }

        fun removeActivity(aty: Activity?) {
            if (aty != null) {
                val listIterator = activityStack.iterator()
                try {
                    while (listIterator.hasNext()) {
                        val getAty = listIterator.next().get()
                        if (aty.javaClass == getAty?.javaClass) {
                            listIterator.remove()
                            continue
                        }
                        listIterator.remove()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 退出应用程序
         */
        fun exitApp() {
            try {
                finishAllActivity()
                // 退出JVM(java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
                System.exit(0)
                // 从操作系统中结束掉当前程序的进程
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (e: java.lang.Exception) {
            }
        }


    }

}