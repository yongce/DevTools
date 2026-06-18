package me.ycdev.android.devtools.widget

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class AdapterNotifyUtilsTest {
    @Test
    fun notifyItemsReplaced_reportsChangedAndInsertedRanges() {
        val adapter = RecordingAdapter()

        adapter.notifyItemsReplaced(oldCount = 3, newCount = 5)

        assertThat(adapter.events)
            .containsExactly("changed:0:3", "inserted:3:2")
            .inOrder()
    }

    @Test
    fun notifyItemsReplaced_reportsChangedAndRemovedRanges() {
        val adapter = RecordingAdapter()

        adapter.notifyItemsReplaced(oldCount = 5, newCount = 2)

        assertThat(adapter.events)
            .containsExactly("changed:0:2", "removed:2:3")
            .inOrder()
    }

    private class RecordingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val events = ArrayList<String>()

        init {
            registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeChanged(
                        positionStart: Int,
                        itemCount: Int,
                    ) {
                        events.add("changed:$positionStart:$itemCount")
                    }

                    override fun onItemRangeInserted(
                        positionStart: Int,
                        itemCount: Int,
                    ) {
                        events.add("inserted:$positionStart:$itemCount")
                    }

                    override fun onItemRangeRemoved(
                        positionStart: Int,
                        itemCount: Int,
                    ) {
                        events.add("removed:$positionStart:$itemCount")
                    }
                },
            )
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerView.ViewHolder = object : RecyclerView.ViewHolder(View(parent.context)) {}

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
        ) {
        }

        override fun getItemCount(): Int = 0
    }
}
