package zw.co.sytbay.app

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WebPageActivity:AppCompatActivity(){
    private lateinit var web:WebView
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);window.statusBarColor=Color.rgb(13,33,56)
        val title=intent.getStringExtra("title")?:"Sytbay";val url=intent.getStringExtra("url")?:"https://sytbay.co.zw/"
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setBackgroundColor(Color.rgb(13,33,56));setPadding(8,8,12,8)}
        val back=Button(this).apply{text="‹";textSize=28f;setTextColor(Color.WHITE);setBackgroundColor(Color.TRANSPARENT);setOnClickListener{if(::web.isInitialized&&web.canGoBack())web.goBack()else finish()}}
        val t=TextView(this).apply{text=title;textSize=19f;setTextColor(Color.WHITE)}
        bar.addView(back,LinearLayout.LayoutParams(54,54));bar.addView(t,LinearLayout.LayoutParams(0,-2,1f));root.addView(bar)
        web=WebView(this).apply{settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=WebViewClient();loadUrl(url)}
        root.addView(web,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)
    }
    override fun onDestroy(){web.destroy();super.onDestroy()}
}
