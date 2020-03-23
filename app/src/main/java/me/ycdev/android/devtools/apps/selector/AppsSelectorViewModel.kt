package me.ycdev.android.devtools.apps.selector

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.common.apps.AppsLoadConfig
import me.ycdev.android.lib.common.apps.AppsLoadFilter
import me.ycdev.android.lib.common.apps.AppsLoadListener
import me.ycdev.android.lib.common.apps.AppsLoader
import timber.log.Timber

class AppsSelectorViewModel(
    app: Application,
    private val excludeUninstalled: Boolean,
    private val excludeDisabled: Boolean,
    private val excludeSystem: Boolean
) : AndroidViewModel(app) {
    private var _apps: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val apps: LiveData<List<AppInfo>> = _apps

    init {
        viewModelScope.launch {
            _apps.setValue(loadApps())
        }
    }

    private suspend fun loadApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        Timber.tag(TAG).d("loading apps...")
        val timeStart = SystemClock.elapsedRealtime()

        val filter = AppsLoadFilter()
        filter.onlyMounted = excludeUninstalled
        filter.onlyEnabled = excludeDisabled
        filter.includeSysApp = !excludeSystem
        val config = AppsLoadConfig()
        val listener: AppsLoadListener = object : AppsLoadListener {
            override fun isCancelled(): Boolean {
                return !isActive
            }

            override fun onProgressUpdated(percent: Int, appInfo: AppInfo) {
                Timber.tag(TAG).d("onProgressUpdated: $percent")
            }
        }

        val timeUsed = SystemClock.elapsedRealtime() - timeStart
        if (timeUsed < 500) {
            delay(500 - timeUsed)
        }
        return@withContext AppsLoader.getInstance(getApplication())
            .loadInstalledApps(filter, config, listener)
    }

    override fun onCleared() {
        Timber.tag(TAG).d("onCleared")
    }

    class Factory(
        private val app: Application,
        private val excludeUninstalled: Boolean,
        private val excludeDisabled: Boolean,
        private val excludeSystem: Boolean
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == AppsSelectorViewModel::class.java) {
                return AppsSelectorViewModel(
                    app,
                    excludeUninstalled,
                    excludeDisabled,
                    excludeSystem
                ) as T
            } else {
                return AndroidViewModelFactory.getInstance(app).create(modelClass)
            }
        }
    }

    companion object {
        private const val TAG = "AppsSelectorViewModel"
    }
}
