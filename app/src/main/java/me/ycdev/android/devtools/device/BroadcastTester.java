package me.ycdev.android.devtools.device;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.base.GridEntriesActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Telephony;

public class BroadcastTester extends GridEntriesActivity {
    private String mTargetPkgName;

    @Override
    protected boolean needLoadIntentsAsync() {
        return true;
    }

    @Override
    protected List<ActivityEntry> getIntents() {
        List<ActivityEntry> broadcasts = new ArrayList<ActivityEntry>();
        mTargetPkgName = getPackageName();
        testDevTools(broadcasts);
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

    @Override
    protected void onItemClicked(ActivityEntry item) {
        try {
            //sendBroadcast(item.intent);
            startActivity(item.intent);
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

    private void testDevTools(List<ActivityEntry> itemsList) {
        Intent intent = new Intent("me.ycdev.devtools.action.test");
        ActivityEntry item = new ActivityEntry(intent, "DevToolTest",
                "Send DevTools test broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageAdd(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "PackageAdded",
                "Send package added broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageReplaced(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REPLACED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "PackageReplaced",
                "Send package replaced broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageRemoved(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REMOVED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "PackageRemoved",
                "Send package removed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testPackageChanged(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_PACKAGE_CHANGED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "PackageChanged",
                "Send package changed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testNewOutgoingCall(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_NEW_OUTGOING_CALL);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "NewOutgoingCall",
                "Send new outgoing call broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testSmsReceived(List<ActivityEntry> itemsList) {
        // OK: the broadcast has no protection (last checked version: android-4.4_r1.1)
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "SmsReceived",
                "Send SMS received broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testSmsDeliver(List<ActivityEntry> itemsList) {
        // Permission Denial: android.permission.BROADCAST_SMS (signature protection) (Supported by android-4.4)
        Intent intent = new Intent(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "SmsDeliver",
                "Send SMS deliver broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testConnectivityChange(List<ActivityEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.0.1_r1)
        // b) SecurityException: protected-broadcast (android-4.0.1_r1+)
        // Can be received even though the receiver is not exported!
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "ConnectChange",
                "Send network connectivity change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testWifiStateChange(List<ActivityEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.2.2_r1)
        // b) SecurityException: protected-broadcast (android-4.2.2_r1+)
        Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "WifiChange",
                "Send wifi state change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testBootComplete(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "BootCompleted",
                "Send boot completed broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testShutdown(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(Intent.ACTION_SHUTDOWN);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "Shutdown",
                "Send shutdown broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testTimeSet(List<ActivityEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        Intent intent = new Intent(Intent.ACTION_TIME_CHANGED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "TimeSet",
                "Send time set broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testAppWidgetUpdate(List<ActivityEntry> itemsList) {
        // OK: no protection (last checked version: android-4.4_r1.1)
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "AppWidgetUpdate",
                "Send app widget update broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testBluetoothStateChange(List<ActivityEntry> itemsList) {
        // SecurityException: protected-broadcast
        Intent intent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "BluetoothChange",
                "Send bluetooth state change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    private void testAirplaceModeChange(List<ActivityEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.3_r0.9)
        // b) SecurityException: protected-broadcast (android-4.3_r0.9+)
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "AirplaceChange",
                "Send airplane mode change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void testLocationChanged(List<ActivityEntry> itemsList) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        Intent intent = new Intent(LocationManager.PROVIDERS_CHANGED_ACTION);
        intent.setPackage(mTargetPkgName);
        ActivityEntry item = new ActivityEntry(intent, "LocationChange",
                "Send location change broadcast: " + intent.getAction());
        itemsList.add(item);
    }

}
