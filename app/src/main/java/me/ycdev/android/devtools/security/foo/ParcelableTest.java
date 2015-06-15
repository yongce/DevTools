package me.ycdev.android.devtools.security.foo;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableTest implements Parcelable {
    private int mOomValue;

    public ParcelableTest(int oomValue) {
        mOomValue = oomValue;
    }

    private ParcelableTest(Parcel in) {
        mOomValue = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOomValue);
    }

    public static final Parcelable.Creator<ParcelableTest> CREATOR = new
            Parcelable.Creator<ParcelableTest>() {

                public ParcelableTest createFromParcel(Parcel in) {
                    return new ParcelableTest(in);
                }

                public ParcelableTest[] newArray(int size) {
                    return new ParcelableTest[size];
                }

            };
}
