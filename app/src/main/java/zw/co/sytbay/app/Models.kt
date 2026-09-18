package zw.co.sytbay.app

data class FeedItem(val id:Int,val type:String,val title:String,val excerpt:String,val link:String,val badge:String)
data class CachedPage(val id:Int,val type:String,val title:String,val excerpt:String,val link:String,val html:String,val modified:String,val savedAt:Long)