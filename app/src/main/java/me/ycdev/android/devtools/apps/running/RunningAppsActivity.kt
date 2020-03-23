package me.ycdev.android.devtools.apps.running

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.databinding.ActCommonListBinding
import timber.log.Timber

class RunningAppsActivity : AppCompatBaseActivity() {
    private lateinit var binding: ActCommonListBinding

    private lateinit var listAdapter: RunningAppsAdapter
    private lateinit var viewModel: RunningAppsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActCommonListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = RunningAppsAdapter()
        binding.list.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@RunningAppsActivity)
            addItemDecoration(
                DividerItemDecoration(this@RunningAppsActivity, DividerItemDecoration.VERTICAL)
            )
        }

        viewModel = ViewModelProvider(this).get(RunningAppsViewModel::class.java)
        viewModel.apps.observe(this, Observer {
            Timber.tag(TAG).d("apps loaded: ${it.size}")
            listAdapter.data = it
            listAdapter.notifyDataSetChanged()
            binding.progress.visibility = View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.running_apps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_refresh) {
            binding.progress.visibility = View.VISIBLE
            viewModel.refreshApps()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "RunningAppsActivity"
    }
}