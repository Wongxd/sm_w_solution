
package io.wongxd.solution.fragmentation_kt.record

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

class ResultRecord : Parcelable {
    @JvmField
    var requestCode = 0

    @JvmField
    var resultCode = 0

    @JvmField
    var resultBundle: Bundle? = null

    constructor()

    constructor(parcel: Parcel) : this() {
        requestCode = parcel.readInt()
        resultCode = parcel.readInt()
        resultBundle = parcel.readBundle(javaClass.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(requestCode)
        dest.writeInt(resultCode)
        dest.writeBundle(resultBundle)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ResultRecord?> =
            object : Parcelable.Creator<ResultRecord?> {
                override fun createFromParcel(parcel: Parcel): ResultRecord {
                    return ResultRecord(parcel)
                }

                override fun newArray(size: Int): Array<ResultRecord?> {
                    return arrayOfNulls(size)
                }
            }
    }
}