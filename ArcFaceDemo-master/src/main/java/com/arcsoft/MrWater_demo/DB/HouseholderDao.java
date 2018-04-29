package com.arcsoft.MrWater_demo.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.arcsoft.MrWater_demo.domain.HouseholderInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mr.water on 2018/3/19.
 */

public class HouseholderDao {
    private HouseholderDBOpenHelper helper;

    public HouseholderDao(Context context)
    {
        helper = new HouseholderDBOpenHelper(context);
    }

    /**
     *
     * @param Householderid
     * 户主房号
     * @param name
     * 名字
     * @param phone
     * 电话
     * @return是否添加成功
     */
    public boolean add(String Householderid, String name, String phone)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Householderid", Householderid);
        values.put("name", name);
        values.put("phone", phone);
        long row = db.insert("info", null, values);
        db.close();
        return row == -1 ? false : true;
    }
    /**
     * 删除一条记录
     *
     * @param name
     *          户主id
     * @return  是否删除成功
     */
    public boolean delete(String name) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int count = db.delete("info", "name=?", new String[]{name});
        db.close();
        return count <= 0 ? false : true;
    }
    /**
     * 添加一条记录
     *
     * @param info
     *          Householder domain
     * @return  是否添加成功
     */
    public boolean add(HouseholderInfo info) {
        return add(String.valueOf(info.getId()), info.getName(), info.getPhone());
    }

    /**
     * 查询一条记录
     *
     * @param position
     *          数据在数据库表里面的位置
     * @return  一条记录信息
     */
    public Map<String, String> gethouseholderInfo(int position) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("info", new String[]{"householderid", "name", "phone"}, null, null, null, null, null);
        cursor.moveToPosition(position);
        String householderid = cursor.getString(0);
        String name = cursor.getString(1);
        String phone = cursor.getString(2);
        cursor.close();
        db.close();
        HashMap<String, String> result = new HashMap<>();
        result.put("householderid", householderid);
        result.put("name", name);
        result.put("phone", phone);
        return result;
    }

    /**
     * 查询数据库里面一共有多少条记录
     *
     * @return  记录的条数
     */
    public int getTotalCount() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("info", null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
    /**
     * 删除所有的数据
     */
    public void deleteAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();// 开启事务

        try {
            Cursor cursor = db.query("info", new String[]{"householderid"}, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String householderid = cursor.getString(0);
                db.delete("info", "householderid=?", new String[]{householderid});
            }
            cursor.close();
            db.setTransactionSuccessful();// 设置事务执行成功，必须要这一行代码执行，数据才会被提交
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
