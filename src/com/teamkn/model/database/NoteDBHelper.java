package com.teamkn.model.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.teamkn.model.Note;
import com.teamkn.model.base.BaseModelDBHelper;
import com.teamkn.model.base.Constants;

public class NoteDBHelper extends BaseModelDBHelper {
  public class Kind{
    public static final String TEXT = "TEXT";
    public static final String IMAGE = "IMAGE";
  }
  
  public static int unsyn_count(){
    SQLiteDatabase db = get_read_db();
    
    Cursor cursor;
    try {
        cursor = db.query(Constants.TABLE_NOTES,
            new String[]{Constants.KEY_ID}, 
            Constants.TABLE_NOTES__IS_SYND + " = ? ", 
            new String[]{"0"} , null, null,null);

        return cursor.getCount();
    } catch (Exception e) {
      Log.e("NoteDBHelper", "unsyn_count", e);
      return 0;
    } finally {
      db.close();
    }
  }
  
  public static List<Note> all(boolean has_removed) throws Exception {
    SQLiteDatabase db = get_read_db();
    List<Note> notes = new ArrayList<Note>();
    Cursor cursor;
    try {
      if(has_removed){
        cursor = db.query(Constants.TABLE_NOTES,
            get_columns(), 
            null, null, null, null,
            Constants.KEY_ID + " DESC");
      }else{
        cursor = db.query(Constants.TABLE_NOTES,
            get_columns(), 
            Constants.TABLE_NOTES__IS_REMOVED + " = ? ", new String[]{"0"}, 
            null, null,
            Constants.KEY_ID + " DESC");
      }

      while (cursor.moveToNext()) {
        System.out.println("moveToNext");
        Note note = build_note_by_cursor(cursor);
        notes.add(note);
      }

      return notes;

    } catch (Exception e) {
      Log.e("NoteDBHelper", "all", e);
      throw e;
    } finally {
      db.close();
    }

  }

  public static boolean create_text_note(String note_content){
    String uuid = create_item_by_kind(note_content,Kind.TEXT);
    if(uuid != null){
      return true;
    }
    return false;
  }
  
  public static boolean create_image_note(String origin_image_path) {
    String uuid = create_item_by_kind("",Kind.IMAGE);
    File note_image_file = Note.note_image_file(uuid);
    
    try {
      FileUtils.copyFile(new File(origin_image_path), note_image_file);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      destroy(uuid);
      return false;
    }
  }
  
  public static void create_new_item_from_pull_server(String uuid, String content, String kind,
      Integer is_removed, long updated_at) {
      // 保存数据库信息
      ContentValues values = new ContentValues();
      values.put(Constants.TABLE_NOTES__UUID, uuid);
      values.put(Constants.TABLE_NOTES__CONTENT, content);
      values.put(Constants.TABLE_NOTES__KIND, kind);
      values.put(Constants.TABLE_NOTES__IS_REMOVED, is_removed);
      values.put(Constants.TABLE_NOTES__IS_SYND, 1);
      values.put(Constants.TABLE_NOTES__UPDATED_AT, updated_at);
      values.put(Constants.TABLE_NOTES__CREATED_AT, updated_at);
      
      create_item(values);
  }
  
  public static boolean touch_updated_at(String uuid, long seconds){
    ContentValues values = new ContentValues();
    values.put(Constants.TABLE_NOTES__UPDATED_AT, seconds);
    values.put(Constants.TABLE_NOTES__IS_SYND,1);
    
    return update_columns(uuid,values);
  }
  
  public static boolean update_from_pull(String uuid, String content, Integer is_removed,
      long updated_at) {
    ContentValues values = new ContentValues();
    values.put(Constants.TABLE_NOTES__CONTENT, content);
    values.put(Constants.TABLE_NOTES__IS_REMOVED, is_removed);
    values.put(Constants.TABLE_NOTES__UPDATED_AT, updated_at);
    values.put(Constants.TABLE_NOTES__IS_SYND,1);
    
    return update_columns(uuid,values);
  }
  
