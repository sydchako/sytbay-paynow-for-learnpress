package zw.co.sytbay.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
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
    private lateinit var download:Button;private lateinit var adSlot:FrameLayout
    private lateinit var store:OfflineStore;private val executor=Executors.newSingleThreadExecutor()
    private var canonical="https://sytbay.co.zw/";private var id=0;private var type="slc_content";private var currentPage:CachedPage?=null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);window.statusBarColor=navy
        id=intent.getIntExtra("id",0);type=intent.getStringExtra("type")?:"slc_content";canonical=intent.getStringExtra("link")?:canonical
        store=OfflineStore(this);setContentView(ui())
        val cached=store.get(type,id)
        if(cached!=null){canonical=cached.link.ifBlank{canonical};currentPage=cached;render(cached,true)}else refresh()
        Ads.init(this){Ads.banner(this,adSlot)}
    }

    private fun ui():LinearLayout{
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(245,247,251))}
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(10),dp(8),dp(10));setBackgroundColor(navy)}
        val back=Button(this).apply{text="‹";textSize=28f;setTextColor(Color.WHITE);setBackgroundColor(Color.TRANSPARENT);setOnClickListener{finish()}}
        titleView=TextView(this).apply{setTextColor(Color.WHITE);textSize=18f;maxLines=2}
        val refresh=Button(this).apply{text="↻";textSize=22f;setTextColor(Color.WHITE);setBackgroundColor(Color.TRANSPARENT);setOnClickListener{refresh()}}
        top.addView(back,LinearLayout.LayoutParams(dp(50),dp(50)));top.addView(titleView,LinearLayout.LayoutParams(0,-2,1f));top.addView(refresh,LinearLayout.LayoutParams(dp(50),dp(50)));root.addView(top)
        statusView=TextView(this).apply{setPadding(dp(16),dp(8),dp(16),dp(8));setTextColor(Color.rgb(75,88,105));textSize=12f};root.addView(statusView)
        progress=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{isIndeterminate=true;visibility=View.GONE};root.addView(progress,LinearLayout.LayoutParams(-1,dp(3)))
        web=WebView(this).apply{
            setBackgroundColor(Color.WHITE);settings.javaScriptEnabled=AppPrefs.mathJax(this@DetailActivity);settings.allowFileAccess=true;settings.allowContentAccess=false;settings.domStorageEnabled=false
            settings.cacheMode=WebSettings.LOAD_CACHE_ELSE_NETWORK
            webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view:WebView?,request:WebResourceRequest?):Boolean{
                    val u=request?.url?.toString().orEmpty()
                    return if(DownloadHelper.isDownload(u)){DownloadHelper.download(this@DetailActivity,u,titleView.text.toString());true}else if(u.startsWith("https://sytbay.co.zw/")){startActivity(Intent(this@DetailActivity,WebPageActivity::class.java).apply{putExtra("title","Sytbay");putExtra("url",u)});true}else false
                }
            }
        }
        root.addView(web,LinearLayout.LayoutParams(-1,0,1f))
        download=Button(this).apply{text="Download attachment";setTextColor(navy);visibility=View.GONE;setOnClickListener{currentPage?.let{p->DownloadHelper.firstAttachment(p.html)?.let{u->DownloadHelper.download(this@DetailActivity,u,p.title)}}}}
        root.addView(download)
        val website=Button(this).apply{text="Open original on Sytbay";setTextColor(navy);setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(canonical)))}}
        root.addView(website)
        adSlot=FrameLayout(this);root.addView(adSlot)
        return root
    }

    private fun refresh(){
        if(id<=0)return;progress.visibility=View.VISIBLE
        executor.execute{
            try{
                val endpoint="https://sytbay.co.zw/wp-json/wp/v2/$type/$id"
                val c=(URL(endpoint).openConnection() as HttpURLConnection).apply{connectTimeout=15000;readTimeout=30000;setRequestProperty("Accept","application/json");setRequestProperty("User-Agent","Sytbay-Android/0.3")}
                val body=c.inputStream.bufferedReader().use{it.readText()};c.disconnect();val o=JSONObject(body)
                var p=CachedPage(id,type,clean(o.optJSONObject("title")?.optString("rendered").orEmpty()),clean(o.optJSONObject("excerpt")?.optString("rendered").orEmpty()),o.optString("link",canonical),o.optJSONObject("content")?.optString("rendered").orEmpty(),o.optString("modified",""),System.currentTimeMillis())
                canonical=p.link.ifBlank{canonical};store.save(p)
                if(type=="slc_content"){val localized=OfflineImages.localize(this,p.html);if(localized!=p.html){p=p.copy(html=localized,savedAt=System.currentTimeMillis());store.save(p)}}
                currentPage=p;runOnUiThread{render(p,false)}
            }catch(_:Exception){runOnUiThread{progress.visibility=View.GONE;if(currentPage==null){titleView.text=intent.getStringExtra("title")?:"Sytbay";statusView.text="Offline copy not available yet";web.loadDataWithBaseURL(null,errorHtml(),"text/html","UTF-8",null)}else statusView.text="Offline copy • refresh unavailable"}}
        }
    }

    private fun render(p:CachedPage,fromCache:Boolean){
        progress.visibility=View.GONE;currentPage=p;titleView.text=p.title.ifBlank{"Sytbay"}
        val saved=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(p.savedAt))
        statusView.text=if(fromCache)"Cached • $saved • tap ↻ to check for updates" else if(type=="slc_content")"Saved offline automatically • $saved" else "Updated • $saved"
        download.visibility=if(DownloadHelper.firstAttachment(p.html)!=null)View.VISIBLE else View.GONE
        val body=p.html.ifBlank{"<p>"+escape(p.excerpt)+"</p>"}
        web.loadDataWithBaseURL("https://sytbay.co.zw/",wrap(p.title,body),"text/html","UTF-8",null)
    }

    private fun wrap(title:String,body:String):String{
        val math=if(AppPrefs.mathJax(this))"""<script>window.MathJax={tex:{inlineMath:[['$','$'],['\\(','\\)']],displayMath:[['$$','$$'],['\\[','\\]']]},svg:{fontCache:'global'}};</script><script async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-svg.js"></script>""" else ""
        return "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"+math+"<style>body{font-family:Arial,sans-serif;color:#172033;line-height:1.65;margin:0;padding:22px;background:#fff}h1,h2,h3{color:#0d2138;line-height:1.25}h1{font-size:28px}h2{font-size:23px;margin-top:28px}img{max-width:100%;height:auto;border-radius:10px}table{display:block;overflow-x:auto;border-collapse:collapse}th,td{padding:9px;border:1px solid #e1e8f0}blockquote{border-left:4px solid #ffbd10;margin-left:0;padding-left:14px}a{color:#146bc2}pre,code{white-space:pre-wrap;word-break:break-word}.MathJax{overflow-x:auto;overflow-y:hidden}</style></head><body><h1>"+escape(title)+"</h1>"+body+"</body></html>"
    }
    private fun errorHtml()="""<html><body style="font-family:Arial;padding:24px"><h2>No offline copy yet</h2><p>Connect once and open this learning page. Sytbay will save it on this phone automatically.</p></body></html>"""
    private fun clean(v:String)=Html.fromHtml(v,Html.FROM_HTML_MODE_LEGACY).toString().trim()
    private fun escape(v:String)=v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    override fun onDestroy(){web.destroy();executor.shutdownNow();store.close();super.onDestroy()}
}
