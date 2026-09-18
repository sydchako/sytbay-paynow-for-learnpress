package zw.co.sytbay.app

import android.content.Context

object AppPrefs{
    private const val FILE="sytbay_settings"
    fun wifiOnly(context:Context)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).getBoolean("wifi_only",false)
    fun setWifiOnly(context:Context,value:Boolean)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putBoolean("wifi_only",value).apply()
    fun mathJax(context:Context)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).getBoolean("mathjax",true)
    fun setMathJax(context:Context,value:Boolean)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putBoolean("mathjax",value).apply()
    fun ads(context:Context)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).getBoolean("ads",true)
    fun setAds(context:Context,value:Boolean)=context.getSharedPreferences(FILE,Context.MODE_PRIVATE).edit().putBoolean("ads",value).apply()
}
