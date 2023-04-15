package io.wongxd.solution.util


import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager


class SimpleOnActivityResult private constructor() {


    companion object {

        private const val TAG = "SimpleOnActivityResult"
        private var mSimpleOnResultFragment: SimpleOnActivityResultAsset.SimpleOnResultFragment? =
            null

        private fun findSimpleOnResultFragment(fragmentManager: FragmentManager): SimpleOnActivityResultAsset.SimpleOnResultFragment? {
            return fragmentManager.findFragmentByTag(TAG) as SimpleOnActivityResultAsset.SimpleOnResultFragment?
        }

        private fun getOnResultFragment(fragmentManager: FragmentManager): SimpleOnActivityResultAsset.SimpleOnResultFragment {
            var simpleOnResultFragment: SimpleOnActivityResultAsset.SimpleOnResultFragment? =
                findSimpleOnResultFragment(fragmentManager)
            if (simpleOnResultFragment == null) {
                simpleOnResultFragment = SimpleOnActivityResultAsset.SimpleOnResultFragment()
                fragmentManager
                    .beginTransaction()
                    .add(simpleOnResultFragment, TAG)
                    .commitAllowingStateLoss()
                fragmentManager.executePendingTransactions()
            }
            return simpleOnResultFragment
        }

        fun simpleForResult(activity: FragmentActivity): SimpleOnActivityResult {
            mSimpleOnResultFragment = getOnResultFragment(activity.supportFragmentManager)
            return SimpleOnActivityResult()
        }

        fun simpleForResult(fragment: Fragment): SimpleOnActivityResult {
            mSimpleOnResultFragment = getOnResultFragment(fragment.childFragmentManager)
            return SimpleOnActivityResult()
        }
    }


    fun startForResult(intent: Intent, reqCode: Int = 0, callback: SimpleOnActivityResultCallback) {
        mSimpleOnResultFragment?.startForResult(intent, reqCode, callback)
    }

    fun startForResult(
        clazz: Class<*>,
        reqCode: Int = 0,
        callback: SimpleOnActivityResultCallback
    ) {
        val intent = Intent(mSimpleOnResultFragment?.activity, clazz)
        startForResult(intent, reqCode, callback)
    }


}

class SimpleOnActivityResultAsset {

    class SimpleOnResultFragment : Fragment() {

        private var requestCode: Int = 0

        private lateinit var callback: SimpleOnActivityResultCallback

        private val requestResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            onResult(activityResult)
        }

        private fun onResult(activityResult: ActivityResult) {
            //callback方式的处理
            callback.invoke(requestCode, activityResult.resultCode, activityResult.data)
        }


        fun startForResult(intent: Intent, reqCode: Int, callback: SimpleOnActivityResultCallback) {
            this.callback = callback
            this.requestCode = reqCode
            requestResultLauncher.launch(intent)
        }

    }

}

/**
 *
 * requestCode: Int, resultCode: Int, data: Intent?
 *
 */
private typealias  SimpleOnActivityResultCallback = (Int, Int, Intent?) -> Unit