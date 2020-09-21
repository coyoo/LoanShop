package com.india.microloan.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.india.microloan.R;
import com.india.microloan.base.BaseFragment;
import com.india.microloan.constant.Constants;
import com.india.microloan.model.mode.OrderData;
import com.india.microloan.present.adapter.OrderListAdapter;
import com.india.microloan.ui.activity.LoginActivity;
import com.india.microloan.ui.activity.MainActivity;
import com.india.microloan.ui.activity.WebDownloadActivity;
import com.india.microloan.ui.view.DownloadCircleDialog;
import com.india.microloan.utils.DownloadUtils;
import com.india.microloan.utils.MyToast;
import com.india.microloan.utils.SDCardUtils;
import com.india.microloan.utils.TextUtils;
import com.india.microloan.utils.UIUtils;
import com.india.microloan.utils.UserUtil;
import com.india.microloan.utils.appUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.android.volley.VolleyLog.TAG;

public class OrderFragment extends BaseFragment{


    RelativeLayout loading;
    OrderListAdapter adapter;
    LinearLayout layoutBack;
    DownloadCircleDialog dialogProgress;

    private ListView listView;
    private List<OrderData> listDataList = new ArrayList<OrderData>();

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_order;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        setCusTomeTitle(view,"All Loans");
        loading=view.findViewById(R.id.loaddata);
        listView = (ListView) view.findViewById(R.id.find_listView);
        layoutBack=view.findViewById(R.id.tv_jiantou);
    }



    @Override
    protected void initData() {
        layoutBack.setVisibility(View.INVISIBLE);
        if(!UIUtils.isLogin()){
            Intent intent=new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
            MainActivity mainActivity= (MainActivity) getActivity();
            mainActivity.mBottomBarLayout.setCurrentItem(0);
        }else{
            loading.setVisibility(View.VISIBLE);
            getAllLoanMessage();
        }

        dialogProgress = new DownloadCircleDialog(getActivity());



    }

    @Override
    protected void initListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), WebDownloadActivity.class);
                if (position - 1 >= listDataList.size()) {
                    return;
                }
                OrderData data1 = listDataList.get(position);
                String str=data1.getIntentUrl().substring(data1.getIntentUrl().length()-3,data1.getIntentUrl().length());
