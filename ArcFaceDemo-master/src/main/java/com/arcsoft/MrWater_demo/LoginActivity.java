package com.arcsoft.MrWater_demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Mr.water on 2018/3/17
 */
public class LoginActivity extends Activity {
    private EditText et_user;
    private EditText et_password;
    private CheckBox cb_remember;
    public static boolean is_login = false;
    /**
     * 1.定义一个共享参数(存放数据方便的api)
     */
    private SharedPreferences sp;
    private Button bt_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2.通过上下文得到一个共享参数的实例对象
        sp = this.getSharedPreferences("config", this.MODE_PRIVATE);

        et_user = (EditText) findViewById(R.id.et_user);
        et_password = (EditText) findViewById(R.id.et_password);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);
        restoreInfo();
    }

    private void restoreInfo() {
        String user = sp.getString("user", "");
        String password = sp.getString("password", "");
        et_user.setText(user);
        et_password.setText(password);
    }

    /**
     * 登录按钮的点击事件
     *
     * @param view
     */
    public void login(View view) {

        final String user = et_user.getText().toString().trim();
        final String password = et_password.getText().toString().trim();

        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // 判断是否需要记录用户名和密码
            if (cb_remember.isChecked()) {
                // 被选中状态，需要记录用户名和密码
                // 3.将数据保存到sp文件中
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("user", user);
                editor.putString("password", password);
                editor.commit();// 提交数据，类似关闭流，事务
            }else{
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();

            }
            bt_login.setEnabled(true);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        //这里填写服务器地址
                        String path = "http://192.168.0.105:8080/WebServer/servlet/LoginServlet";
                        URL url = new URL(path);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setConnectTimeout(1500);

                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// 设置发送的数据为表单类型，会被添加到http body当中

                        String data = "username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(password);

                        conn.setRequestProperty("Content-Length", String.valueOf(data.length()));

                        // post的请求是把数据以流的方式写给了服务器
                        // 指定请求输出模式
                        conn.setDoOutput(true);// 运行当前的应用程序给服务器写数据
                        conn.getOutputStream().write(data.getBytes());
                        if("a".equals(user)&&"1".equals(password))
                        {
                            is_login = true;
                        }
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            System.out.println("result1"+result);
                            if("登录成功".equals(result))
                            {
                                is_login = true;
                            }
                            showToastInAnyThread(result);

                        } else {
                            showToastInAnyThread("请求失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToastInAnyThread("请求失败");
                    }
                }
            }.start();
        }
    }

    private void showToastInAnyThread(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
