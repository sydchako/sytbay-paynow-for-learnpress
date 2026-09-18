package zw.co.sytbay.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OfflineStore(context:Context):SQLiteOpenHelper(context,"sytbay_offline.db",null,2){
    override fun onCreate(db:SQLiteDatabase){
        db.execSQL("CREATE TABLE cached_pages(post_type TEXT NOT NULL,remote_id INTEGER NOT NULL,title TEXT NOT NULL,excerpt TEXT NOT NULL DEFAULT '',link TEXT NOT NULL DEFAULT '',html TEXT NOT NULL DEFAULT '',modified TEXT NOT NULL DEFAULT '',saved_at INTEGER NOT NULL,PRIMARY KEY(post_type,remote_id))")
        db.execSQL("CREATE INDEX idx_cached_type_saved ON cached_pages(post_type,saved_at DESC)")
        db.execSQL("CREATE TABLE feed_cache(feed_key TEXT NOT NULL,position INTEGER NOT NULL,remote_id INTEGER NOT NULL,post_type TEXT NOT NULL,title TEXT NOT NULL,excerpt TEXT NOT NULL DEFAULT '',link TEXT NOT NULL DEFAULT '',badge TEXT NOT NULL DEFAULT '',saved_at INTEGER NOT NULL,PRIMARY KEY(feed_key,position))")
        db.execSQL("CREATE INDEX idx_feed_key ON feed_cache(feed_key)")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){
        if(oldVersion<2){
            db.execSQL("CREATE TABLE IF NOT EXISTS feed_cache(feed_key TEXT NOT NULL,position INTEGER NOT NULL,remote_id INTEGER NOT NULL,post_type TEXT NOT NULL,title TEXT NOT NULL,excerpt TEXT NOT NULL DEFAULT '',link TEXT NOT NULL DEFAULT '',badge TEXT NOT NULL DEFAULT '',saved_at INTEGER NOT NULL,PRIMARY KEY(feed_key,position))")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_feed_key ON feed_cache(feed_key)")
        }
    }

    fun save(p:CachedPage){
        val v=ContentValues().apply{put("post_type",p.type);put("remote_id",p.id);put("title",p.title);put("excerpt",p.excerpt);put("link",p.link);put("html",p.html);put("modified",p.modified);put("saved_at",p.savedAt)}
        writableDatabase.insertWithOnConflict("cached_pages",null,v,SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun get(type:String,id:Int):CachedPage?{
        readableDatabase.query("cached_pages",null,"post_type=? AND remote_id=?",arrayOf(type,id.toString()),null,null,null,"1").use{c->
            if(!c.moveToFirst())return null
            return fromPage(c)
        }
    }

    fun learning(query:String=""):List<CachedPage>{
        val q=query.trim()
        val selection=if(q.isBlank())"post_type=?" else "post_type=? AND (title LIKE ? OR excerpt LIKE ?)"
        val args=if(q.isBlank())arrayOf("slc_content") else arrayOf("slc_content","%$q%","%$q%")
        val out=mutableListOf<CachedPage>()
        readableDatabase.query("cached_pages",null,selection,args,null,null,"saved_at DESC").use{c->while(c.moveToNext())out.add(fromPage(c))}
        return out
    }

    fun countLearning():Int{
        readableDatabase.rawQuery("SELECT COUNT(*) FROM cached_pages WHERE post_type='slc_content'",null).use{c->return if(c.moveToFirst())c.getInt(0) else 0}
    }

    fun saveFeed(key:String,items:List<FeedItem>){
        val db=writableDatabase
        db.beginTransaction()
        try{
            db.delete("feed_cache","feed_key=?",arrayOf(key))
            val now=System.currentTimeMillis()
            items.forEachIndexed{position,item->
                val v=ContentValues().apply{
                    put("feed_key",key);put("position",position);put("remote_id",item.id);put("post_type",item.type)
                    put("title",item.title);put("excerpt",item.excerpt);put("link",item.link);put("badge",item.badge);put("saved_at",now)
                }
                db.insert("feed_cache",null,v)
            }
            db.setTransactionSuccessful()
        }finally{db.endTransaction()}
    }

    fun getFeed(key:String):List<FeedItem>{
        val out=mutableListOf<FeedItem>()
        readableDatabase.query("feed_cache",null,"feed_key=?",arrayOf(key),null,null,"position ASC").use{c->
            while(c.moveToNext())out.add(FeedItem(
                c.getInt(c.getColumnIndexOrThrow("remote_id")),
                c.getString(c.getColumnIndexOrThrow("post_type")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("excerpt")),
                c.getString(c.getColumnIndexOrThrow("link")),
                c.getString(c.getColumnIndexOrThrow("badge"))
            ))
        }
        return out
    }

    fun clearFeedCache(){writableDatabase.delete("feed_cache",null,null)}
    fun clearOfflinePages(){writableDatabase.delete("cached_pages",null,null)}

    private fun fromPage(c:android.database.Cursor)=CachedPage(
        c.getInt(c.getColumnIndexOrThrow("remote_id")),c.getString(c.getColumnIndexOrThrow("post_type")),
        c.getString(c.getColumnIndexOrThrow("title")),c.getString(c.getColumnIndexOrThrow("excerpt")),
        c.getString(c.getColumnIndexOrThrow("link")),c.getString(c.getColumnIndexOrThrow("html")),
        c.getString(c.getColumnIndexOrThrow("modified")),c.getLong(c.getColumnIndexOrThrow("saved_at")))
}
