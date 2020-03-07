package me.ycdev.android.devtools.contacts

import android.Manifest.permission
import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.os.Bundle
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.arch.wrapper.ToastHelper
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.databinding.ActContactsBinding
import me.ycdev.android.lib.common.perms.PermissionCallback
import me.ycdev.android.lib.common.perms.PermissionRequestParams
import me.ycdev.android.lib.common.perms.PermissionUtils
import me.ycdev.android.lib.commonui.utils.WaitingAsyncTask
import timber.log.Timber
import java.util.ArrayList
import java.util.Locale
import java.util.Random

class ContactsActivity : AppCompatBaseActivity(), OnClickListener, PermissionCallback {
    private lateinit var binding: ActContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.query.setOnClickListener(this)
        binding.dump.setOnClickListener(this)
        binding.create.setOnClickListener(this)
        binding.create.isEnabled = false
        binding.delete.setOnClickListener(this)
        binding.count.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                binding.create.isEnabled = s.toString().isNotEmpty()
            }
        })

        if (!PermissionUtils.hasPermissions(
                this,
                *REQUESTED_PERMISSIONS
            )
        ) {
            PermissionUtils.requestPermissions(this, createPermissionRequestParams())
        } else {
            refreshContactsState()
        }
    }

    override fun onClick(v: View) {
        when {
            v === binding.query -> {
                refreshContactsState()
            }
            v === binding.dump -> {
                queryContactsCounts(true)
                queryDeletedContactsCounts(true)
            }
            v === binding.create -> {
                val countStr = binding.count.text.toString()
                if (countStr.isEmpty()) {
                    ToastHelper.show(
                        this, R.string.apps_sampler_sample_interval_input_toast,
                        Toast.LENGTH_SHORT
                    )
                    return
                }
                val count = binding.count.text.toString().toInt()
                WaitingAsyncTask(
                    this,
                    getString(R.string.contacts_msg_creating),
                    Runnable {
                        createContacts(count)
                        updateState(queryContactsCounts(false), queryDeletedContactsCounts(false))
                    }
                ).execute()
            }
            v === binding.delete -> {
                WaitingAsyncTask(
                    this,
                    getString(R.string.contacts_msg_deleting),
                    Runnable {
                        deleteContacts()
                        updateState(queryContactsCounts(false), queryDeletedContactsCounts(false))
                    }
                ).execute()
            }
        }
    }

    private fun refreshContactsState() {
        WaitingAsyncTask(
            this,
            getString(R.string.contacts_msg_querying),
            Runnable {
                updateState(
                    queryContactsCounts(false),
                    queryDeletedContactsCounts(false)
                )
            }
        ).execute()
    }

    private fun updateState(count: Int, deletedCount: Int) {
        runOnUiThread {
            val stateMsg = getString(R.string.contacts_count_state, count, deletedCount)
            binding.state.text = stateMsg
        }
    }

    private fun queryContactsCounts(dump: Boolean): Int {
        return doQueryContactsCounts(RAW_CONTACT_QUERY_SELECTION, dump)
    }

    private fun queryDeletedContactsCounts(dump: Boolean): Int {
        return doQueryContactsCounts(RAW_DELETED_CONTACT_QUERY_SELECTION, dump)
    }

    private fun doQueryContactsCounts(selection: String, dump: Boolean): Int {
        val projection = arrayOf(
            RawContacts._ID, // 0
            RawContacts.DISPLAY_NAME_PRIMARY, // 1
            RawContacts.STARRED, // 2
            RawContacts.DELETED
        )
        Timber.tag(TAG).d("query selection: %s", selection)
        val cursor = contentResolver.query(
            RawContacts.CONTENT_URI,
            projection, selection, null, null
        )
            ?: return 0
        return cursor.use {
            val count = it.count
            if (dump && count > 0) {
                Timber.tag(TAG).d("Dump contacts: %s", count)
                while (it.moveToNext()) {
                    val rowId = it.getLong(0)
                    val name = it.getString(1)
                    val starred = it.getInt(2)
                    val deleted = it.getInt(3)
                    Timber.tag(TAG).d(
                        "contact: $rowId, name: $name, starred: $starred, deleted: $deleted"
                    )
                }
            }
            count
        }
    }

    private fun createContacts(count: Int) {
        val random = Random(System.currentTimeMillis())
        val numberSet = arrayOfNulls<String>(count / 2)
        generateNumbers(numberSet, random)

        val numberTypeSet = intArrayOf(
            Phone.TYPE_MOBILE,
            Phone.TYPE_HOME,
            Phone.TYPE_WORK,
            Phone.TYPE_FAX_WORK,
            Phone.TYPE_COMPANY_MAIN
        )
        val batchGroup = ArrayList<ArrayList<ContentProviderOperation>>()
        var batchOps = ArrayList<ContentProviderOperation>(BATCH_OPERATIONS_MAX + 10)
        for (i in 0 until count) {
            val starred = if (random.nextInt(10) == 3) 1 else 0
            val rawContactInsertIndex = batchOps.size
            batchOps.add(
                ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.STARRED, starred)
                    .withValue(RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
                    .withValue(RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
                    .build()
            )
            val contactName = CONTACT_NAME_PREFIX + String.format(
                Locale.US,
                "%04d",
                i + 1
            )
            batchOps.add(
                ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(
                        Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, contactName)
                    .build()
            )

            val numberCount = 1 + random.nextInt(5)
            for (k in 0 until numberCount) {
                val number = numberSet[random.nextInt(numberSet.size)]
                val numberType = numberTypeSet[random.nextInt(numberTypeSet.size)]
                batchOps.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(
                            Data.RAW_CONTACT_ID,
                            rawContactInsertIndex
                        )
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, number)
                        .withValue(Phone.TYPE, numberType)
                        .build()
                )
            }
            if (batchOps.size > BATCH_OPERATIONS_MAX) {
                batchGroup.add(batchOps)
                batchOps = ArrayList(BATCH_OPERATIONS_MAX + 10)
            }
        }
        if (batchOps.size > 0) {
            batchGroup.add(batchOps)
        }
        try {
            for (ops in batchGroup) {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            e.printStackTrace()
        }
    }

    private fun generateNumbers(numberSet: Array<String?>, random: Random) {
        for (i in numberSet.indices) {
            numberSet[i] = CONTACT_NUMBER_PREFIX + (10000000 + random.nextInt(90000000))
        }
    }

    private fun deleteContacts() {
        // delete the contacts
        val deleteUri = RawContacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .build()
        contentResolver.delete(
            deleteUri,
            RAW_CONTACT_DELETE_SELECTION,
            null
        )
    }

    private fun createPermissionRequestParams(): PermissionRequestParams {
        val params = PermissionRequestParams()
        params.requestCode = PERMISSION_RC_CONTACTS
        params.permissions = REQUESTED_PERMISSIONS
        params.rationaleTitle = getString(R.string.title_permission_request)
        params.rationaleContent = getString(R.string.contacts_permissions_rationale)
        params.callback = this
        return params
    }

    override fun onRationaleDenied(requestCode: Int) {
        ToastHelper.show(this, R.string.contacts_msg_permission_denied, Toast.LENGTH_SHORT)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsGranted = PermissionUtils.verifyPermissions(grantResults)
        if (permissionsGranted) {
            refreshContactsState()
        } else {
            onRationaleDenied(0)
        }
    }

    companion object {
        private const val TAG = "ContactsActivity"

        private const val ACCOUNT_TYPE = "me.ycdev.android.devtools"
        private const val ACCOUNT_NAME = "contacts_creator"

        private const val CONTACT_NAME_PREFIX = "ycdev_#"
        private const val CONTACT_NUMBER_PREFIX = "+86168"

        private const val RAW_CONTACT_QUERY_SELECTION = (RawContacts.DISPLAY_NAME_PRIMARY +
                " like \'" + CONTACT_NAME_PREFIX + "%\'" +
                " AND " + RawContacts.DELETED + "!=1")
        private const val RAW_DELETED_CONTACT_QUERY_SELECTION =
            (RawContacts.DISPLAY_NAME_PRIMARY +
                    " like \'" + CONTACT_NAME_PREFIX + "%\'" +
                    " AND " + RawContacts.DELETED + "==1")
        private const val RAW_CONTACT_DELETE_SELECTION = (RawContacts.DISPLAY_NAME_PRIMARY +
                " like \'" + CONTACT_NAME_PREFIX + "%\'")

        // the exactly number is '499' limited by Android
        private const val BATCH_OPERATIONS_MAX = 400

        private const val PERMISSION_RC_CONTACTS = 1
        private val REQUESTED_PERMISSIONS = arrayOf(
            permission.READ_CONTACTS,
            permission.WRITE_CONTACTS
        )
    }
}