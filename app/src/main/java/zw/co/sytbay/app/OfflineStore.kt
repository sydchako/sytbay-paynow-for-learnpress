package zw.co.sytbay.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OfflineStore(context:Context):SQLiteOpenHelper(context,"sytbay_offline.db",null,1){
    override fun onCreate(db:SQLiteDatabase){
        db.execSQL("CREATE TABLE cached_pages(post_type TEXT NOT NULL,remote_id INTEGER NOT NULL,title TEXT NOT NULL,excerpt TEXT NOT NULL DEFAULT '',link TEXT NOT NULL DEFAULT '',html TEXT NOT NULL DEFAULT '',modified TEXT NOT NULL DEFAULT '',saved_at INTEGER NOT NULL,PRIMARY KEY(post_type,remote_id))")
        db.execSQL("CREATE INDEX idx_cached_type_saved ON cached_pages(post_type,saved_at DESC)")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){}
    fun save(p:CachedPage){
        val v=ContentValues().apply{put("post_type",p.type);put("remote_id",p.id);put("title",p.title);put("excerpt",p.excerpt);put("link",p.link);put("html",p.html);put("modified",p.modified);put("saved_at",p.savedAt)}
        writableDatabase.insertWithOnConflict("cached_pages",null,v,SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun get(type:String,id:Int):CachedPage?{
        readableDatabase.query("cached_pages",null,"post_type=? AND remote_id=?",arrayOf(type,id.toString()),null,null,null,"1").use{c->
            if(!c.moveToFirst())return null
            return from(c)
        }
    }
    fun learning(query:String=""):List<CachedPage>{
        val q=query.trim()
        val selection=if(q.isBlank())"post_type=?" else "post_type=? AND (title LIKE ? OR excerpt LIKE ?)"
        val args=if(q.isBlank())arrayOf("slc_content") else arrayOf("slc_content","%$q%","%$q%")
        val out=mutableListOf<CachedPage>()
        readableDatabase.query("cached_pages",null,selection,args,null,null,"saved_at DESC").use{c->while(c.moveToNext())out.add(from(c))}
        return out
    }
    fun countLearning():Int{
        readableDatabase.rawQuery("SELECT COUNT(*) FROM cached_pages WHERE post_type='slc_content'",null).use{c->return if(c.moveToFirst())c.getInt(0) else 0}
    }
    private fun from(c:android.database.Cursor)=CachedPage(
        c.getInt(c.getColumnIndexOrThrow("remote_id")),c.getString(c.getColumnIndexOrThrow("post_type")),
        c.getString(c.getColumnIndexOrThrow("title")),c.getString(c.getColumnIndexOrThrow("excerpt")),
        c.getString(c.getColumnIndexOrThrow("link")),c.getString(c.getColumnIndexOrThrow("html")),
        c.getString(c.getColumnIndexOrThrow("modified")),c.getLong(c.getColumnIndexOrThrow("saved_at")))
}