package me.ycdev.android.devtools

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity

class CommonIntentService :
    Service(),
    Callback {
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("CommonIntentService")
        handlerThread.start()
        handler = Handler(handlerThread.looper, this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        handler.obtainMessage(MSG_HANDLE_INTENT, startId, 0, intent).sendToTarget()
        return START_NOT_STICKY
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what != MSG_HANDLE_INTENT) {
            return false
        }
        handleIntent(msg.obj as? Intent)
        stopSelf(msg.arg1)
        return true
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handlerThread.quitSafely()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        if (ACTION_UNMARSHALL_SCANNER == action) {
            UnmarshallScannerActivity.scanUnmarshallIssue(this)
        }
    }

    companion object {
        private const val ACTION_PREFIX = "class.action."
        private const val MSG_HANDLE_INTENT = 1

        const val ACTION_UNMARSHALL_SCANNER = ACTION_PREFIX + "UNMARSHALL_SCANNER"
    }
}
