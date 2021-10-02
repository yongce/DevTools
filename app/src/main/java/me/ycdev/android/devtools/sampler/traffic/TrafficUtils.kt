package me.ycdev.android.devtools.sampler.traffic

import me.ycdev.android.lib.common.utils.IoUtils.readAllLines
import me.ycdev.android.lib.common.utils.StringUtils.parseLong
import java.io.IOException
import java.util.Locale

object TrafficUtils {
    private const val DEV_UID_RCV_FILE = "/proc/uid_stat/%d/tcp_rcv"
    private const val DEV_UID_SND_FILE = "/proc/uid_stat/%d/tcp_snd"

    /**
     * Get traffic stats of specified UIDs.
     * @return null may be returned if failed to read the stat
     */
    fun getTrafficStat(uid: Int): AppTrafficStat {
        val trafficStat = AppTrafficStat(uid)
        try {
            val rcvFile = String.format(Locale.US, DEV_UID_RCV_FILE, uid)
            val rcvData = readAllLines(rcvFile)
            trafficStat.recvBytes = parseLong(rcvData, 0)
            val sndFile = String.format(Locale.US, DEV_UID_SND_FILE, uid)
            val sndData = readAllLines(sndFile)
            trafficStat.sendBytes = parseLong(sndData, 0)
            return trafficStat
        } catch (e: IOException) {
            // The files may not exist if no traffic happens
        }
        return trafficStat
    }
}
