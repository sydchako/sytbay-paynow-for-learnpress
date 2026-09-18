package zw.co.sytbay.app

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object OfflineImages{
    private val imgRegex=Regex("""(?i)<img\b[^>]*?\bsrc\s*=\s*["'](https://[^"']+)["'][^>]*>""")
    private val srcsetRegex=Regex("""(?i)\s+srcset\s*=\s*["'][^"']*["']""")
    fun localize(context:Context,html:String):String{
        var result=html.replace(srcsetRegex,"")
        val dir=File(context.filesDir,"offline_images").apply{mkdirs()}
        val urls=imgRegex.findAll(result).map{it.groupValues[1]}.distinct().toList()
        for(url in urls){
            try{
                val ext=when{url.substringBefore("?").endsWith(".png",true)->".png";url.substringBefore("?").endsWith(".webp",true)->".webp";url.substringBefore("?").endsWith(".gif",true)->".gif";else->".jpg"}
                val file=File(dir,sha(url)+ext)
                if(!file.exists()){
                    val c=(URL(url).openConnection() as HttpURLConnection).apply{connectTimeout=12000;readTimeout=20000;instanceFollowRedirects=true;setRequestProperty("User-Agent","Sytbay-Android/0.2")}
                    if(c.responseCode in 200..299)c.inputStream.use{input->file.outputStream().use{input.copyTo(it)}};c.disconnect()
                }
                if(file.exists()&&file.length()>0)result=result.replace(url,file.toURI().toString())
            }catch(_:Exception){}
        }
        return result
    }
    private fun sha(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)}
}