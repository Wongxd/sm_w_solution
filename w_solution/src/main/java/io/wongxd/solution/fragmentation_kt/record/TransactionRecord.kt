
package io.wongxd.solution.fragmentation_kt.record

class TransactionRecord {
    @JvmField
    var tag: String? = null

    @JvmField
    var targetFragmentEnter = Int.MIN_VALUE

    @JvmField
    var currentFragmentPopExit = Int.MIN_VALUE

    @JvmField
    var currentFragmentPopEnter = Int.MIN_VALUE

    @JvmField
    var targetFragmentExit = Int.MIN_VALUE
}