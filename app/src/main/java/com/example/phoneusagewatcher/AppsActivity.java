package com.example.phoneusagewatcher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import getappusageinfo.PackageInfo;
import getappusageinfo.UseTimeDataManager;

import static getappusageinfo.DateTransUtils.milliseconds2hms;

public class AppsActivity extends AppCompatActivity {

//    private RecyclerView RvGrid;
//    private RecyclerView.Adapter mAdapter;

    private TableLayout apps_talbe;
    private  UseTimeDataManager mUseTimeDataManager;
    private Handler mHandler;
    Context context;

    //是否使用特殊的标题栏背景颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;


    //导航栏-----
    private Toolbar myToolbar;
//    private LayoutInflater layoutInflater;
//    private ViewGroup customView;
//    private ViewGroup customCoverView;
//    private WindowManager wm;
//    private DisplayMetrics metrics;
//    private PopupWindow popupWindow;
//    private PopupWindow popupCover;
//    private LinearLayout main;
    //导航栏-----

    List<String> appsInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

//        RvGrid = findViewById(R.id.app_rv);
//        RvGrid.setLayoutManager(new GridLayoutManager(AppsActivity.this,3));
//        mAdapter = new AppsRvAdapter(appsInfo);
//        RvGrid.setAdapter(mAdapter);


        setStatusBar();
        //Table layout
        initialTableLayout();

        mUseTimeDataManager = UseTimeDataManager.getInstance(AppsActivity.this);
        mUseTimeDataManager.refreshData(0,0);
        final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();

        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                for (int i = 0; i < packageInfos.size(); i++) {
                    Log.d("SIZE", packageInfos.size()+"");
                    TableRow row = new TableRow(getApplicationContext());
                    TextView appname= new TextView(getApplicationContext());
                    //View appname;
                    TextView usedcount = new TextView(getApplicationContext());
                    TextView usedtime = new TextView(getApplicationContext());
                    Drawable icon;
                    //icon
                    ImageView img = new ImageView(getApplicationContext());
                    icon = packageInfos.get(i).getAppIcon(AppsActivity.this,packageInfos.get(i).getmPackageName());
                    img.setImageDrawable(icon);
                    row.addView(img);
                    //name
                    //LayoutInflater layoutInflater = (LayoutInflater) AppsActivity.this.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
                   // appname = layoutInflater.inflate(R.id.txv,null);
                   // appname.setBackgroundResource(R.id.txv);
                    //appname=findViewById(R.id.txv);
                    appname.setText(packageInfos.get(i).getmAppName());
                    appname.setTextColor(Color.BLACK);
                    row.addView(appname);
                    //count
                    usedcount.setText(packageInfos.get(i).getmUsedCount()+"");
                    usedcount.setTextColor(Color.BLACK);
                    //坑，当TextView对象中的setText()传入int值时，TextView对象会认为传入的是资源文件的id，于是它去资源文件中查找这个资源的id
                    //所以后面加""
                    row.addView(usedcount);
                    //time
                    usedtime.setText(milliseconds2hms(packageInfos.get(i).getmUsedTime()));
                    usedtime.setTextColor(Color.BLACK);
                    row.addView(usedtime);
                    row.setVerticalGravity(122);

                    apps_talbe.addView(row);
                    Log.d("TAG", packageInfos.get(i).getmAppName()+packageInfos.get(i).getmPackageName());
                }

            }
        };
        mHandler.sendEmptyMessage(1);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d("TAG", "无权限");
            //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.PACKAGE_USAGE_STATS},0);
            checkUsagePermission();
        }



        //导航栏
        myToolbar = findViewById(R.id.apps_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //initpopupWindow();
        myToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_black_24dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppsActivity.this,MainActivity.class));
            }
        });
    }


    //坑，关于PACKAGE_USAGE_STATS，requestPermissions不会提醒，需要以下两个方法
    private boolean checkUsagePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, 1);
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (!granted) {
                Toast.makeText(this, "请开启该权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void initialTableLayout()
    {
        apps_talbe = findViewById(R.id.apps_table);
        apps_talbe.setStretchAllColumns(true);
        TableRow row = new TableRow(getApplicationContext());
        TextView space =new TextView(getApplicationContext());
        TextView app= new TextView(getApplicationContext());
        TextView times = new TextView(getApplicationContext());
        TextView duration = new TextView(getApplicationContext());
        space.setText("");
        space.setTextSize(20);
        row.addView(space);
        app.setText("应用");
        app.setTextSize(20);
        app.setTextColor(Color.BLACK);
        row.addView(app);
        times.setText("打开次数");
        times.setTextSize(20);
        times.setTextColor(Color.BLACK);
        row.addView(times);
        duration.setText("使用时间");
        duration.setTextSize(20);
        duration.setTextColor(Color.BLACK);
        row.addView(duration);
        row.setDividerPadding(20);
        apps_talbe.addView(row);
    }

    //顶部状态栏----------
    public void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //根据上面设置是否对状态栏单独设置颜色
            if (useThemestatusBarColor) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));//设置状态栏背景色
            } else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);//透明
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        } else {
            Toast.makeText(this, "低于4.4的android系统版本不存在沉浸式状态栏", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && useStatusBarColor) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