  public static boolean update(String uuid,String note_content) {
    // 保存数据库信息
    long current_seconds = System.currentTimeMillis()/1000;
    
    ContentValues values = new ContentValues();
    values.put(Constants.TABLE_NOTES__CONTENT, note_content);
    values.put(Constants.TABLE_NOTES__UPDATED_AT, current_seconds);
      
    return update_columns(uuid,values);
  }
  
  final public static Note find(String uuid){
    SQLiteDatabase db = get_read_db();
    
    try {
      Cursor cursor = db.query(
        Constants.TABLE_NOTES, get_columns(), 
        Constants.TABLE_NOTES__UUID + " = " + "'" + uuid + "'", 
        null, null, null, null
      );
  
      boolean has_result = cursor.moveToFirst();
  
      if (has_result) {
        return build_note_by_cursor(cursor);
      } else {
        return Note.NIL_NOTE;
      }
      
    } catch (Exception e) {
      Log.e("NoteDBHelper", "find", e);
      return Note.NIL_NOTE;
    } finally {
      db.close();
    }
  }

  public static boolean destroy(String uuid) {
    // 保存数据库信息
    long current_seconds = System.currentTimeMillis()/1000;
    
    ContentValues values = new ContentValues();
    values.put(Constants.TABLE_NOTES__IS_REMOVED, 1);
    values.put(Constants.TABLE_NOTES__UPDATED_AT, current_seconds);
      
    return update_columns(uuid,values);
  }
  
  private static String create_item_by_kind(String note_content,String kind){
    long current_seconds = System.currentTimeMillis()/1000;
    String uuid = UUID.randomUUID().toString();
    
    ContentValues values = new ContentValues();
    values.put(Constants.TABLE_NOTES__UUID, uuid);
    values.put(Constants.TABLE_NOTES__CONTENT, note_content);
    values.put(Constants.TABLE_NOTES__KIND, kind);
    values.put(Constants.TABLE_NOTES__IS_REMOVED, 0);
    values.put(Constants.TABLE_NOTES__IS_SYND, 0);
    values.put(Constants.TABLE_NOTES__UPDATED_AT, current_seconds);
    values.put(Constants.TABLE_NOTES__CREATED_AT, current_seconds);
    
    create_item(values);
    
    return uuid;
  }
  
  private static void create_item(ContentValues values) {
    SQLiteDatabase db = get_write_db();
    
    try {
      db.insert(Constants.TABLE_NOTES, null, values);
    } catch (Exception e) {
      Log.e("NoteDBHelper", "create_item", e);
    } finally {
      db.close();
    }
  }

  private static boolean update_columns(String uuid,ContentValues values){
    SQLiteDatabase db = get_write_db();

    try {
      // 保存数据库信息
      int row_count = db.update(Constants.TABLE_NOTES, 
          values, Constants.TABLE_NOTES__UUID + " = ? ", 
          new String[]{uuid});
      
      if(row_count != 1){return false;}
      return true;
    } catch (Exception e) {
      Log.e("NoteDBHelper", "update", e);
      return false;
    } finally {
      db.close();
    }
  }
  
  private static String[] get_columns(){
    return new String[] { 
        Constants.KEY_ID, Constants.TABLE_NOTES__UUID,
        Constants.TABLE_NOTES__CONTENT,
        Constants.TABLE_NOTES__KIND,
        Constants.TABLE_NOTES__IS_REMOVED,
        Constants.TABLE_NOTES__CREATED_AT,
        Constants.TABLE_NOTES__UPDATED_AT };
  }
  
  private static Note build_note_by_cursor(Cursor cursor) {
    int id = cursor.getInt(0);
    String uuid = cursor.getString(1);
    String content = cursor.getString(2);
    String kind = cursor.getString(3);
    int is_removed = cursor.getInt(4);
    long created_at = cursor.getLong(5);
    long updated_at = cursor.getLong(6);
    return new Note(id, uuid, content,kind, is_removed, created_at,
        updated_at);
  }

}