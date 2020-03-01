package me.ycdev.android.devtools.contacts;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.NonNull;
import me.ycdev.android.arch.activity.AppCompatBaseActivity;
import me.ycdev.android.arch.wrapper.ToastHelper;
import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.perms.PermissionCallback;
import me.ycdev.android.lib.common.perms.PermissionRequestParams;
import me.ycdev.android.lib.common.perms.PermissionUtils;
import me.ycdev.android.lib.commonui.utils.WaitingAsyncTask;
import timber.log.Timber;

public class ContactsActivity extends AppCompatBaseActivity implements View.OnClickListener,
        PermissionCallback {
    private static final String TAG = "ContactsActivity";

    private static final String ACCOUNT_TYPE = "me.ycdev.android.devtools";
    private static final String ACCOUNT_NAME = "contacts_creator";

    private static final String CONTACT_NAME_PREFIX = "ycdev_#";
    private static final String CONTACT_NUMBER_PREFIX = "+86168";

    private static final String RAW_CONTACT_QUERY_SELECTION = RawContacts.DISPLAY_NAME_PRIMARY
            + " like \'" + CONTACT_NAME_PREFIX + "%\'"
            + " AND " + RawContacts.DELETED + "!=1";
    private static final String RAW_DELETED_CONTACT_QUERY_SELECTION = RawContacts.DISPLAY_NAME_PRIMARY
            + " like \'" + CONTACT_NAME_PREFIX + "%\'"
            + " AND " + RawContacts.DELETED + "==1";
    private static final String RAW_CONTACT_DELETE_SELECTION = RawContacts.DISPLAY_NAME_PRIMARY
            + " like \'" + CONTACT_NAME_PREFIX + "%\'";

    private static final int BATCH_OPERATIONS_MAX = 400; // the exactly number is '499' limited by Android

    private static final int PERMISSION_RC_CONTACTS = 1;
    private static final String[] REQUESTED_PERMISSIONS = new String[] {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    private TextView mStateView;
    private Button mQueryBtn;
    private Button mDumpBtn;
    private EditText mCountView;
    private Button mCreateBtn;
    private Button mDeleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_contacts);

        mStateView = (TextView) findViewById(R.id.state);
        mQueryBtn = (Button) findViewById(R.id.query);
        mQueryBtn.setOnClickListener(this);
        mDumpBtn = (Button) findViewById(R.id.dump);
        mDumpBtn.setOnClickListener(this);
        mCountView = (EditText) findViewById(R.id.count);
        mCreateBtn = (Button) findViewById(R.id.create);
        mCreateBtn.setOnClickListener(this);
        mCreateBtn.setEnabled(false);
        mDeleteBtn = (Button) findViewById(R.id.delete);
        mDeleteBtn.setOnClickListener(this);

        mCountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                mCreateBtn.setEnabled(!s.toString().isEmpty());
            }
        });

        if (!PermissionUtils.INSTANCE.hasPermissions(this, REQUESTED_PERMISSIONS)) {
            PermissionUtils.INSTANCE.requestPermissions(this, createPermissionRequestParams());
        } else {
            refreshContactsState();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mQueryBtn) {
            refreshContactsState();
        } else if (v == mDumpBtn) {
            queryContactsCounts(true);
            queryDeletedContactsCounts(true);
        } else if (v == mCreateBtn) {
            String countStr = mCountView.getText().toString();
            if (countStr.length() == 0) {
                ToastHelper.INSTANCE.show(this, R.string.apps_sampler_sample_interval_input_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            final int count = Integer.parseInt(mCountView.getText().toString());
            new WaitingAsyncTask(this, getString(R.string.contacts_msg_creating), new Runnable() {
                @Override
                public void run() {
                    createContacts(count);
                    updateState(queryContactsCounts(false), queryDeletedContactsCounts(false));
                }
            }).execute();
        } else if (v == mDeleteBtn) {
            new WaitingAsyncTask(this, getString(R.string.contacts_msg_deleting), new Runnable() {
                @Override
                public void run() {
                    deleteContacts();
                    updateState(queryContactsCounts(false), queryDeletedContactsCounts(false));
                }
            }).execute();
        }
    }

    private void refreshContactsState() {
        new WaitingAsyncTask(this, getString(R.string.contacts_msg_querying), new Runnable() {
            @Override
            public void run() {
                updateState(queryContactsCounts(false), queryDeletedContactsCounts(false));
            }
        }).execute();
    }

    private void updateState(final int count, final int deletedCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String stateMsg = getString(R.string.contacts_count_state, count, deletedCount);
                mStateView.setText(stateMsg);
            }
        });
    }

    private int queryContactsCounts(boolean dump) {
        return doQueryContactsCounts(RAW_CONTACT_QUERY_SELECTION, dump);
    }

    private int queryDeletedContactsCounts(boolean dump) {
        return doQueryContactsCounts(RAW_DELETED_CONTACT_QUERY_SELECTION, dump);
    }

    private int doQueryContactsCounts(String selection, boolean dump) {
        String[] projection = new String[] {
                RawContacts._ID, // 0
                RawContacts.DISPLAY_NAME_PRIMARY, // 1
                RawContacts.STARRED, // 2
                RawContacts.DELETED, // 3
        };
        Timber.tag(TAG).d("query selection: %s", selection);
        Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI,
                projection, selection, null, null);
        if (cursor == null) {
            return 0;
        }

        try {
            final int COUNT = cursor.getCount();
            if (dump && COUNT > 0) {
                Timber.tag(TAG).d("Dump contacts: %s", COUNT);
                while (cursor.moveToNext()) {
                    long rowId = cursor.getLong(0);
                    String name = cursor.getString(1);
                    int starred = cursor.getInt(2);
                    int deleted = cursor.getInt(3);
                    Timber.tag(TAG).d("contact: " + rowId + ", name: " + name
                            + ", starred: " + starred + ", deleted: " + deleted);
                }
            }
            return COUNT;
        } finally {
            cursor.close();
        }
    }

    private void createContacts(final int count) {
        Random random = new Random(System.currentTimeMillis());
        String[] numberSet = new String[count / 2];
        generateNumbers(numberSet, random);
        int[] numberTypeSet = new int[] {
                Phone.TYPE_MOBILE,
                Phone.TYPE_HOME,
                Phone.TYPE_WORK,
                Phone.TYPE_FAX_WORK,
                Phone.TYPE_COMPANY_MAIN,
        };
        ArrayList<ArrayList<ContentProviderOperation>> batchGroup = new ArrayList<>();
        ArrayList<ContentProviderOperation> batchOps = new ArrayList<>(BATCH_OPERATIONS_MAX + 10);

        for (int i = 0; i < count; i++) {
            int starred = (random.nextInt(10) == 3 ? 1 : 0);
            int rawContactInsertIndex = batchOps.size();
            batchOps.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.STARRED, starred)
                    .withValue(RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
                    .withValue(RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
                    .build());

            String contactName = CONTACT_NAME_PREFIX + String.format(Locale.US, "%04d", i + 1);
            batchOps.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, contactName)
                    .build());

            final int numberCount = 1 + random.nextInt(5);
            for (int k = 0; k < numberCount; k++) {
                String number = numberSet[random.nextInt(numberSet.length)];
                int numberType = numberTypeSet[random.nextInt(numberTypeSet.length)];

                batchOps.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, number)
                        .withValue(Phone.TYPE, numberType)
                        .build());
            }

            if (batchOps.size() > BATCH_OPERATIONS_MAX) {
                batchGroup.add(batchOps);
                batchOps = new ArrayList<>(BATCH_OPERATIONS_MAX + 10);
            }
        }
        if (batchOps.size() > 0) {
            batchGroup.add(batchOps);
        }

        try {
            for (ArrayList<ContentProviderOperation> ops : batchGroup) {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            }
        }  catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void generateNumbers(String[] numberSet, Random random) {
        for (int i = 0; i < numberSet.length; i++) {
            numberSet[i] = CONTACT_NUMBER_PREFIX + (10000000 + random.nextInt(90000000));
        }
    }

    private void deleteContacts() {
        // delete the contacts
        Uri deleteUri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();
        getContentResolver().delete(deleteUri, RAW_CONTACT_DELETE_SELECTION, null);
    }

    private PermissionRequestParams createPermissionRequestParams() {
        PermissionRequestParams params = new PermissionRequestParams();
        params.setRequestCode(PERMISSION_RC_CONTACTS);
        params.setPermissions(REQUESTED_PERMISSIONS);
        params.setRationaleTitle(getString(R.string.title_permission_request));
        params.setRationaleContent(getString(R.string.contacts_permissions_rationale));
        params.setCallback(this);
        return params;
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        ToastHelper.INSTANCE.show(this, R.string.contacts_msg_permission_denied, Toast.LENGTH_SHORT);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionsGranted = PermissionUtils.INSTANCE.verifyPermissions(grantResults);
        if (permissionsGranted) {
            refreshContactsState();
        } else {
            onRationaleDenied(0);
        }
    }

}
