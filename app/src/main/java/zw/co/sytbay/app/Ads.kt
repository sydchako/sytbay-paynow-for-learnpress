package zw.co.sytbay.app

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object Ads{
    private var initialized=false
    fun init(activity:Activity,onReady:()->Unit){
        if(!AppPrefs.ads(activity)){onReady();return}
        val consentInformation:ConsentInformation=UserMessagingPlatform.getConsentInformation(activity)
        val params=ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(activity,params,{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity){
                if(consentInformation.canRequestAds())initialize(activity,onReady) else onReady()
            }
        },{
            if(consentInformation.canRequestAds())initialize(activity,onReady) else onReady()
        })
    }
    private fun initialize(activity:Activity,onReady:()->Unit){
        if(initialized){onReady();return}
        MobileAds.initialize(activity){initialized=true;onReady()}
    }
    fun banner(activity:Activity,container:ViewGroup){
        if(!AppPrefs.ads(activity)){container.visibility=View.GONE;return}
        val ad=AdView(activity).apply{
            adUnitId=BuildConfig.ADMOB_BANNER_ID
            setAdSize(AdSize.BANNER)
            loadAd(AdRequest.Builder().build())
        }
        container.removeAllViews();container.addView(ad)
    }
}
