package zw.co.sytbay.app

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object DownloadHelper{
    private val ext=Regex("""(?i)\.(pdf|docx?|xlsx?|pptx?|zip|epub|csv|txt)(\?.*)?$""")
    fun isDownload(url:String)=url.startsWith("https://")&&ext.containsMatchIn(url)

    fun download(context:Context,url:String,title:String){
        if(AppPrefs.wifiOnly(context)&&!isWifi(context)){
            Toast.makeText(context,"Wi-Fi-only downloads are enabled in Settings.",Toast.LENGTH_LONG).show();return
        }
        try{
            val name=fileName(url,title)
            val req=DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setDescription("Downloading from Sytbay")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(!AppPrefs.wifiOnly(context))
                .setDestinationInExternalFilesDir(context,Environment.DIRECTORY_DOWNLOADS,name)
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            Toast.makeText(context,"Download started: $name",Toast.LENGTH_SHORT).show()
        }catch(_:Exception){Toast.makeText(context,"Could not start download.",Toast.LENGTH_LONG).show()}
    }

    fun firstAttachment(html:String):String?{
        val r=Regex("""(?i)href\s*=\s*["'](https://[^"']+\.(?:pdf|docx?|xlsx?|pptx?|zip|epub|csv|txt)(?:\?[^"']*)?)["']""")
        return r.find(html)?.groupValues?.getOrNull(1)
    }

    private fun fileName(url:String,title:String):String{
        val raw=Uri.parse(url).lastPathSegment?.substringBefore("?").orEmpty()
        if(raw.contains(".")&&raw.length<120)return raw.replace(Regex("[^A-Za-z0-9._ -]"),"_")
        return title.take(70).replace(Regex("[^A-Za-z0-9._ -]"),"_")+".pdf"
    }
    private fun isWifi(context:Context):Boolean{
        val cm=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n=cm.activeNetwork?:return false
        return cm.getNetworkCapabilities(n)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true
    }
}
