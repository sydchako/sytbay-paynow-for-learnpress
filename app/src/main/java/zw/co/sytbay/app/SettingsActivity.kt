package zw.co.sytbay.app

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity:AppCompatActivity(){
    private lateinit var store:OfflineStore
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);window.statusBarColor=Color.rgb(13,33,56);store=OfflineStore(this)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,24,22,24);setBackgroundColor(Color.rgb(245,247,251))}
        root.addView(TextView(this).apply{text="Settings";textSize=28f;setTextColor(Color.rgb(13,33,56))})
        root.addView(TextView(this).apply{text="Control downloads, maths rendering, ads and local cache.";textSize=14f;setPadding(0,8,0,18)})
        root.addView(toggle("Wi-Fi only downloads",AppPrefs.wifiOnly(this)){AppPrefs.setWifiOnly(this,it)})
        root.addView(toggle("MathJax maths rendering",AppPrefs.mathJax(this)){AppPrefs.setMathJax(this,it)})
        root.addView(toggle("Show AdMob ads",AppPrefs.ads(this)){AppPrefs.setAds(this,it)})
        root.addView(Button(this).apply{text="Clear tab/list cache";setOnClickListener{store.clearFeedCache();Toast.makeText(this@SettingsActivity,"List cache cleared. Offline learning pages kept.",Toast.LENGTH_SHORT).show()}})
        root.addView(Button(this).apply{text="Clear offline learning pages";setOnClickListener{store.clearOfflinePages();Toast.makeText(this@SettingsActivity,"Offline learning pages cleared.",Toast.LENGTH_SHORT).show()}})
        root.addView(Button(this).apply{text="Back";setOnClickListener{finish()}})
        setContentView(root)
    }
    private fun toggle(label:String,checked:Boolean,changed:(Boolean)->Unit):Switch=Switch(this).apply{text=label;isChecked=checked;textSize=16f;setPadding(0,10,0,10);setOnCheckedChangeListener{_:CompoundButton,v:Boolean->changed(v)}}
    override fun onDestroy(){store.close();super.onDestroy()}
}
