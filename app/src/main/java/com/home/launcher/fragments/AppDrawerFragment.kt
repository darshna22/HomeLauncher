package com.home.launcher.fragments

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.MainActivity
import com.home.launcher.R
import com.layoutxml.applistmanagerlibrary.objects.AppData
import java.util.*
import kotlin.collections.ArrayList

class AppDrawerFragment : Fragment(), Filterable {
  @JvmField var appListAdapter: AppAdapter? = null
  @JvmField var progressBar: ProgressBar? = null
  lateinit var appDataList: List<AppData>
  lateinit var filterList: List<AppData>
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.app_drawer, container, false)
    progressBar = view.findViewById(R.id.progressBar)
    appDataList = MainActivity.appDataList
    appListAdapter = AppAdapter(activity, appDataList)
    val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
    val mSearchView: SearchView = view.findViewById(R.id.mSearchView)
    recyclerView.setHasFixedSize(true)
    recyclerView.adapter = appListAdapter
    recyclerView.layoutManager = LinearLayoutManager(view.context)

    mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(s: String?): Boolean {
        return false
      }

      override fun onQueryTextChange(s: String?): Boolean {
        filter.filter(s);
        return false
      }
    })
    mSearchView.setOnCloseListener {
      val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
      mSearchView.clearFocus()
      false
    }
    return view
  }

  inner class AppAdapter(
    private val context: Context?, mList: List<AppData> = emptyList()
  ) :
    RecyclerView.Adapter<TagViewHolder>() {
    var list: List<AppData> = mList

    init {
      progressBar!!.visibility = View.GONE

    }

    fun setDataList(mList: List<AppData>) {
      list = mList
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
      val inflater = LayoutInflater.from(parent.context)
      return TagViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
      val appData: AppData = list[position]
      holder.bind(appData, context)
      holder.itemView.setOnClickListener {
        val name = ComponentName(
          list.get(position).packageName,
          list.get(position).activityName
        )
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        i.component = name
        context?.startActivity(i)
      }
    }

    override fun getItemCount(): Int = list.size

  }

  class TagViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.app_item_view, parent, false)) {
    private var appIcon: ImageView? = null
    private var appName: TextView? = null
    private var packageName: TextView? = null

    init {
      appIcon = itemView.findViewById(R.id.itemLogo)
      packageName = itemView.findViewById((R.id.itemPackageName))
      appName = itemView.findViewById((R.id.itemName))

    }

    fun bind(appData: AppData, context: Context?) {
      val mAppName = appData.name
      val appPackageName = appData.packageName
      val mAppIcon = appData.icon
      appName?.text = mAppName
      packageName?.text = appPackageName
      appIcon?.setImageDrawable(mAppIcon)
      appIcon?.contentDescription = mAppName
    }

  }

  inner class ValueFilter : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
      val results = FilterResults()
      if (constraint != null && constraint.isNotEmpty()) {
        val filterList: ArrayList<AppData> = ArrayList<AppData>()
        for (i in 0 until appDataList.size) {
          if (appDataList.get(i).name.toUpperCase(Locale.ROOT)
                .contains(constraint.toString().toUpperCase(Locale.ROOT))
          ) {
            val appData = AppData()
            appData.name = appDataList[i].name
            appData.packageName = appDataList[i].packageName
            appData.icon = appDataList[i].icon
            appData.activityName = appDataList[i].activityName
            filterList.add(appData)
          }
        }
        results.count = filterList.size
        results.values = filterList
      } else {
        results.count = appDataList.size
        results.values = appDataList
      }
      return results
    }

    override fun publishResults(
      constraint: CharSequence?,
      results: FilterResults
    ) {
      filterList = results.values as MutableList<AppData>
      appListAdapter?.setDataList(filterList)
      appListAdapter?.notifyDataSetChanged()
    }
  }

  companion object {
    private const val TAG = "AppDrawerFragment"
  }

  override fun getFilter(): Filter {
    return ValueFilter()
  }

}