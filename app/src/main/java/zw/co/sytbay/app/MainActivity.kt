package zw.co.sytbay.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity:AppCompatActivity(){
    private val navy=Color.rgb(13,33,56);private val blue=Color.rgb(20,107,194);private val soft=Color.rgb(245,247,251);private val muted=Color.rgb(96,112,132)
    private val executor=Executors.newSingleThreadExecutor()
    private lateinit var titleView:TextView;private lateinit var subtitleView:TextView;private lateinit var search:SearchView;private lateinit var recycler:RecyclerView
    private lateinit var store:OfflineStore;private lateinit var drawer:DrawerLayout;private lateinit var swipe:SwipeRefreshLayout;private lateinit var adSlot:FrameLayout
    private val adapter=FeedAdapter{open(it)};private var all=listOf<FeedItem>();private var current=1

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);window.statusBarColor=navy;store=OfflineStore(this);setContentView(ui());showTab(1,false);Ads.init(this){Ads.banner(this,adSlot)}
    }

    private fun ui():DrawerLayout{
        drawer=DrawerLayout(this)
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(soft)}
        val header=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(16),dp(12),dp(10));setBackgroundColor(navy)}
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val menu=TextView(this).apply{text="☰";textSize=28f;setTextColor(Color.WHITE);setPadding(dp(8),0,dp(14),0);setOnClickListener{drawer.openDrawer(GravityCompat.START)}}
        val titles=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        titleView=TextView(this).apply{setTextColor(Color.WHITE);textSize=25f;setTypeface(typeface,Typeface.BOLD)}
        subtitleView=TextView(this).apply{setTextColor(Color.rgb(220,232,244));textSize=13f}
        titles.addView(titleView);titles.addView(subtitleView);row.addView(menu);row.addView(titles,LinearLayout.LayoutParams(0,-2,1f));header.addView(row)
        search=SearchView(this).apply{queryHint="Search this section";setIconifiedByDefault(false);setBackgroundColor(Color.WHITE)}
        search.setOnQueryTextListener(object:SearchView.OnQueryTextListener{override fun onQueryTextSubmit(q:String?)=false;override fun onQueryTextChange(q:String?):Boolean{filter(q.orEmpty());return true}})
        header.addView(search,LinearLayout.LayoutParams(-1,dp(48)));content.addView(header)

        swipe=SwipeRefreshLayout(this)
        recycler=RecyclerView(this).apply{layoutManager=LinearLayoutManager(this@MainActivity);adapter=this@MainActivity.adapter;setPadding(dp(13),dp(14),dp(13),dp(14));clipToPadding=false}
        swipe.addView(recycler);swipe.setOnRefreshListener{showTab(current,true)}
        content.addView(swipe,LinearLayout.LayoutParams(-1,0,1f))

        adSlot=FrameLayout(this);content.addView(adSlot,LinearLayout.LayoutParams(-1,-2))

        val nav=BottomNavigationView(this)
        nav.menu.add(0,1,0,"Home").setIcon(android.R.drawable.ic_menu_view)
        nav.menu.add(0,2,1,"Learn").setIcon(android.R.drawable.ic_menu_edit)
        nav.menu.add(0,3,2,"Downloads").setIcon(android.R.drawable.stat_sys_download_done)
        nav.menu.add(0,4,3,"Institutions").setIcon(android.R.drawable.ic_menu_myplaces)
        nav.menu.add(0,5,4,"Offline").setIcon(android.R.drawable.ic_menu_recent_history)
        nav.setBackgroundColor(Color.WHITE)
        nav.setOnItemSelectedListener{current=it.itemId;search.setQuery("",false);showTab(current,false);true}
        content.addView(nav)

        drawer.addView(content,DrawerLayout.LayoutParams(-1,-1))
        drawer.addView(drawerMenu(),DrawerLayout.LayoutParams(dp(310),-1,GravityCompat.START))
        return drawer
    }

    private fun drawerMenu():NavigationView{
        val n=NavigationView(this)
        val h=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(34),dp(20),dp(20));setBackgroundColor(navy)}
        h.addView(TextView(this).apply{text="Sytbay";textSize=28f;setTextColor(Color.WHITE);setTypeface(typeface,Typeface.BOLD)})
        h.addView(TextView(this).apply{text="Start where you are. Use what you have. Do what you can.";textSize=13f;setTextColor(Color.rgb(220,232,244));setPadding(0,dp(6),0,0)})
        n.addHeaderView(h)
        val m=n.menu
        m.add(0,101,0,"Articles / Home");m.add(0,102,1,"Learn");m.add(0,103,2,"Downloads");m.add(0,104,3,"Institutions");m.add(0,105,4,"Programmes");m.add(0,106,5,"Offline Library")
        m.addSubMenu("Sytbay").apply{add(0,120,0,"About Us");add(0,121,1,"Mission & Vision");add(0,122,2,"Open website")}
        m.addSubMenu("Legal").apply{add(0,130,0,"Privacy Policy");add(0,131,1,"Cookie Policy");add(0,132,2,"Terms & Conditions");add(0,133,3,"Refund & Return");add(0,134,4,"Disclaimer")}
        m.add(0,140,99,"Settings")
        n.setNavigationItemSelectedListener{
            when(it.itemId){
                101->{current=1;showTab(1,false)};102->{current=2;showTab(2,false)};103->{current=3;showTab(3,false)};104->{current=4;showTab(4,false)}
                105->openWeb("Programmes","https://sytbay.co.zw/find-a-school/");106->{current=5;showTab(5,false)}
                120->openWeb("About Us","https://sytbay.co.zw/about-us/");121->openWeb("Mission & Vision","https://sytbay.co.zw/mission-vision/")
                122->startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://sytbay.co.zw/")))
                130->openWeb("Privacy Policy","https://sytbay.co.zw/privacy-policy/");131->openWeb("Cookie Policy","https://sytbay.co.zw/cookie-policy-eu/")
                132->openWeb("Terms & Conditions","https://sytbay.co.zw/terms-conditions/");133->openWeb("Refund & Return","https://sytbay.co.zw/refund-return/")
                134->openWeb("Disclaimer","https://sytbay.co.zw/disclaimer/");140->startActivity(Intent(this,SettingsActivity::class.java))
            };drawer.closeDrawer(GravityCompat.START);true
        }
        return n
    }

    private data class Req(val url:String,val type:String,val badge:String)
    private data class Tab(val key:String,val title:String,val subtitle:String,val reqs:List<Req>)

    private fun spec(tab:Int)=when(tab){
        2->Tab("learn","Learn","Study notes and learning pages. Open once to keep offline.",listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=60&slc_resource_type=4843&orderby=modified&order=desc","slc_content","Study note")))
        3->Tab("downloads","Downloads","Downloadable Sytbay learning resources.",listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/slc_content?per_page=60&slc_resource_type=4944&orderby=modified&order=desc","slc_content","Download")))
        4->Tab("institutions","Institutions","Colleges, polytechnics and other institutions.",listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/sed_institution?per_page=100&orderby=title&order=asc","sed_institution","Institution")))
        else->Tab("home","Articles","Latest Sytbay articles and updates.",listOf(Req("https://sytbay.co.zw/wp-json/wp/v2/posts?per_page=30&orderby=date&order=desc","posts","Article"),Req("https://sytbay.co.zw/wp-json/wp/v2/sed_inst_update?per_page=10&orderby=date&order=desc","sed_inst_update","Institution update")))
    }

    private fun showTab(tab:Int,forceRefresh:Boolean){
        current=tab
        if(tab==5){showOffline();return}
        val s=spec(tab);heading(s.title,s.subtitle)
        val cached=store.getFeed(s.key)
        if(cached.isNotEmpty()){all=cached;adapter.submit(cached);swipe.isRefreshing=false}
        if(forceRefresh||cached.isEmpty())refresh(s)
    }

    private fun refresh(s:Tab){
        swipe.isRefreshing=true
        executor.execute{
            try{
                val out=mutableListOf<FeedItem>();s.reqs.forEach{out.addAll(fetch(it))}
                store.saveFeed(s.key,out)
                runOnUiThread{all=out;adapter.submit(out);swipe.isRefreshing=false}
            }catch(_:Exception){
                runOnUiThread{swipe.isRefreshing=false;if(store.getFeed(s.key).isEmpty())adapter.submit(listOf(FeedItem(0,"","Could not refresh","Pull down to try again. Saved offline pages remain available.","","Offline")))}
            }
        }
    }

    private fun showOffline(){
        val n=store.countLearning();heading("Offline Library",n.toString()+" learning "+(if(n==1)"page" else "pages")+" saved on this phone.")
        all=store.learning().map{FeedItem(it.id,it.type,it.title,it.excerpt,it.link,"Offline")};adapter.submit(all);swipe.isRefreshing=false
    }

    private fun fetch(r:Req):List<FeedItem>{
        val c=(URL(r.url).openConnection() as HttpURLConnection).apply{connectTimeout=15000;readTimeout=30000;setRequestProperty("Accept","application/json");setRequestProperty("User-Agent","Sytbay-Android/0.3")}
        val text=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();val arr=JSONArray(text)
        return(0 until arr.length()).map{i->val o=arr.getJSONObject(i);FeedItem(o.optInt("id"),r.type,clean(o.optJSONObject("title")?.optString("rendered").orEmpty()),clean(o.optJSONObject("excerpt")?.optString("rendered").orEmpty()),o.optString("link"),r.badge)}
    }

    private fun filter(q:String){
        if(current==5){all=store.learning(q).map{FeedItem(it.id,it.type,it.title,it.excerpt,it.link,"Offline")};adapter.submit(all);return}
        val t=q.trim();adapter.submit(if(t.isBlank())all else all.filter{it.title.contains(t,true)||it.excerpt.contains(t,true)||it.badge.contains(t,true)})
    }
    private fun open(x:FeedItem){if(x.id<=0)return;startActivity(Intent(this,DetailActivity::class.java).apply{putExtra("id",x.id);putExtra("type",x.type);putExtra("title",x.title);putExtra("link",x.link)})}
    private fun openWeb(title:String,url:String)=startActivity(Intent(this,WebPageActivity::class.java).apply{putExtra("title",title);putExtra("url",url)})
    private fun heading(t:String,s:String){titleView.text=t;subtitleView.text=s}
    private fun clean(v:String)=Html.fromHtml(v,Html.FROM_HTML_MODE_LEGACY).toString().trim()
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onResume(){super.onResume();if(::store.isInitialized&&current==5)showOffline()}
    override fun onDestroy(){executor.shutdownNow();store.close();super.onDestroy()}

    inner class FeedAdapter(private val click:(FeedItem)->Unit):RecyclerView.Adapter<FeedAdapter.Holder>(){
        private val items=mutableListOf<FeedItem>()
        fun submit(v:List<FeedItem>){items.clear();items.addAll(v);notifyDataSetChanged()}
        override fun getItemCount()=items.size
        override fun onCreateViewHolder(p:ViewGroup,v:Int):Holder{
            val card=MaterialCardView(p.context).apply{radius=dp(16).toFloat();cardElevation=dp(1).toFloat();strokeWidth=dp(1);setStrokeColor(Color.rgb(225,232,240));setCardBackgroundColor(Color.WHITE);layoutParams=RecyclerView.LayoutParams(-1,-2).apply{bottomMargin=dp(11)}}
            val box=LinearLayout(p.context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(16))}
            val badge=TextView(p.context).apply{setTextColor(blue);textSize=12f;setTypeface(typeface,Typeface.BOLD)}
            val title=TextView(p.context).apply{setTextColor(navy);textSize=18f;setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(7),0,0)}
            val excerpt=TextView(p.context).apply{setTextColor(muted);textSize=14f;setPadding(0,dp(6),0,0);maxLines=3}
            val action=TextView(p.context).apply{setTextColor(blue);textSize=13f;text="Open →";setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(9),0,0)}
            box.addView(badge);box.addView(title);box.addView(excerpt);box.addView(action);card.addView(box);return Holder(card,badge,title,excerpt)
        }
        override fun onBindViewHolder(h:Holder,pos:Int){val x=items[pos];h.badge.text=x.badge;h.title.text=x.title;h.excerpt.text=x.excerpt.ifBlank{"Open to view details."};h.card.setOnClickListener{click(x)}}
        inner class Holder(val card:MaterialCardView,val badge:TextView,val title:TextView,val excerpt:TextView):RecyclerView.ViewHolder(card)
    }
}
