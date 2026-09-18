package zw.co.sytbay.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.Executors

class DetailActivity:AppCompatActivity(){
    private val navy=Color.rgb(13,33,56)
    private lateinit var titleView:TextView;private lateinit var statusView:TextView;private lateinit var web:WebView;private lateinit var progress:ProgressBar
    private lateinit var store:OfflineStore;private val executor=Executors.newSingleThreadExecutor()
    private var canonical="https://sytbay.co.zw/";private var id=0;private var type="slc_content"
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);window.statusBarColor=navy
        id=intent.getIntExtra("id",0);type=intent.getStringExtra("type")?:"slc_content";canonical=intent.getStringExtra("link")?:canonical
        store=OfflineStore(this);setContentView(ui())
        val cached=store.get(type,id);if(cached!=null){canonical=cached.link.ifBlank{canonical};render(cached,true)}
        refresh(cached==null)
    }
    private fun ui():LinearLayout{
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(245,247,251))}
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),dp(12),dp(14),dp(12));setBackgroundColor(navy)}
        val back=Button(this).apply{text="‹";textSize=28f;setTextColor(Color.WHITE);setBackgroundColor(Color.TRANSPARENT);setOnClickListener{finish()}}
        titleView=TextView(this).apply{setTextColor(Color.WHITE);textSize=19f;maxLines=2}
        top.addView(back,LinearLayout.LayoutParams(dp(54),dp(54)));top.addView(titleView,LinearLayout.LayoutParams(0,-2,1f));root.addView(top)
        statusView=TextView(this).apply{setPadding(dp(16),dp(9),dp(16),dp(9));setTextColor(Color.rgb(75,88,105));textSize=12f};root.addView(statusView)
        progress=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{isIndeterminate=true};root.addView(progress,LinearLayout.LayoutParams(-1,dp(3)))
        web=WebView(this).apply{setBackgroundColor(Color.WHITE);settings.javaScriptEnabled=false;settings.allowFileAccess=true;settings.allowContentAccess=false;settings.domStorageEnabled=false}
        root.addView(web,LinearLayout.LayoutParams(-1,0,1f))
        val website=Button(this).apply{text="Open original on Sytbay";setTextColor(navy);setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(canonical)))}}
        root.addView(website);return root
    }
    private fun refresh(showEmptyError:Boolean){
        if(id<=0)return;progress.visibility=android.view.View.VISIBLE
        executor.execute{
            try{
                val endpoint="https://sytbay.co.zw/wp-json/wp/v2/$type/$id"
                val c=(URL(endpoint).openConnection() as HttpURLConnection).apply{connectTimeout=15000;readTimeout=30000;setRequestProperty("Accept","application/json");setRequestProperty("User-Agent","Sytbay-Android/0.2")}
                val body=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();val o=JSONObject(body)
                var p=CachedPage(id,type,clean(o.optJSONObject("title")?.optString("rendered").orEmpty()),clean(o.optJSONObject("excerpt")?.optString("rendered").orEmpty()),o.optString("link",canonical),o.optJSONObject("content")?.optString("rendered").orEmpty(),o.optString("modified",""),System.currentTimeMillis())
                canonical=p.link.ifBlank{canonical};store.save(p);runOnUiThread{render(p,false)}
                if(type=="slc_content"){val localized=OfflineImages.localize(this,p.html);if(localized!=p.html){p=p.copy(html=localized,savedAt=System.currentTimeMillis());store.save(p);runOnUiThread{render(p,false)}}}
            }catch(_:Exception){
                runOnUiThread{progress.visibility=android.view.View.GONE;if(showEmptyError&&store.get(type,id)==null){titleView.text=intent.getStringExtra("title")?:"Sytbay";statusView.text="Offline copy not available yet";web.loadDataWithBaseURL(null,errorHtml(),"text/html","UTF-8",null)}else statusView.text="Offline copy • refresh unavailable"}
            }
        }
    }
    private fun render(p:CachedPage,fromCache:Boolean){
        progress.visibility=android.view.View.GONE;titleView.text=p.title.ifBlank{"Sytbay"}
        val saved=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(p.savedAt))
        statusView.text=if(fromCache)"Saved offline • $saved" else if(type=="slc_content")"Saved offline automatically • $saved" else "Updated • $saved"
        val body=p.html.ifBlank{"<p>"+escape(p.excerpt)+"</p>"};web.loadDataWithBaseURL("file://"+filesDir.absolutePath+"/",wrap(p.title,body),"text/html","UTF-8",null)
    }
    private fun wrap(title:String,body:String)="""<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><style>body{font-family:Arial,sans-serif;color:#172033;line-height:1.65;margin:0;padding:22px;background:#fff}h1,h2,h3{color:#0d2138;line-height:1.25}h1{font-size:28px}h2{font-size:23px;margin-top:28px}img{max-width:100%;height:auto;border-radius:10px}table{display:block;overflow-x:auto;border-collapse:collapse}th,td{padding:9px;border:1px solid #e1e8f0}blockquote{border-left:4px solid #ffbd10;margin-left:0;padding-left:14px}a{color:#146bc2}pre,code{white-space:pre-wrap;word-break:break-word}</style></head><body><h1>"""+escape(title)+"</h1>"+body+"</body></html>"
    private fun errorHtml()="""<html><body style="font-family:Arial;padding:24px"><h2>No offline copy yet</h2><p>Connect to the internet and open this learning page once. Sytbay will then save the page and its images on this phone automatically.</p></body></html>"""
    private fun clean(v:String)=Html.fromHtml(v,Html.FROM_HTML_MODE_LEGACY).toString().trim()
    private fun escape(v:String)=v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onDestroy(){web.destroy();executor.shutdownNow();store.close();super.onDestroy()}
}