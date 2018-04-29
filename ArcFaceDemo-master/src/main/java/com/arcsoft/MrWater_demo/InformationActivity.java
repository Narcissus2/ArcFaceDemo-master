package com.arcsoft.MrWater_demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.MrWater_demo.DB.HouseholderDao;
import com.arcsoft.MrWater_demo.domain.HouseholderInfo;

import java.util.Map;

/**
 * Created by Mr.water on 2018/3/19.
 */

public class InformationActivity extends Activity {

    private EditText et_id;
    private EditText et_name;
    private EditText et_phone;
    private ListView lv;
    //private ArrayList<HouseholderInfo> list;
    public static HouseholderDao dao;
    public static int PeopleNum;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // 1.找到控件
        // 2.模拟操作，添加模拟数据
        // 3.去除模拟数据，添加真实数据
        // 4.将真实数据写入到数据库中
        // 5.将数据库上传到服务器
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item_deleteall:
                dao.deleteAll();
                Toast.makeText(this, "删除全部数据成功", Toast.LENGTH_SHORT).show();
                lv.setAdapter(new Myadapter());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void initView() {
        setContentView(R.layout.activity_information);
        et_id = (EditText) findViewById(R.id.et_id);
        et_name = (EditText) findViewById(R.id.et_name);
        et_phone = (EditText) findViewById(R.id.et_phone);
        lv = (ListView) findViewById(R.id.lv);
        dao = new HouseholderDao(this);
        lv.setAdapter(new Myadapter());
    }

    /**
     * 点击事件，用来添加户主信息
     * @param view
     */
    public void addHouseholder(View view){
        if(dao.getTotalCount()<=8){
        String id =  et_id.getText().toString().trim();
        String name =  et_name.getText().toString().trim();
        String phone =  et_phone.getText().toString().trim();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) ||TextUtils.isEmpty(phone)) {
            Toast.makeText(this,"数据不能为空", Toast.LENGTH_SHORT).show();
            return;
        }else{
            //保存数据到数据库，并且同步显示到界面
            HouseholderInfo info = new HouseholderInfo();
            info.setId(Integer.valueOf(id));
            info.setName(name);
            info.setPhone(phone);

            boolean result = dao.add(info);
            if(result)
            {
                Toast.makeText(this, "添加成功,现在家庭成员"+dao.getTotalCount()+"人", Toast.LENGTH_SHORT).show();
                PeopleNum = dao.getTotalCount();
                lv.setAdapter((new Myadapter()));
            }

        }}
        else{
            Toast.makeText(this, "您的家庭成员不应超过8人", Toast.LENGTH_SHORT).show();
        }

    }


    private class Myadapter extends BaseAdapter {
        @Override
        public int getCount() {
            //return list.size();
            return dao.getTotalCount();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = View.inflate(InformationActivity.this, R.layout.item_information, null);
            TextView tv_item_id = (TextView) view.findViewById(R.id.tv_item_id);
            TextView tv_item_name = (TextView) view.findViewById(R.id.tv_item_name);
            TextView tv_item_phone = (TextView) view.findViewById(R.id.tv_item_phone);
            ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
            final Map<String, String> map = dao.gethouseholderInfo(position);
            iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   //list.remove(position);
                    boolean result = dao.delete(map.get("name"));
                    if(result)
                    {
                        Toast.makeText(InformationActivity.this, "删除成功，现在家庭成员为"+dao.getTotalCount()+"人", Toast.LENGTH_SHORT).show();
                        lv.setAdapter(new Myadapter());
                    }
                }
            });
            tv_item_id.setText(map.get("householderid"));
            tv_item_name.setText(String.valueOf(map.get("name")));
            tv_item_phone.setText(map.get("phone"));

            return view;
        }
        @Override
        public HouseholderInfo getItem(int position) {
            //return list.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


    }
}
