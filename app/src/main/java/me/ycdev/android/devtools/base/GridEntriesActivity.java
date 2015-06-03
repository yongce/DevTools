package me.ycdev.android.devtools.base;

import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.utils.IntentUtils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class GridEntriesActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static class IntentEntry {
        public static final int TYPE_ACTIVITY = 1;
        public static final int TYPE_BROADCAST = 2;

        public Intent intent;
        public String title;
        public String desc;
        public int type = TYPE_ACTIVITY;
        public String perm;

        public IntentEntry(Intent intent, String title, String desc) {
            this.intent = intent;
            this.title = title;
            this.desc = desc;
        }
    }

    protected SystemEntriesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_entries);

        mAdapter = new SystemEntriesAdapter(this);

        GridView gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

        loadItems();
    }

    private void loadItems() {
        if (needLoadIntentsAsync()) {
            new AsyncTask<Void, Void, List<IntentEntry>>() {
                @Override
                protected List<IntentEntry> doInBackground(Void... params) {
                    return getIntents();
                }

                @Override
                protected void onPostExecute(List<IntentEntry> result) {
                    mAdapter.setData(getIntents());
                }
            }.execute();
        } else {
            mAdapter.setData(getIntents());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        IntentEntry item = mAdapter.getItem(position);
        onItemClicked(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        IntentEntry item = mAdapter.getItem(position);
        Toast.makeText(this, item.desc, Toast.LENGTH_LONG).show();
        return true;
    }

    /**
     * Decide if we need to invoke {@link #getIntent()} async.
     * @return true for async and false for sync. false by default
     */
    protected boolean needLoadIntentsAsync() {
        return false;
    }

    protected abstract List<IntentEntry> getIntents();

    protected void onItemClicked(IntentEntry item) {
        if (IntentUtils.canStartActivity(this, item.intent)) {
            startActivity(item.intent);
        } else {
            Toast.makeText(this, item.desc, Toast.LENGTH_LONG).show();
        }
    }

    protected static class SystemEntriesAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<IntentEntry> mList;

        public SystemEntriesAdapter(Context cxt) {
            mInflater = LayoutInflater.from(cxt);
        }

        public void setData(List<IntentEntry> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public IntentEntry getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IntentEntry item = getItem(position);
            ViewHolder holder = ViewHolder.get(convertView, parent, mInflater);
            holder.titleView.setText(item.title);
            return holder.rootView;
        }

        private static class ViewHolder {
            public View rootView;
            public TextView titleView;

            public ViewHolder(View convertView) {
                rootView = convertView;
                titleView = (TextView) rootView.findViewById(R.id.title);
            }

            public static ViewHolder get(View convertView, ViewGroup parent, LayoutInflater inflater) {
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.grid_entries_item, parent, false);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                return holder;
            }
        }
    }
}
