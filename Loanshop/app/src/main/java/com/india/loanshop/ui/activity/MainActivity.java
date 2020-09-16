package com.india.loanshop.ui.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.chaychan.library.BottomBarItem;
import com.chaychan.library.BottomBarLayout;
import com.india.loanshop.R;
import com.india.loanshop.base.BaseActivity;
import com.india.loanshop.base.BaseFragment;
import com.india.loanshop.ui.fragment.HomeFragment2;
import com.india.loanshop.ui.fragment.MeFragment2;
import com.india.loanshop.ui.fragment.OrderFragment;
import com.india.loanshop.utils.RestartAppService;
import com.india.loanshop.utils.UIUtils;
import com.india.loanshop.utils.appUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private List<BaseFragment> mFragmentList = new ArrayList<>();
    public BottomBarLayout mBottomBarLayout;
    private String strPayState;
    //    RelativeLayout loading;
    //
    private int[] mNormalIconIds = new int[]{
            R.mipmap.unselect_home,
            R.mipmap.unselect_verification, R.mipmap.unselect_me
    };

    private int[] mSelectedIconIds = new int[]{
            R.drawable.icon_home,
            R.drawable.icon_verification, R.drawable.icon_me
    };
    private int[] mTitleIds = new int[]{
            R.string.tab_home2,
            R.string.tab_order,
            R.string.tab_me2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUtil.setTranslucentStatus(this);
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mBottomBarLayout = (BottomBarLayout) findViewById(R.id.layout_bottom_bat);
    }


    @Override
    protected void initData() {
        if (!UIUtils.isLogin()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }else {
            getView();
        }

    }

    @Override
    protected void initListener() {
        mBottomBarLayout.setOnItemSelectedListener(new BottomBarLayout.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final BottomBarItem bottomBarItem, int previousPosition, final int currentPosition) {
                Log.i("MainActivity", "position: " + currentPosition);
                changeFragment(currentPosition);
            }
        });

    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    restartApp();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };



    @NonNull
    private BaseFragment createFragment(int titleId) {
        BaseFragment fragment = null;
        switch (titleId) {
            case R.string.tab_home2:
                fragment = new HomeFragment2();
                break;
            case R.string.tab_order:
                fragment = new OrderFragment();
                break;
            case R.string.tab_me2:
                fragment = new MeFragment2();
                break;

        }
        return fragment;
    }


    public BottomBarItem createBottomBarItem(int i) {
        BottomBarItem item = new BottomBarItem.Builder(this)
                .titleTextSize(8)
                .titleNormalColor(R.color.home_explain_text)
                .titleSelectedColor(R.color.home_btn)
//              .openTouchBg(false)
//              .marginTop(5)
//              .itemPadding(5)
//              .unreadNumThreshold(99)
//              .unreadTextColor(R.color.white)
                //还有很多属性，查看Builder里面的方法
                .create(mNormalIconIds[i], mSelectedIconIds[i], getString(mTitleIds[i]));
        return item;
    }


    private void changeFragment(int currentPosition) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_content, mFragmentList.get(currentPosition));
        transaction.commit();
    }


    private void getView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mTitleIds.length; i++) {
                    //创建item
                    BottomBarItem item = createBottomBarItem(i);
                    mBottomBarLayout.addItem(item);
                    BaseFragment homeFragment = createFragment(mTitleIds[i]);
                    mFragmentList.add(homeFragment);
                }
                changeFragment(0); //默认显示第一页
//                loading.setVisibility(View.GONE);
//                hideLoading();
                mLoadingPager.showContent();
            }
        });


    }

    private void getLoadingState(int type) {//type[1：网络超时 2.捕捉错误,请求返回错误码404,401。 3.data为空，暂无数据 4.等待加载页面
        switch (type) {
            case 1:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingPager.showNetWorkError();
                        restartAPPDialog();
                    }
                });
                break;
            case 2:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingPager.showError();
                        restartAPPDialog();
                    }
                });
                break;
            case 3:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingPager.showEmptyData();
                        restartAPPDialog();
                    }
                });
                break;
            case 4:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingPager.showLoading();
                    }
                });
                break;

        }

    }

    private void restartAPPDialog() {
        AlertDialog isExit = new AlertDialog.Builder(this).create();
        // 设置对话框标题
        isExit.setTitle("Tips");
        // 设置对话框消息
        isExit.setMessage("Loading failed, please try again in three seconds.");
        // 添加选择按钮并注册监听
        isExit.setButton("OK", listener);
        // 显示对话框
        isExit.show();
    }

    private void restartApp() {
        Intent intent = new Intent(this, RestartAppService.class);
        intent.putExtra("packageName", getApplication().getPackageName());
        startService(intent);
        appExit();
    }

    private void appExit() {
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }




}
