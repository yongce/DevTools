package me.ycdev.android.devtools.contacts

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContactsActivityTest {
    @Test
    fun contactNumberPoolSize_keepsAtLeastOneNumber() {
        assertThat(ContactsActivity.contactNumberPoolSize(1)).isEqualTo(1)
    }

    @Test
    fun contactNumberPoolSize_scalesWithContactCount() {
        assertThat(ContactsActivity.contactNumberPoolSize(10)).isEqualTo(5)
    }
}
