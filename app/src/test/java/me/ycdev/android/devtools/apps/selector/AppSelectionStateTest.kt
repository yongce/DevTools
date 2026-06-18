package me.ycdev.android.devtools.apps.selector

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppSelectionStateTest {
    @Test
    fun toggle_replacesPreviousSelectionInSingleChoiceMode() {
        val selectionState = AppSelectionState(multiChoice = false)

        assertThat(selectionState.toggle("pkg.one")).containsExactly("pkg.one")
        assertThat(selectionState.toggle("pkg.two")).containsExactly("pkg.one", "pkg.two").inOrder()

        assertThat(selectionState.isSelected("pkg.one")).isFalse()
        assertThat(selectionState.isSelected("pkg.two")).isTrue()
        assertThat(selectionState.selectedPackageNames).containsExactly("pkg.two")
        assertThat(selectionState.oneSelectedPackageName).isEqualTo("pkg.two")
    }

    @Test
    fun toggle_keepsMultipleSelectionsInMultiChoiceMode() {
        val selectionState = AppSelectionState(multiChoice = true)

        selectionState.toggle("pkg.one")
        selectionState.toggle("pkg.two")
        assertThat(selectionState.selectedPackageNames).containsExactly("pkg.one", "pkg.two").inOrder()

        assertThat(selectionState.toggle("pkg.one")).containsExactly("pkg.one")
        assertThat(selectionState.selectedPackageNames).containsExactly("pkg.two")
    }
}