//                Log.d("lhm==========apk",str);
                if(str.equals("apk")){
                    showNewVersion(data1.getIntentUrl(),data1.getUserName(),data1.getProductId());
                }else{
                    intent.putExtra("url", data1.getIntentUrl());
                    intent.putExtra("title",data1.getUserName());
                    intent.putExtra("productId",data1.getProductId());
                    startActivity(intent);
                }

            }
        } );
    }
    private void initListData() {
        adapter = new OrderListAdapter(getActivity(),R.layout.item_order_content,listDataList );
        listView.setAdapter(adapter);
        setListHeight();
    }


    private void setListHeight(){
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {// 不在最前端界面显示
        } else {// 重新显示到最前端中
            initData();
        }

    }

    private void getAllLoanMessage(){
        String url = Constants.GET_ALL_LOAN;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .addHeader(Constants.CLIENT_USER_SESSION,UserUtil.getSession())
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                            listDataList = new ArrayList<OrderData>();
                            JSONObject objectData =object.getJSONObject("data");
                            JSONArray jsonArray=objectData.getJSONArray("list");
                            int lenth=objectData.getInt("total");
                            for (int i = 0; i < lenth; i++) {// 最后一个不显示
                                String UserIcon=jsonArray.getJSONObject(i).getString("productLogo");
                                String UserName=jsonArray.getJSONObject(i).getString("productName");
                                String UserQuota= jsonArray.getJSONObject(i).getString("maxAmount");
                                String UserInterest=jsonArray.getJSONObject(i).getString("showRate")+"%";
                                String UserTime=jsonArray.getJSONObject(i).getString("maxPeriod");
                                String UserStage=jsonArray.getJSONObject(i).getString("isQuota");
                                String IntentUrl=jsonArray.getJSONObject(i).getString("linkAddress");
                                String ProductID=jsonArray.getJSONObject(i).getString("id");
                                if(UserStage.equals("0")){
                                    UserStage="Unlimited";
                                }else if(UserStage.equals("1")){
                                    UserStage="Quota";
                                }
                                OrderData data = new OrderData(UserIcon,UserName,UserQuota,UserInterest,UserTime,UserStage,IntentUrl,ProductID);
                                listDataList.add(data);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initListData();
                                    loading.setVisibility(View.GONE);
                                }
                            });
                        }else{
                            MyToast.show(getActivity(), "Network exception, please try again later.");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onResponse: " + content);
            }
        });
    }


    //1.权限申请，通过后开始下载
    public void showNewVersion(final String url, final String name,final String productId) {
        AndPermission.with(this)
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> data) {

                    Log.e(TAG,"以获得权限");
                        new AlertDialog.Builder(getActivity()).setTitle(name).setMessage("Do you want to download "+name +" now ?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        downloadApk(getActivity(), url,name);
                                        postProductClick(productId);
                                    }
                                })
                                .setNegativeButton("CANCEL", null).show();

                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.e(TAG,"未获得权限"+data.toString());
                    }
                }).start();
    }
    //2.开始下载apk

    public void downloadApk(final Activity context, String down_url,final String name) {
        dialogProgress.show();
        DownloadUtils.getInstance().download(down_url, SDCardUtils.getSDCardPath(), name+".apk", new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                dialogProgress.dismiss();
//                Log.i(TAG,"恭喜你下载成功，开始安装！==" + SDCardUtils.getSDCardPath() + name+".apk");
                MyToast.show(getContext(),"Congratulations on your successful download and installation!");
                String successDownloadApkPath = SDCardUtils.getSDCardPath() + name+".apk";
                installApkO(getContext(), successDownloadApkPath);
            }
            @Override
            public void onDownloading(ProgressInfo progressInfo) {
                dialogProgress.setProgress(progressInfo.getPercent());
                boolean finish = progressInfo.isFinish();
                if (!finish) {
                    long speed = progressInfo.getSpeed();
                    dialogProgress.setMsg("(" + (speed > 0 ? TextUtils.formatFileSize(speed) : speed) + "/s)Downloading...");
                } else {
                    dialogProgress.setMsg("Download complete !");
                }
            }
            @Override
            public void onDownloadFailed() {
                dialogProgress.dismiss();
                MyToast.show(getContext(),"Download failed !");
            }
        });

    }
    // 3.下载成功，开始安装,兼容8.0安装位置来源的权限
    private void installApkO(Context context, String downloadApkPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //是否有安装位置来源的权限
            boolean haveInstallPermission = SDCardUtils.isSDCardEnable();
            if (haveInstallPermission) {
                Log.i(TAG,"8.0手机已经拥有安装未知来源应用的权限，直接安装！");
                appUtil.installApk(context, downloadApkPath);
            } else {
                showMyDialog();
            }
        } else {
            appUtil.installApk(context, downloadApkPath);
        }
    }


    //4.开启了安装未知来源应用权限后，再次进行步骤3的安装。
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086) {
            Log.i(TAG,"设置了安装未知应用后的回调。。。");
            String successDownloadApkPath = SDCardUtils.getSDCardPath() + "QQ.apk";
            installApkO(getContext(), successDownloadApkPath);
        }
    }


    private void showMyDialog() {
        // 创建退出对话框
        android.app.AlertDialog isExit = new android.app.AlertDialog.Builder(getActivity()).create();
        // 设置对话框标题
        isExit.setTitle("Tips");
        // 设置对话框消息
        isExit.setMessage("To install an application, you need to open the permission to install an application from an unknown source. Please open the permission in the settings");
        // 添加选择按钮并注册监听
        isExit.setButton("OK", listener);
        isExit.setButton2("CANCEL", listener);
        // 显示对话框
        isExit.show();
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case android.app.AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    Uri packageUri = Uri.parse("package:"+ appUtil.getPackageName(getContext()));
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageUri);
                    startActivityForResult(intent,10086);
                    break;
                case android.app.AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    private void postProductClick(String productId){
        String url = Constants.POST_PRODUCT;
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("productId",productId)
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
                        JSONObject object = new JSONObject(connect);
                        int yes = object.getInt("status");

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onResponse: " + connect);
            }
        });

    }

}
