package zw.co.sytbay.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class FeedItem(val title:String,val excerpt:String,val link:String,val badge:String)

class MainActivity : AppCompatActivity() {
    private val navy = Color.rgb(13,33,56)
    private val blue = Color.rgb(20,107,194)
    private val soft = Color.rgb(245,247,251)
    private val muted = Color.rgb(96,112,132)
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var titleView:TextView
    private lateinit var subtitleView:TextView
    private lateinit var recycler:RecyclerView
    private val adapter = FeedAdapter { open(it.link) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = navy
        setContentView(buildUi())
        showHome()
    }

    private fun buildUi(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(soft)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20),dp(24),dp(20),dp(20))
            setBackgroundColor(navy)
        }
        titleView = TextView(this).apply {
            setTextColor(Color.WHITE); textSize=28f; setTypeface(typeface,Typeface.BOLD)
        }
        subtitleView = TextView(this).apply {
            setTextColor(Color.rgb(220,232,244)); textSize=15f; setPadding(0,dp(6),0,0)
        }
        header.addView(titleView)
        header.addView(subtitleView)
        root.addView(header, LinearLayout.LayoutParams(-1,-2))

        recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setPadding(dp(14),dp(14),dp(14),dp(14))
            clipToPadding=false
        }
        root.addView(recycler, LinearLayout.LayoutParams(-1,0,1f))

        val nav = BottomNavigationView(this)
        nav.menu.add(0,1,0,"Home").setIcon(android.R.drawable.ic_menu_view)
        nav.menu.add(0,2,1,"Learn").setIcon(android.R.drawable.ic_menu_edit)
        nav.menu.add(0,3,2,"Resources").setIcon(android.R.drawable.ic_menu_save)
        nav.menu.add(0,4,3,"Directory").setIcon(android.R.drawable.ic_menu_myplaces)
        nav.menu.add(0,5,4,"Account").setIcon(android.R.drawable.ic_menu_manage)
        nav.setBackgroundColor(Color.WHITE)
        nav.setOnItemSelectedListener {
            when(it.itemId){2->showLearn();3->showResources();4->showDirectory();5->showAccount();else->showHome()}
            true
        }
        root.addView(nav, LinearLayout.LayoutParams(-1,-2))
        return root
    }

    private fun heading(title:String, subtitle:String) {
        titleView.text=title; subtitleView.text=subtitle
    }

    private fun showHome() {
        heading("Sytbay","Learn for school. Prepare for exams. Build real skills.")
        loadMany(listOf(
            "https://sytbay.co.zw/wp-json/wp/v2/sed_inst_update?per_page=5&orderby=date&order=desc" to "Institution update",
            "https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=8&orderby=date&order=desc" to "Latest learning"
        ))
    }

    private fun showLearn() {
        heading("Learn","Syllabus-aligned study notes and explanations.")
        load("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=30&slc_resource_type=4843","Study note")
    }

    private fun showResources() {
        heading("Resources","Past papers, files and downloadable study material.")
        load("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=30&slc_resource_type=4944","Download")
    }

    private fun showDirectory() {
        heading("College Directory","Browse Zimbabwe institutions and verified programme information.")
        load("https://sytbay.co.zw/wp-json/wp/v2/sed_institution?per_page=50&orderby=title&order=asc","Institution")
    }

    private fun showAccount() {
        heading("Your Sytbay","Saved learning and native account sync are next.")
        adapter.submit(listOf(
            FeedItem("My Learning","Open your Sytbay learning dashboard.","https://sytbay.co.zw/learn/dashboard/","Account"),
            FeedItem("College Updates","Current intakes, deadlines and institution notices.","https://sytbay.co.zw/institution-updates/","Admissions"),
            FeedItem("Sytbay Website","Open the full Sytbay Academy website.","https://sytbay.co.zw/","Website")
        ))
    }

    private fun load(url:String,badge:String) = loadMany(listOf(url to badge))

    private fun loadMany(requests:List<Pair<String,String>>) {
        adapter.submit(listOf(FeedItem("Loading…","Fetching the latest content from Sytbay.","https://sytbay.co.zw/","Online")))
        executor.execute {
            try {
                val all = mutableListOf<FeedItem>()
                requests.forEach { (url,badge) -> all.addAll(fetch(url,badge)) }
                runOnUiThread { adapter.submit(all) }
            } catch(e:Exception) {
                runOnUiThread { adapter.submit(listOf(FeedItem("Could not load Sytbay","Check your internet connection, then select the tab again.","https://sytbay.co.zw/","Offline"))) }
            }
        }
    }

    private fun fetch(endpoint:String,badge:String):List<FeedItem> {
        val c = URL(endpoint).openConnection() as HttpURLConnection
        c.connectTimeout=15000; c.readTimeout=20000
        c.setRequestProperty("Accept","application/json")
        val text = c.inputStream.bufferedReader().use { it.readText() }
        c.disconnect()
        val array=JSONArray(text)
        return (0 until array.length()).map { i ->
            val o=array.getJSONObject(i)
            val title=o.getJSONObject("title").optString("rendered")
            val excerpt=o.optJSONObject("excerpt")?.optString("rendered").orEmpty()
            FeedItem(html(title),html(excerpt),o.optString("link","https://sytbay.co.zw/"),badge)
        }
    }

    private fun html(v:String)=Html.fromHtml(v,Html.FROM_HTML_MODE_LEGACY).toString().trim()
    private fun open(url:String)=startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()

    inner class FeedAdapter(private val onClick:(FeedItem)->Unit):RecyclerView.Adapter<FeedAdapter.Holder>() {
        private val items= mutableListOf<FeedItem>()
        fun submit(v:List<FeedItem>){items.clear();items.addAll(v);notifyDataSetChanged()}
        override fun getItemCount()=items.size
        override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):Holder {
            val card=MaterialCardView(parent.context).apply {
                radius=dp(16).toFloat(); cardElevation=dp(1).toFloat()
                strokeWidth=dp(1); setStrokeColor(Color.rgb(225,232,240))
                setCardBackgroundColor(Color.WHITE)
                layoutParams=RecyclerView.LayoutParams(-1,-2).apply{bottomMargin=dp(12)}
            }
            val box=LinearLayout(parent.context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(18))}
            val badge=TextView(parent.context).apply{setTextColor(blue);textSize=12f;setTypeface(typeface,Typeface.BOLD)}
            val title=TextView(parent.context).apply{setTextColor(navy);textSize=19f;setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(8),0,0)}
            val excerpt=TextView(parent.context).apply{setTextColor(muted);textSize=14f;setPadding(0,dp(7),0,0);maxLines=3}
            val open=TextView(parent.context).apply{setTextColor(blue);textSize=13f;text="Open on Sytbay →";setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(10),0,0)}
            box.addView(badge);box.addView(title);box.addView(excerpt);box.addView(open);card.addView(box)
            return Holder(card,badge,title,excerpt)
        }
        override fun onBindViewHolder(h:Holder,p:Int){val x=items[p];h.badge.text=x.badge;h.title.text=x.title;h.excerpt.text=x.excerpt.ifBlank{"Open to view full details."};h.card.setOnClickListener{onClick(x)}}
        inner class Holder(val card:MaterialCardView,val badge:TextView,val title:TextView,val excerpt:TextView):RecyclerView.ViewHolder(card)
    }
}
