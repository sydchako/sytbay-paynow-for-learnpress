package zw.co.sytbay.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
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

class MainActivity:AppCompatActivity(){
    private val navy=Color.rgb(13,33,56);private val blue=Color.rgb(20,107,194);private val soft=Color.rgb(245,247,251);private val muted=Color.rgb(96,112,132)
    private val executor=Executors.newSingleThreadExecutor();private lateinit var titleView:TextView;private lateinit var subtitleView:TextView;private lateinit var search:SearchView;private lateinit var recycler:RecyclerView;private lateinit var store:OfflineStore
    private val adapter=FeedAdapter{open(it)};private var all=listOf<FeedItem>();private var current=1
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=navy;store=OfflineStore(this);setContentView(ui());showHome()}
    private fun ui():LinearLayout{
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(soft)}
        val header=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(22),dp(18),dp(12));setBackgroundColor(navy)}
        titleView=TextView(this).apply{setTextColor(Color.WHITE);textSize=27f;setTypeface(typeface,Typeface.BOLD)}
        subtitleView=TextView(this).apply{setTextColor(Color.rgb(220,232,244));textSize=14f;setPadding(0,dp(5),0,dp(8))}
        search=SearchView(this).apply{queryHint="Search";setIconifiedByDefault(false);setBackgroundColor(Color.WHITE)}
        search.setOnQueryTextListener(object:SearchView.OnQueryTextListener{override fun onQueryTextSubmit(q:String?)=false;override fun onQueryTextChange(q:String?):Boolean{filter(q.orEmpty());return true}})
        header.addView(titleView);header.addView(subtitleView);header.addView(search,LinearLayout.LayoutParams(-1,dp(48)));root.addView(header)
        recycler=RecyclerView(this).apply{layoutManager=LinearLayoutManager(this@MainActivity);adapter=this@MainActivity.adapter;setPadding(dp(13),dp(14),dp(13),dp(14));clipToPadding=false}
        root.addView(recycler,LinearLayout.LayoutParams(-1,0,1f))
        val nav=BottomNavigationView(this);nav.menu.add(0,1,0,"Home").setIcon(android.R.drawable.ic_menu_view);nav.menu.add(0,2,1,"Learn").setIcon(android.R.drawable.ic_menu_edit);nav.menu.add(0,3,2,"Resources").setIcon(android.R.drawable.ic_menu_save);nav.menu.add(0,4,3,"Directory").setIcon(android.R.drawable.ic_menu_myplaces);nav.menu.add(0,5,4,"Offline").setIcon(android.R.drawable.ic_menu_recent_history)
        nav.setBackgroundColor(Color.WHITE);nav.setOnItemSelectedListener{current=it.itemId;search.setQuery("",false);when(current){2->showLearn();3->showResources();4->showDirectory();5->showOffline();else->showHome()};true};root.addView(nav);return root
    }
    private fun heading(t:String,s:String){titleView.text=t;subtitleView.text=s}
    private data class Req(val url:String,val type:String,val badge:String)
    private fun showHome(){heading("Sytbay","Learn for school. Prepare for exams. Build real skills.");loadMany(listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=12&orderby=date&order=desc","slc_content","Latest learning"),Req("https://sytbay.co.zw/wp-json/wp/v2/sed_inst_update?per_page=6&orderby=date&order=desc","sed_inst_update","College update")))}
    private fun showLearn(){heading("Learn","Open any learning page once and it is saved to this phone.");loadMany(listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=60&slc_resource_type=4843&orderby=modified&order=desc","slc_content","Study note")))}
    private fun showResources(){heading("Resources","Downloads, worked solutions, practicals and practice.");loadMany(listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=25&slc_resource_type=4944","slc_content","Download"),Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=25&slc_resource_type=4861","slc_content","Worked solution"),Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=20&slc_resource_type=4849","slc_content","Practical"),Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=20&slc_resource_type=4967","slc_content","Practice")))}
    private fun showDirectory(){heading("College Directory","Search institutions and programmes.");loadMany(listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/sed_institution?per_page=100&orderby=title&order=asc","sed_institution","Institution"),Req("https://sytbay.co.zw/wp-json/wp/v2/sed_programme?per_page=100&orderby=title&order=asc","sed_programme","Programme")))}
    private fun showOffline(){val n=store.countLearning();heading("Offline Library",n.toString()+" learning "+(if(n==1)"page" else "pages")+" saved on this phone.");all=store.learning().map{FeedItem(it.id,it.type,it.title,it.excerpt,it.link,"Offline")};adapter.submit(all)}
    private fun loadMany(reqs:List<Req>){adapter.submit(listOf(FeedItem(0,"","Loading…","Fetching current Sytbay content.","","Online")));executor.execute{try{val out=mutableListOf<FeedItem>();reqs.forEach{out.addAll(fetch(it))};runOnUiThread{all=out;adapter.submit(out)}}catch(_:Exception){runOnUiThread{adapter.submit(listOf(FeedItem(0,"","Could not refresh","Check your connection. Pages already in Offline remain available.","","Offline")))}}}}
    private fun fetch(r:Req):List<FeedItem>{val c=(URL(r.url).openConnection() as HttpURLConnection).apply{connectTimeout=15000;readTimeout=30000;setRequestProperty("Accept","application/json");setRequestProperty("User-Agent","Sytbay-Android/0.2")};val text=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();val arr=JSONArray(text);return(0 until arr.length()).map{i->val o=arr.getJSONObject(i);FeedItem(o.optInt("id"),r.type,clean(o.optJSONObject("title")?.optString("rendered").orEmpty()),clean(o.optJSONObject("excerpt")?.optString("rendered").orEmpty()),o.optString("link"),r.badge)}}
    private fun filter(q:String){if(current==5){all=store.learning(q).map{FeedItem(it.id,it.type,it.title,it.excerpt,it.link,"Offline")};adapter.submit(all);return};val t=q.trim();adapter.submit(if(t.isBlank())all else all.filter{it.title.contains(t,true)||it.excerpt.contains(t,true)||it.badge.contains(t,true)})}
    private fun open(x:FeedItem){if(x.id<=0)return;startActivity(Intent(this,DetailActivity::class.java).apply{putExtra("id",x.id);putExtra("type",x.type);putExtra("title",x.title);putExtra("link",x.link)})}
    private fun clean(v:String)=Html.fromHtml(v,Html.FROM_HTML_MODE_LEGACY).toString().trim();private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onResume(){super.onResume();if(::store.isInitialized&&current==5)showOffline()};override fun onDestroy(){executor.shutdownNow();store.close();super.onDestroy()}
    inner class FeedAdapter(private val click:(FeedItem)->Unit):RecyclerView.Adapter<FeedAdapter.Holder>(){
        private val items=mutableListOf<FeedItem>();fun submit(v:List<FeedItem>){items.clear();items.addAll(v);notifyDataSetChanged()};override fun getItemCount()=items.size
        override fun onCreateViewHolder(p:ViewGroup,v:Int):Holder{val card=MaterialCardView(p.context).apply{radius=dp(16).toFloat();cardElevation=dp(1).toFloat();strokeWidth=dp(1);setStrokeColor(Color.rgb(225,232,240));setCardBackgroundColor(Color.WHITE);layoutParams=RecyclerView.LayoutParams(-1,-2).apply{bottomMargin=dp(11)}};val box=LinearLayout(p.context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(16))};val badge=TextView(p.context).apply{setTextColor(blue);textSize=12f;setTypeface(typeface,Typeface.BOLD)};val title=TextView(p.context).apply{setTextColor(navy);textSize=18f;setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(7),0,0)};val excerpt=TextView(p.context).apply{setTextColor(muted);textSize=14f;setPadding(0,dp(6),0,0);maxLines=3};val action=TextView(p.context).apply{setTextColor(blue);textSize=13f;text="Open →";setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(9),0,0)};box.addView(badge);box.addView(title);box.addView(excerpt);box.addView(action);card.addView(box);return Holder(card,badge,title,excerpt)}
        override fun onBindViewHolder(h:Holder,pos:Int){val x=items[pos];h.badge.text=x.badge;h.title.text=x.title;h.excerpt.text=x.excerpt.ifBlank{"Open to view details."};h.card.setOnClickListener{click(x)}}
        inner class Holder(val card:MaterialCardView,val badge:TextView,val title:TextView,val excerpt:TextView):RecyclerView.ViewHolder(card)
    }
}