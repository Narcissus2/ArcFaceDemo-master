package com.arcsoft.MrWater_demo.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mr.water on 2018/3/19.
 */

public class HouseholderDBOpenHelper extends SQLiteOpenHelper {
    public HouseholderDBOpenHelper(Context context) {
        super(context, "householder.db",null,1);
    }

    // 数据库第一次被创建调用的方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table info (_id integer primary key autoincrement, householderid varchar(20), name varchar(20), phone varchar(20));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
