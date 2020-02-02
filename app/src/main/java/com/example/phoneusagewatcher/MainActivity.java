package com.example.phoneusagewatcher;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import AAChartCoreLib.AAChartConfiger.AAChartModel;
import AAChartCoreLib.AAChartConfiger.AAChartView;
import AAChartCoreLib.AAChartEnum.AAChartType;
import AAChartCoreLib.AAOptionsModel.AAPie;
import getappusageinfo.PackageInfo;
import getappusageinfo.UseTimeDataManager;

import static com.example.phoneusagewatcher.WeekStats.weekAverage;
import static com.example.phoneusagewatcher.WeekStats.weekUsage;
import static getappusageinfo.DateTransUtils.milliseconds2hms;


public class MainActivity extends AppCompatActivity {

    //导航栏
    private Toolbar myToolbar;
    private LayoutInflater layoutInflater;
    private ViewGroup customView;
    private ViewGroup customCoverView;
    private WindowManager wm;
    private DisplayMetrics metrics;
    private PopupWindow popupWindow;
    private PopupWindow popupCover;
    private LinearLayout main;
    //应用详情
    private TextView appsinfo_tv;
    private ImageView appsinfo_iv;
    private  UseTimeDataManager mUseTimeDataManager;
    //一周统计
    private TextView weekstats_tv;
    private ImageView weekstats_iv;
    //关于
    private TextView about_tv;
    private ImageView about_iv;
    //扇形图
    AAChartModel piegraph;
    private AAChartView aaChartView;

    //解锁次数统计
//    private LockScreenStateReceiver mLockScreenStateReceiver;
//    public static int count = 0;
    //使用时间
    private TextView usagetime;
    private TextView date;

    //一周统计
    private static int initialflag=0;
    public static List<PackageInfo> WeekPackageInfos;

    //是否使用特殊的标题栏背景颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;




    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d("TAG", "无权限");
            //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.PACKAGE_USAGE_STATS},0);
            checkUsagePermission();
        }
        //实现沉浸式状态栏
        setStatusBar();


        //顶部导航栏
        myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initpopupWindow();
        myToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpView();
            }
        });

        //解锁次数统计
//        mLockScreenStateReceiver = new LockScreenStateReceiver();
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_USER_PRESENT);
//
//        registerReceiver(mLockScreenStateReceiver, filter);
//        Log.d("解锁", count+"次");
        //
//        EventStats eventStats = new EventStats();
//        eventStats.
        //使用时间
        initialcard2();

        //扇形图
        aaChartView = findViewById(R.id.main_ChartView);
        initialPieGraph();

        //
        Log.d("flag", "onCreate: "+initialflag);
        if(initialflag==0)
            initial_weekstats();
        else;




    }

    //-----导航栏-----
    public void initpopupWindow()
    {
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
        customView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout,null);
        customCoverView= (ViewGroup) layoutInflater.inflate(R.layout.cover_layout,null);
        main = findViewById(R.id.main_layout);
        wm = getWindowManager();
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
    }

    public void showPopUpView()
    {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        popupCover = new PopupWindow(customCoverView,width,height,false);
        popupWindow = new PopupWindow(customView,(int)(width*0.7),height,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        //在主界面加载成功之后弹出
        findViewById(R.id.main_layout).post(new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY,0,0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY,0,0);

                //点击灰色蒙版部分后其自身也消失
                customCoverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return false;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
        //应用详情
        appsinfo_iv = customView.findViewById(R.id.apps_image);//注意是customView.find...
        appsinfo_tv = customView.findViewById(R.id.apps_text);
        appsinfo_tv.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
               startActivity(new Intent(MainActivity.this,AppsActivity.class));
             }
        });
        appsinfo_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AppsActivity.class));
            }
        });

        //一周统计
        weekstats_iv=customView.findViewById(R.id.graph_image);
        weekstats_tv=customView.findViewById(R.id.graph_text);
        weekstats_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,WeekStats.class));
            }
        });
        weekstats_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,WeekStats.class));
            }
        });

        //关于
        about_iv=customView.findViewById(R.id.about_image);
        about_tv=customView.findViewById(R.id.about_text);
        about_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AboutUs.class));
            }
        });
        about_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AboutUs.class));
            }
        });





    }
    //-----导航栏-----

    public int getPhoneUsedTime()
    {
        int sumtime=0;
        mUseTimeDataManager = UseTimeDataManager.getInstance(MainActivity.this);
        mUseTimeDataManager.refreshData(0,0);
        final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();
        for (int i = 0; i < packageInfos.size(); i++) {
        sumtime+=packageInfos.get(i).getmUsedTime();
    }
        return sumtime;
    }

    public void initialcard2()
    {
        usagetime=findViewById(R.id.main_usagetime);
        usagetime.setText("今日已使用\n"+milliseconds2hms(getPhoneUsedTime()));
        date=findViewById(R.id.main_date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
//获取当前时间
        Date datetoday = new Date(System.currentTimeMillis());
        date.setText(""+simpleDateFormat.format(datetoday));

    }

    public void initialPieGraph()
    {
        //getTop5
        mUseTimeDataManager = UseTimeDataManager.getInstance(MainActivity.this);
        mUseTimeDataManager.refreshData(0,0);
        final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();

        piegraph = new AAChartModel().chartType(AAChartType.Pie)
                .backgroundColor("#ffffff")
                .title("屏幕时间占比")
                .subtitle("top 5")
                .dataLabelsEnabled(true)//是否直接显示扇形图数据
                .yAxisTitle("℃")
                .series(new AAPie[] {
                                new AAPie()
                                        .name("Language market shares")
                                        .innerSize("20%")
                                        .data(new Object[][] {
                                        {packageInfos.get(0).getmAppName()  ,(int)packageInfos.get(0).getmUsedTime()},
                                        {packageInfos.get(1).getmAppName()  ,(int)packageInfos.get(1).getmUsedTime()},
                                        {packageInfos.get(2).getmAppName()  ,(int)packageInfos.get(2).getmUsedTime()},
                                        {packageInfos.get(3).getmAppName()  ,(int)packageInfos.get(3).getmUsedTime()},
                                        {packageInfos.get(4).getmAppName()  ,(int)packageInfos.get(4).getmUsedTime()},
                                })
                                ,
                        }
                );

        aaChartView.aa_drawChartWithChartModel(piegraph);
    }

    //
    public void initial_weekstats()
    {
        mUseTimeDataManager = UseTimeDataManager.getInstance(MainActivity.this);
        for(int i=0;i<7;i++)
        {
            mUseTimeDataManager.refreshData(i,0);
            final List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();
            int sumtime=0;
            for (int j = 0; j < packageInfos.size(); j++) {
                sumtime+=packageInfos.get(j).getmUsedTime();
            }
            double hour = ((sumtime*1.0) / (1000.0 * 60.0*60.0));
            hour = (double) Math.round(hour * 10) / 10;
            //hour=(double)(Math.round(hour*10)/10);
            weekUsage[i]=hour;
            weekAverage+=weekUsage[i];

            Log.d("sumtime", ""+sumtime);
            Log.d("hour", ""+weekUsage[i]);
            Log.d("statsk", ""+milliseconds2hms(sumtime));
        }
        weekAverage=weekAverage/7.0;
        weekAverage=(double) Math.round(weekAverage * 10) / 10;
        mUseTimeDataManager = UseTimeDataManager.getInstance(MainActivity.this);
        mUseTimeDataManager.refreshData(7,1);
        WeekPackageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();






        initialflag=1;
    }

    //权限-----------------
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
    //权限--------------

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
