package me.ycdev.android.devtools.device

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.databinding.DeviceInfoActivityBinding
import me.ycdev.android.devtools.utils.ViewHelper
import me.ycdev.android.lib.common.internalapi.android.os.EnvironmentIA.getExternalStorageAndroidDataDir
import me.ycdev.android.lib.common.internalapi.android.os.EnvironmentIA.getSecureDataDirectory
import me.ycdev.android.lib.common.internalapi.android.os.EnvironmentIA.getSystemSecureDirectory
import me.ycdev.android.lib.common.internalapi.android.os.EnvironmentIA.isEncryptedFilesystemEnabled
import me.ycdev.android.lib.common.internalapi.android.os.SystemPropertiesIA
import me.ycdev.android.lib.common.utils.ReflectionUtils
import timber.log.Timber

class DeviceInfoActivity : AppCompatBaseActivity() {
    private lateinit var binding: DeviceInfoActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeviceInfoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    @SuppressLint("InlinedApi")
    private fun initViews() {
        val holder = binding.contentHolder
        val screenMetrics = getScreenMetrics(this)
        ViewHelper.addTextView(holder, "Manufacturer", Build.MANUFACTURER)
        ViewHelper.addTextView(holder, "Product", Build.PRODUCT)
        ViewHelper.addTextView(holder, "Device", Build.DEVICE)
        ViewHelper.addTextView(holder, "Id", Build.ID)
        ViewHelper.addTextView(holder, "Model", Build.MODEL)
        ViewHelper.addTextView(holder, "API level", VERSION.SDK_INT.toString())
        ViewHelper.addTextView(holder, "SDK Release", VERSION.RELEASE)
        ViewHelper.addTextView(holder, "CPU ABIs", Build.SUPPORTED_ABIS.joinToString())
        ViewHelper.addTextView(holder, "Memory", memorySize)
        ViewHelper.addTextView(holder, "Fingerprint", Build.FINGERPRINT)
        ViewHelper.addLineView(holder, 0xff0100) // green
        ViewHelper.addTextView(holder, "LCD densityDpi", screenMetrics.densityDpi)
        ViewHelper.addTextView(
            holder, "LCD real X&Y densityDPI",
            screenMetrics.xdpi.toString() + " x " + screenMetrics.ydpi
        )
        ViewHelper.addTextView(holder, "LCD density", screenMetrics.density.toDouble())
        ViewHelper.addTextView(holder, "LCD scaledDensity", screenMetrics.scaledDensity.toDouble())
        ViewHelper.addTextView(holder, "LCD type", screenLayoutSizeType)
        val screenSizePt = getScreenHardwareSize(this)
        ViewHelper.addTextView(
            holder, "LCD size in pixels",
            screenSizePt.x.toString() + " x " + screenSizePt.y
        )
        ViewHelper.addTextView(
            holder, "LCD size in DIP",
            (screenMetrics.widthPixels / screenMetrics.density).toString() + " x " + screenMetrics.heightPixels / screenMetrics.density
        )
        ViewHelper.addTextView(
            holder,
            "VM heap",
            SystemPropertiesIA.get("dalvik.vm.heapsize", "N/A")
        )
        ViewHelper.addTextView(
            holder,
            "ANR trace",
            SystemPropertiesIA.get("dalvik.vm.stack-trace-file", "N/A")
        )
        ViewHelper.addLineView(holder, -0xff0100) // green
        ViewHelper.addTextView(holder, "DNS1", SystemPropertiesIA.get("dhcp.eth0.dns1", "N/A"))
        ViewHelper.addTextView(holder, "DNS2", SystemPropertiesIA.get("dhcp.eth0.dns2", "N/A"))
        ViewHelper.addTextView(holder, "IP", SystemPropertiesIA.get("dhcp.eth0.ipaddress", "N/A"))
        ViewHelper.addTextView(
            holder,
            "Gateway",
            SystemPropertiesIA.get("dhcp.eth0.gateway", "N/A")
        )
        ViewHelper.addTextView(holder, "Mask", SystemPropertiesIA.get("dhcp.eth0.mask", "N/A"))
        ViewHelper.addLineView(holder, -0xff0100) // green
        ViewHelper.addTextView(
            holder, "Encrypted FS: " + isEncryptedFilesystemEnabled()
        )
        ViewHelper.addTextView(
            holder, "Data Dir: " + Environment.getDataDirectory().absolutePath
        )
        ViewHelper.addTextView(
            holder, "Download Cache Dir: " + Environment.getDownloadCacheDirectory()
        )
        ViewHelper.addTextView(
            holder, "Android Secure Data Dir: " + getSecureDataDirectory()
        )
        ViewHelper.addTextView(
            holder, "Android System Secure Dir: " + getSystemSecureDirectory()
        )
        ViewHelper.addTextView(
            holder, "External Storage Dir: " +
                    Environment.getExternalStorageDirectory()
        )
        ViewHelper.addTextView(holder, "app#getExternalFilesDir(): " + getExternalFilesDir(null))
        ViewHelper.addTextView(
            holder, "External Storage state: " +
                    Environment.getExternalStorageState()
        )
        ViewHelper.addTextView(
            holder,
            "External Storage size: $externalStorageSize"
        )
        ViewHelper.addTextView(
            holder, "External storage removable: " +
                    Environment.isExternalStorageRemovable()
        )
        ViewHelper.addTextView(
            holder, "External Storage Data Dir: " +
                    getExternalStorageAndroidDataDir()
        )
        ViewHelper.addTextView(
            holder, "Alarms audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS)
        )
        ViewHelper.addTextView(
            holder, "DCIM: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        )
        ViewHelper.addTextView(
            holder, "Download: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )
        ViewHelper.addTextView(
            holder, "Movies: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        )
        ViewHelper.addTextView(
            holder, "Music: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        )
        ViewHelper.addTextView(
            holder, "Notification audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS)
        )
        ViewHelper.addTextView(
            holder, "Pictures: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        )
        ViewHelper.addTextView(
            holder, "Podcasts: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
        )
        ViewHelper.addTextView(
            holder, "Ringtones: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
        )
    }

    private val screenLayoutSizeType: String
        get() {
            val type =
                resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            when (type) {
                Configuration.SCREENLAYOUT_SIZE_SMALL -> return "small-320x426dp"
                Configuration.SCREENLAYOUT_SIZE_NORMAL -> return "normal-320x470dp"
                Configuration.SCREENLAYOUT_SIZE_LARGE -> return "large-480x640"
                Configuration.SCREENLAYOUT_SIZE_XLARGE -> return "xlarge-720x960"
                Configuration.SCREENLAYOUT_SIZE_UNDEFINED -> return "undefined"
            }
            return "unknown: $type"
        }

    // /proc/meminfo读出的内核信息进行解释
    private val totalMemory: Long
        get() { // /proc/meminfo读出的内核信息进行解释
            val path = "/proc/meminfo"
            var content: String? = null
            var br: BufferedReader? = null
            try {
                br = BufferedReader(FileReader(path), 8)
                content = br.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (br != null) {
                    try {
                        br.close()
                    } catch (ignored: IOException) {
                    }
                }
            }
            if (content == null) {
                return -1
            }
            val begin = content.indexOf(':')
            val end = content.indexOf('k') // 单位是KB
            content = content.substring(begin + 1, end).trim { it <= ' ' }
            return (content.toInt() / 1024).toLong() // 返回单位是MB
        }

    private val memorySize: String
        get() {
            val am =
                this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = MemoryInfo()
            am.getMemoryInfo(mi)
            var total: Long = 0
            total = mi.totalMem / (1024 * 1024)
            val avail = mi.availMem / (1024 * 1024)
            return "$avail/$total MB"
        }

    companion object {
        private const val TAG = "DeviceInfoActivity"

        fun getScreenHardwareSize(cxt: Context): Point {
            val wm = cxt.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screen = wm.defaultDisplay
            val pt = Point()
            try {
                val getRealSizeMethod = ReflectionUtils.findMethod(
                    Display::class.java,
                    "getRealSize",
                    Point::class.java
                )
                getRealSizeMethod.invoke(screen, pt)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Unexpected exception: ")
                screen.getSize(pt) // exclude window decor size (eg, statusbar)
            }
            return pt
        }

        // in MB
        private val externalStorageSize: String
            get() {
                val stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
                val a: Long
                val t: Long
                a = stat.blockSizeLong * stat.availableBlocksLong / (1024 * 1024)
                t = stat.blockSizeLong * stat.blockCountLong / (1024 * 1024)
                return "$a/$t MB"
            }

        private fun getScreenMetrics(context: Context): DisplayMetrics {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            return dm
        }
    }
}
