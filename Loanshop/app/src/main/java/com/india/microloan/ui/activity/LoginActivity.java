package com.india.microloan.ui.activity;



import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.india.microloan.R;
import com.india.microloan.base.BaseActivity;
import com.india.microloan.constant.Constants;
import com.india.microloan.net.HttpParams;
import com.india.microloan.net.NetUtil;
import com.india.microloan.utils.LogUtils;
import com.india.microloan.utils.MyToast;
import com.india.microloan.utils.UserUtil;
import com.india.microloan.utils.appUtil;


import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.android.volley.VolleyLog.TAG;

public class LoginActivity extends BaseActivity {
    TextView tvOTP,tvUnClickOTP,tvAgree;
    Button btnApply;
    EditText editPhone,editOTP;
    String mPhoneNumber,mOTP,getmOTP;
    RelativeLayout loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUtil.setTranslucentStatus(this);


    }

    @Override
    protected int initContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        tvOTP=findViewById(R.id.tv_login_otp);
        tvUnClickOTP=findViewById(R.id.tv_login_otp2);
        btnApply=findViewById(R.id.login_apply_btn);
        editPhone=findViewById(R.id.edt_login_phone);
        editOTP=findViewById(R.id.edt_login_otp);
        loading=findViewById(R.id.loaddata);
        tvAgree=findViewById(R.id.tv_user_agree);
        tvUnClickOTP.setVisibility(View.GONE);
        tvOTP.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        tvOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareSendOTP();
            }
        });
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareLogin();
            }
        });
        tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,WebActivity.class);
                intent.putExtra("url","https://www.jq62.com/useragreement/user.html");
                intent.putExtra("title","User Login Protocol");
                startActivity(intent);
            }
        });
    }



    public void prepareSendOTP(){
        mPhoneNumber=editPhone.getText().toString();
        if(mPhoneNumber.isEmpty()){
            Toast.makeText(LoginActivity.this,"Please input the phone number,cannot be empty.",Toast.LENGTH_SHORT).show();
        }else if(mPhoneNumber.length()!=10){
            Toast.makeText(LoginActivity.this,"Please enter the correct phone number.",Toast.LENGTH_SHORT).show();
        }else{
            loading.setVisibility(View.VISIBLE);
            getOTP();
        }
    }

    public void getOTP(){
        HttpParams param = new HttpParams();
        param.put("phone", mPhoneNumber);
        String url = Constants.GET_OTP+param.toGetParams();

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(Constants.DOMAIN, "onFailure: ");
                changeUI(2);
                MyToast.show(LoginActivity.this,"Network exception, please try again later.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        int yes = object.getInt("status");
                        if(yes==200){
                            getmOTP=object.getString("data");
                            changeUI(1);
                        }else{
                            changeUI(2);
                            MyToast.show(LoginActivity.this,object.getString("msg"));
                        }
                    }catch (Exception e){
                        changeUI(2);
                        MyToast.show(LoginActivity.this,"Network exception, please try again later.");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void prepareLogin(){
        mOTP=editOTP.getText().toString();
        if(mOTP.isEmpty()){
            Toast.makeText(LoginActivity.this,"Verification code cannot be empty.",Toast.LENGTH_SHORT).show();
        }else if(!mOTP.equals(tvUnClickOTP.getText().toString())){
            Toast.makeText(LoginActivity.this,"Please enter the correct verification code.",Toast.LENGTH_SHORT).show();
        }else{
            gotoLogin();
            finish();
        }
    }

    public void gotoLogin(){
        HttpParams param = new HttpParams();
        param.put("phone", mPhoneNumber);
        param.put("verCode",mOTP);
        String url = Constants.LOGIN+param.toGetParams();

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);


        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(Constants.DOMAIN, "onFailure: ");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.protocol() + " " +response.code() + " " + response.message());
                String content=response.body().string();
                if (response != null) {
                    try {
                        JSONObject object = new JSONObject(content);
                        int yes = object.getInt("status");
                        if(yes==200){
                            Log.d(Constants.DOMAIN,"登录成功");
                            MyToast.show(LoginActivity.this,"Login successful!");
                            UserUtil.setSession(object.getString("data"));
                            UserUtil.setPhoneNum(mPhoneNumber);
                            sendIP();
                            Intent intent =new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            MyToast.show(LoginActivity.this,"Network exception, please try again later.");

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onResponse: " + content);
            }
        });
    }

    private void changeUI(int type){
        if(type==1){//接收成功otp，tv显示，且不可点击
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.GONE);
                    tvOTP.setVisibility(View.GONE);
                    tvUnClickOTP.setVisibility(View.VISIBLE);
                    tvUnClickOTP.setText(getmOTP);
                }
            });
        }else if(type==2){//返回OTP失败，需要重新发送
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.GONE);
                    tvUnClickOTP.setVisibility(View.GONE);
                    tvOTP.setVisibility(View.VISIBLE);
                }
            });
        }



    }


    private void sendIP() {
        String IP = NetUtil.getIpAdress(this);
        String brand = android.os.Build.BRAND;
        String model = Build.MODEL;
        String url = Constants.POST_MESSAGE;
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("ip", IP)
                .add("brand", brand)
                .add("model", model)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(Constants.CLIENT_USER_SESSION, UserUtil.getSession())
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.protocol() + " " + response.code() + " " + response.message());
                String connect = response.body().string();
                if (response != null) {
                    try {
//                        JSONObject object = new JSONObject(connect);
//                        int yes = object.getInt("status");

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onResponse: " + connect);
            }
        });

        LogUtils.d("BRAND+MODEL :","brand= "+brand+" model= "+model);
    }





}
