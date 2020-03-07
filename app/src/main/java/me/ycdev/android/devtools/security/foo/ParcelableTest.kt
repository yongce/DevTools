package me.ycdev.android.devtools.security.foo

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class ParcelableTest : Parcelable {
    private var oomValue: Int

    constructor(oomValue: Int) {
        this.oomValue = oomValue
    }

    private constructor(`in`: Parcel) {
        oomValue = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(oomValue)
    }

    companion object {
        @JvmField val CREATOR: Creator<ParcelableTest> =
            object : Creator<ParcelableTest> {
                override fun createFromParcel(`in`: Parcel): ParcelableTest? {
                    return ParcelableTest(`in`)
                }

                override fun newArray(size: Int): Array<ParcelableTest?> {
                    return arrayOfNulls(size)
                }
            }
    }
}