package me.ycdev.android.devtools.apps.selector

internal class AppSelectionState(
    private val multiChoice: Boolean,
) {
    private val _selectedPackageNames = LinkedHashSet<String>()

    val selectedCount: Int
        get() = _selectedPackageNames.size

    val selectedPackageNames: List<String>
        get() = ArrayList(_selectedPackageNames)

    val oneSelectedPackageName: String?
        get() = _selectedPackageNames.firstOrNull()

    fun isSelected(pkgName: String): Boolean = _selectedPackageNames.contains(pkgName)

    fun toggle(pkgName: String): List<String> {
        val changedPackageNames = ArrayList<String>()
        if (_selectedPackageNames.remove(pkgName)) {
            changedPackageNames.add(pkgName)
            return changedPackageNames
        }
        if (!multiChoice) {
            oneSelectedPackageName?.let {
                changedPackageNames.add(it)
            }
            _selectedPackageNames.clear()
        }
        _selectedPackageNames.add(pkgName)
        changedPackageNames.add(pkgName)
        return changedPackageNames.distinct()
    }
}
