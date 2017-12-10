package me.ycdev.android.devtools.device;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.arch.wrapper.ToastHelper;
import me.ycdev.android.lib.common.wrapper.BroadcastHelper;
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity;
import me.ycdev.android.devtools.utils.Constants;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Telephony;
import android.widget.Toast;

import static me.ycdev.android.arch.ArchConstants.IntentType.INTENT_TYPE_ACTIVITY;
import static me.ycdev.android.arch.ArchConstants.IntentType.INTENT_TYPE_BROADCAST;

public class BroadcastTesterActivity extends GridEntriesActivity {
    private String mTargetPkgName;

    private BroadcastReceiver mReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ToastHelper.show(getApplicationContext(), "Received1: " + intent.getAction(),
                    Toast.LENGTH_LONG);
        }
    };

    private BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ToastHelper.show(getApplicationContext(), "Received2: " + intent.getAction(),
                    Toast.LENGTH_LONG);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        BroadcastHelper.registerForExternal(this, mReceiver1,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        BroadcastHelper.registerForInternal(this, mReceiver2,
                new IntentFilter(Constants.ACTION_DYNAMIC_BROADCAST_TEST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver1);
        unregisterReceiver(mReceiver2);
    }

    @Override
    protected boolean needLoadIntentsAsync() {
        return true;
    }

    @Override
    protected List<IntentEntry> getIntents() {
        List<IntentEntry> broadcasts = new ArrayList<IntentEntry>();
        mTargetPkgName = getPackageName();
        testBroadcastWithPerm(broadcasts);
        testBroadcastWithoutPerm(broadcasts);
        testPackageAdd(broadcasts);
        testPackageReplaced(broadcasts);
        testPackageRemoved(broadcasts);
        testPackageChanged(broadcasts);
        testNewOutgoingCall(broadcasts);
        testSmsReceived(broadcasts);
        testSmsDeliver(broadcasts);
        testConnectivityChange(broadcasts);
        testWifiStateChange(broadcasts);
        testBootComplete(broadcasts);
        testShutdown(broadcasts);
        testTimeSet(broadcasts);
        testAppWidgetUpdate(broadcasts);
        testBluetoothStateChange(broadcasts);
        testAirplaceModeChange(broadcasts);
        testLocationChanged(broadcasts);
        return broadcasts;
    }

    @SuppressLint("MyBroadcastHelper")
    @Override
    protected void onItemClicked(IntentEntry item) {
        try {
            if (item.type == INTENT_TYPE_ACTIVITY) {
                startActivity(item.intent);
            } else if (item.type == INTENT_TYPE_BROADCAST) {
                sendBroadcast(item.intent, item.perm);
            }
        } catch (Exception e) {
            showCrashInfo(e);
            e.printStackTrace();
        }
    }

    private void showCrashInfo(Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crashe Info");
        builder.setMessage(e.toString());
        builder.show();
    }

    private void testBroadcastWithPerm(List<IntentEntry> itemsList) {
        Intent intent = new Intent(Constants.ACTION_DYNAMIC_BROADCAST_TEST);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "Broadcast#1",
                "Send Broadcast with perm: " + intent.getAction());
        item.perm = BroadcastHelper.getInternalBroadcastPerm(this);
        itemsList.add(item);
    }

    private void testBroadcastWithoutPerm(List<IntentEntry> itemsList) {
        Intent intent = new Intent(Constants.ACTION_DYNAMIC_BROADCAST_TEST);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "Broadcast#2",
                "Send Broadcast without perm: " + intent.getAction());
        itemsList.add(item);
    }


    private void testPackageAdd(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "PackageAdded",
                "Send package added broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageReplaced(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REPLACED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "PackageReplaced",
                "Send package replaced broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageRemoved(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REMOVED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "PackageRemoved",
                "Send package removed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageChanged(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_CHANGED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "PackageChanged",
                "Send package changed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testNewOutgoingCall(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_NEW_OUTGOING_CALL);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "NewOutgoingCall",
                "Send new outgoing call broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testSmsReceived(List<IntentEntry> itemsList) {
        // OK: the broadcast has no protection (last checked version: android-4.4_r1.1)
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "SmsReceived",
                "Send SMS received broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testSmsDeliver(List<IntentEntry> itemsList) {
        // Permission Denial: android.permission.BROADCAST_SMS (signature protection) (Supported by android-4.4)
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "SmsDeliver",
                "Send SMS deliver broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testConnectivityChange(List<IntentEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.0.1_r1)
        // b) SecurityException: protected-broadcast (android-4.0.1_r1+)
        // Can be received even though the receiver is not exported!
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "ConnectChange",
                "Send network connectivity change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testWifiStateChange(List<IntentEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.2.2_r1)
        // b) SecurityException: protected-broadcast (android-4.2.2_r1+)
        Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "WifiChange",
                "Send wifi state change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testBootComplete(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "BootCompleted",
                "Send boot completed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testShutdown(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_SHUTDOWN);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "Shutdown",
                "Send shutdown broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testTimeSet(List<IntentEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        Intent intent = new Intent(Intent.ACTION_TIME_CHANGED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "TimeSet",
                "Send time set broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testAppWidgetUpdate(List<IntentEntry> itemsList) {
        // OK: no protection (last checked version: android-4.4_r1.1)
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "AppWidgetUpdate",
                "Send app widget update broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testBluetoothStateChange(List<IntentEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "BluetoothChange",
                "Send bluetooth state change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testAirplaceModeChange(List<IntentEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.3_r0.9)
        // b) SecurityException: protected-broadcast (android-4.3_r0.9+)
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "AirplaceChange",
                "Send airplane mode change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void testLocationChanged(List<IntentEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        Intent intent = new Intent(LocationManager.PROVIDERS_CHANGED_ACTION);
        intent.setPackage(mTargetPkgName);
        IntentEntry item = new IntentEntry(INTENT_TYPE_BROADCAST, intent, "LocationChange",
                "Send location change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

}
